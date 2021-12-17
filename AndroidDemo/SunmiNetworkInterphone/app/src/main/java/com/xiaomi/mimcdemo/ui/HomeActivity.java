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
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.ActivityHomeBinding;
import com.xiaomi.mimcdemo.manager.ContactManager;
import com.xiaomi.mimcdemo.service.TalkService;
import com.xiaomi.mimcdemo.utils.LogUtil;

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
                    uiRefreshContact();
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

                if(msg.what == MSG_NEW_USER_NAME){
                    LogUtil.e(TAG, "HomeActivity new User title");
                    String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
                    binding.tvUserTitle.setText(name);
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
                Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_SCAN);
            }
        });
        // 点击更改User title
        binding.tvUserTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditUserTitleFragment fragment = EditUserTitleFragment.newInstance();
                fragment.show(getFragmentManager(), EditUserTitleFragment.TAG);
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
//        LogUtil.e(TAG, "now ui refresh contact");
//
//        // 获取所有的联系人信息
//        List<Contact> contacts = MainApplication.getInstance().queryData();
//        if (contacts == null || contacts.size() == 0) {
//            LogUtil.e(TAG, "no contact");
//            binding.tvNoContact.setVisibility(View.VISIBLE);
//            binding.rvContactList.setVisibility(View.GONE);
//            return;
//        }
//
//        contactAdapter = new ContactAdapter(contacts);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        binding.rvContactList.setLayoutManager(linearLayoutManager);
//        binding.rvContactList.setAdapter(contactAdapter);
//        binding.tvNoContact.setVisibility(View.GONE);
//        binding.rvContactList.setVisibility(View.VISIBLE);
//
//        binding.llContactList.invalidate();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT_SCAN) {
            if (data != null) {
                String qrInfo = data.getStringExtra(CustomKeys.KEY_QR_INFO);
                LogUtil.e(TAG, "qrInfo is: " + qrInfo);
                String sn = qrInfo.substring(0, 16);
                String customName = qrInfo.replaceAll(sn, "");
                LogUtil.e(TAG, "SN is: " + sn);
                LogUtil.e(TAG, "Custom Name: " + customName);

                if (sn.contains(MainApplication.getInstance().getSerial())) {
                    LogUtil.e(TAG, "detect it's own id, refuse!");
                    Toast.makeText(HomeActivity.this, "无法添加本机为联系人", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean result = ContactManager.getInstance().insertData(customName, sn);
                if (result) {
                    Toast.makeText(HomeActivity.this, "添加联系人成功", Toast.LENGTH_SHORT).show();
                    Message message1 = Message.obtain();
                    message1.what = MSG_ADD_CONTACT;
                    mainHandler.sendMessage(message1);
                }
            } else {
                LogUtil.e(TAG, "activity result is null");
                Toast.makeText(HomeActivity.this, "未获取到联系人信息", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == ACTIVITY_RESULT_ADD_CONTACT) {
            if (data != null) {
                String name = data.getStringExtra(CustomKeys.KEY_USER_NAME);
                String sn = data.getStringExtra(CustomKeys.KEY_SN);

                LogUtil.e(TAG, "Custom Name: " + name);
                LogUtil.e(TAG, "SN is: " + sn);

//                boolean ret = MainApplication.getInstance().insertData(name, sn);
//                if (ret) {
//                    Message message1 = Message.obtain();
//                    message1.what = MSG_ADD_CONTACT;
//                    mainHandler.sendMessage(message1);
//                }
            } else {
                LogUtil.e(TAG, "add result is null");
                Toast.makeText(HomeActivity.this, "取消增加联系人", Toast.LENGTH_SHORT).show();
            }
        }
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
        }
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
