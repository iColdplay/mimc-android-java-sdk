package com.xiaomi.mimcdemo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mumu.dialog.MMLoading;
import com.tencent.mmkv.MMKV;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.ActivityHomeBinding;
import com.xiaomi.mimcdemo.databinding.DesignContactItemBinding;
import com.xiaomi.mimcdemo.manager.ContactManager;
import com.xiaomi.mimcdemo.manager.SDKUserBehaviorManager;
import com.xiaomi.mimcdemo.service.TalkService;
import com.xiaomi.mimcdemo.utils.LogUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final long DOUBLE_CLICK_BACK_CHECK = 2000L;

    public static final int ACTIVITY_RESULT_SCAN = 10001;
    public static final int ACTIVITY_RESULT_ADD_CONTACT = 10002;

    private ActivityHomeBinding binding;

    private MMKV mmkv;

    private long exitTime;

    public static Handler mainHandler;
    public static final int MSG_LOGOUT = 1001;
    public static final int MSG_ADD_CONTACT = 1002;
    public static final int MSG_DELETE_CONTACT = 1003;
    public static final int MSG_NEW_USER_NAME = 1004;
    public static final int MSG_SHOW_LOADING = 1005;
    public static final int MSG_HIDE_LOADING = 1006;

    private ContactAdapter contactAdapter;

    public static Collection pongSet;

    private TalkService talkService;
    private TalkService.TalkServiceBinder binder;
    private TalkService.DataCallback dataCallback = new TalkService.DataCallback() {
        @Override
        public void onDataCallback(Bundle data) {

        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.e(TAG, "onServiceConnected()");
            binder = (TalkService.TalkServiceBinder) service;
            talkService = binder.getService();
            talkService.setCallback(dataCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.e(TAG, "onServiceDisconnected()");
        }
    };

    // modify contact
    public static ArrayList<String> contactList = new ArrayList<>();

    // loading dialog
    private MMLoading mmLoading;

    protected void showLoading(String msg, boolean outCancel) {
        if (mmLoading == null) {
            MMLoading.Builder builder = new MMLoading.Builder(this)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setCancelOutside(outCancel);
            mmLoading = builder.create();
        } else {
            mmLoading.dismiss();
            MMLoading.Builder builder = new MMLoading.Builder(this)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setCancelOutside(outCancel);
            mmLoading = builder.create();
        }
        mmLoading.show();
    }

    /**
     * 100ms, to dismiss the dialog
     */
    protected void hideLoading() {
        long startDismiss = System.currentTimeMillis();
        while (System.currentTimeMillis() - startDismiss < 100) {
            if (mmLoading != null && mmLoading.isShowing()) {
                mmLoading.dismiss();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e(TAG, "onCreate()");

        Intent serviceIntent = new Intent(this, TalkService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }

        Intent bindIntent = new Intent(this, TalkService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        setNBSBColor(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        requestPermissions(new String[]{"android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                "android.permission.CAMERA",
                "android.permission.MODIFY_AUDIO_SETTINGS"}, 0);

        mainHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_LOGOUT) {
                    LogUtil.e(TAG, "HomeActivity logout");
                }

                if (msg.what == MSG_ADD_CONTACT) {
                    LogUtil.e(TAG, "HomeActivity add contact");
                    Bundle data = msg.getData();
                    String name = data.getString(CustomKeys.KEY_USER_NAME);
                    String sn = data.getString(CustomKeys.KEY_SN);
                    LogUtil.e(TAG, "Custom Name: " + name);
                    LogUtil.e(TAG, "SN is: " + sn);
                    boolean ret = ContactManager.getInstance().insertData(name, sn);
                    if (ret) {
                        Toast.makeText(HomeActivity.this, "添加联系人成功", Toast.LENGTH_SHORT).show();
                        uiRefreshContact();
                    } else {
                        Toast.makeText(HomeActivity.this, "添加联系人失败, 请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                }

                if (msg.what == MSG_DELETE_CONTACT) {
                    LogUtil.e(TAG, "HomeActivity delete contact");
                    Bundle data = msg.getData();
//                    boolean ret = MainApplication.getInstance().deleteDataBySN(data.getString(CustomKeys.KEY_SN));
//                    if (ret) {
                    Toast.makeText(HomeActivity.this, "删除联系人成功", Toast.LENGTH_SHORT).show();
//                    }
                    uiRefreshContact();
                }

                if (msg.what == MSG_NEW_USER_NAME) {
                    LogUtil.e(TAG, "HomeActivity new User title");
                    String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
                    binding.tvUserTitle.setText(name);
                }

                if(msg.what == MSG_SHOW_LOADING){
                    LogUtil.e(TAG, "HomeActivity MSG_SHOW_LOADING handle start");
                    showLoading("正在连线", false);
                }

                if(msg.what == MSG_HIDE_LOADING){
                    LogUtil.e(TAG, "HomeActivity MSG_HIDE_LOADING handle start");
                    hideLoading();
                }
            }
        };

        mmkv = MMKV.defaultMMKV();
        Set<String> pongs = new HashSet<>();
        pongSet = Collections.synchronizedSet(pongs);

        // 显示ID信息
        String id = MainApplication.getInstance().getSerial();
        binding.tvIdInfo.setText("ID: " + id);

        initView();
    }

    private void initView() {
        // 快捷侧边按键设置跳转
        final Intent intentGoSettingCustomKey = new Intent();
        intentGoSettingCustomKey.setAction("com.sunmi.toolbox.customkey");
        binding.btnSetCustomKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.this.startActivity(intentGoSettingCustomKey);
            }
        });
        // 展示本机联系人信息
        binding.llIdInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 展示id+名称的二维码
                QRCodeFragment qrCodeFragment = QRCodeFragment.newInstance();
                qrCodeFragment.show(getFragmentManager(), QRCodeFragment.TAG);
            }
        });
        // 扫码添加联系人
        binding.tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContactAdapter.isAnythingInConnection){
                    LogUtil.e(TAG, "something is in connection, ignore this click 1");
                    return;
                }

                Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_SCAN);
            }
        });
        // 点击更改User title
        binding.tvUserTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "now show EditUserTitleFragment UI");
                EditUserTitleFragment fragment = EditUserTitleFragment.newInstance();
                fragment.show(getFragmentManager(), EditUserTitleFragment.TAG);
            }
        });
        // 点击添加联系人
        binding.tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContactAdapter.isAnythingInConnection){
                    LogUtil.e(TAG, "something is in connection, ignore this click 1");
                    return;
                }

                Intent it = new Intent(HomeActivity.this, ScanActivity.class);
                startActivityForResult(it, ACTIVITY_RESULT_SCAN);
            }
        });
        // 编辑模式触发
        binding.tvModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContactAdapter.isAnythingInConnection){
                    LogUtil.e(TAG, "something is in connection, ignore this click 1");
                    return;
                }

                if(binding.tvModifyCancel.getVisibility() == View.VISIBLE){
                    LogUtil.e(TAG, "no more need to activate the modify mode, just return");
                    return;
                }
                ContactAdapter.shouldShowEditView = true;
                uiRefreshContact();
                binding.tvModifyCancel.setVisibility(View.VISIBLE);
                binding.tvModifyConfirm.setVisibility(View.VISIBLE);
                contactList.clear();
            }
        });
        // 编辑模式底部按钮初始化
        binding.tvModifyCancel.setVisibility(View.INVISIBLE);
        binding.tvModifyCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContactAdapter.shouldShowEditView = false;
                uiRefreshContact();
                binding.tvModifyCancel.setVisibility(View.INVISIBLE);
                binding.tvModifyConfirm.setVisibility(View.INVISIBLE);
            }
        });
        binding.tvModifyConfirm.setVisibility(View.INVISIBLE);
        binding.tvModifyConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(contactList.size() != 0){
                    for(String sn : contactList){
                        boolean ret = ContactManager.getInstance().deleteDataBySN(sn);
                        if(ret){
                            LogUtil.e(TAG, "delete SN: " + sn + " successfully");
                        }
                    }
                }
                ContactAdapter.shouldShowEditView = false;
                uiRefreshContact();
                binding.tvModifyCancel.setVisibility(View.INVISIBLE);
                binding.tvModifyConfirm.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // user title 初始化
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "默认名称");
        binding.tvUserTitle.setText(name);
    }

    private void uiRefreshContact() {
        LogUtil.e(TAG, "now ui refresh contact");

        // 获取所有的联系人信息
        List<Contact> contacts = ContactManager.getInstance().queryData();
        if (contacts == null || contacts.size() == 0) {
            LogUtil.e(TAG, "no contact");
            binding.imageNoContact.setVisibility(View.VISIBLE);
            binding.tvNoContact.setVisibility(View.VISIBLE);
            binding.rvContactList.setVisibility(View.GONE);
            binding.tvModify.setVisibility(View.GONE);
            binding.verticalDivider.setVisibility(View.GONE);
            return;
        }

        // 刷新RecycleView
        binding.tvNoContact.setVisibility(View.GONE);
        binding.imageNoContact.setVisibility(View.GONE);
        binding.rvContactList.setVisibility(View.VISIBLE);
        binding.tvModify.setVisibility(View.VISIBLE);
        binding.verticalDivider.setVisibility(View.VISIBLE);
        contactAdapter = new ContactAdapter(contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvContactList.setLayoutManager(linearLayoutManager);
        binding.rvContactList.setAdapter(contactAdapter);
        binding.rvContactList.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_SCAN) {
            if (data != null) {
                String qrInfo = data.getStringExtra(CustomKeys.KEY_QR_INFO);
                LogUtil.e(TAG, "qrInfo is: " + qrInfo);
                // 展示qr
                if (qrInfo.contains("my_qr")) {
                    binding.llIdInfo.performClick();
                    return;
                }

                // 手动add
                if (qrInfo.contains("hand_add")) {
                    EditHandAddFragment fragment = EditHandAddFragment.newInstance();
                    fragment.show(getFragmentManager(), EditHandAddFragment.TAG);
                    return;
                }

                String sn = qrInfo.substring(0, 16);
                String customName = qrInfo.replaceAll(sn, "");
                LogUtil.e(TAG, "SN is: " + sn);
                LogUtil.e(TAG, "Custom Name: " + customName);

                if (sn.contains(MainApplication.getInstance().getSerial())) {
                    LogUtil.e(TAG, "detect it's own id, refuse!");
                    Toast.makeText(HomeActivity.this, "无法添加本机为联系人", Toast.LENGTH_SHORT).show();
                    return;
                }

                Message message1 = Message.obtain();
                message1.what = MSG_ADD_CONTACT;
                Bundle contact = new Bundle();
                contact.putString(CustomKeys.KEY_USER_NAME, customName);
                contact.putString(CustomKeys.KEY_SN, sn);
                message1.setData(contact);
                mainHandler.sendMessage(message1);

            } else {
                LogUtil.e(TAG, "activity result is null");
                Toast.makeText(HomeActivity.this, "未获取到联系人信息", Toast.LENGTH_SHORT).show();
            }
        }

//        if (requestCode == ACTIVITY_RESULT_ADD_CONTACT) {
//            if (data != null) {
//                String name = data.getStringExtra(CustomKeys.KEY_USER_NAME);
//                String sn = data.getStringExtra(CustomKeys.KEY_SN);
//
//                LogUtil.e(TAG, "Custom Name: " + name);
//                LogUtil.e(TAG, "SN is: " + sn);
//
////                boolean ret = MainApplication.getInstance().insertData(name, sn);
////                if (ret) {
////                    Message message1 = Message.obtain();
////                    message1.what = MSG_ADD_CONTACT;
////                    mainHandler.sendMessage(message1);
////                }
//            } else {
//                LogUtil.e(TAG, "add result is null");
//                Toast.makeText(HomeActivity.this, "取消增加联系人", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume()");
        // 检查是否有联系人
        List<Contact> contactList = ContactManager.getInstance().queryData();
        if (contactList == null || contactList.size() == 0) {
            LogUtil.e(TAG, "no contact");
            binding.tvModify.setVisibility(View.GONE);
            binding.verticalDivider.setVisibility(View.GONE);
            binding.imageNoContact.setVisibility(View.VISIBLE);
            binding.tvNoContact.setVisibility(View.VISIBLE);
        } else {
            binding.imageNoContact.setVisibility(View.GONE);
            binding.verticalDivider.setVisibility(View.VISIBLE);
            binding.tvModify.setVisibility(View.VISIBLE);
            binding.tvNoContact.setVisibility(View.GONE);
        }
        uiRefreshContact();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        LogUtil.e(TAG, "onBackPressed()");
        if ((System.currentTimeMillis() - exitTime) > DOUBLE_CLICK_BACK_CHECK) {
            Toast.makeText(HomeActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        }else {
            super.onBackPressed();
        }
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(msg))
                    Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***
     * 修改NavigationBar背景颜色 可自定义颜色
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setNBSBColor(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(activity.getResources().getColor(R.color.design_background));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = activity.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            activity.getWindow().setStatusBarColor(getColor(R.color.design_background));
        }
    }


}
