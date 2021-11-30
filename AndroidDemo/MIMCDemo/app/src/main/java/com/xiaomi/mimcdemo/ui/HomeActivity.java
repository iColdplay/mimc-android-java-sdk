package com.xiaomi.mimcdemo.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.MIMCUser;
import com.xiaomi.mimc.common.MIMCConstant;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.databinding.ActivityHomeBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;

import java.util.List;

public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final int ACTIVITY_RESULT_SCAN = 10001;
    public static final int ACTIVITY_RESULT_ADD_CONTACT = 10002;

    private ActivityHomeBinding binding;

    private UserManager userManager;

    private MMKV mmkv;

    private long exitTime;

    public static Handler mainHandler;
    public static final int MSG_LOGOUT = 1001;
    public static final int MSG_ADD_CONTACT = 1002;
    public static final int MSG_DELETE_CONTACT = 1003;

    private ContactAdapter contactAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e(TAG, "onCreate()");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        userManager = UserManager.getInstance();

        uiGoInitializingLayout();

        if (NetWorkUtils.isNetwork(this)) {
            uiGoLoginLayout();
        } else {
            Toast.makeText(this, "未检测到网络, 请连接网络后重试...", Toast.LENGTH_SHORT).show();
            Thread gonnaFinishThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            };
            gonnaFinishThread.start();
        }

        requestPermissions(new String[]{"android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.READ_PHONE_STATE",
                "android.permission.READ_PRIVILEGED_PHONE_STATE",
                "android.permission.CAMERA"}, 0);

        mmkv = MMKV.defaultMMKV();
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
        assert name != null;
        if (name.length() > 0 && !TextUtils.isEmpty(name)) {
            binding.et.setText(name);
            binding.et.requestFocus();
            binding.et.setSelection(binding.et.getText().toString().length());
        } else {
            binding.et.setText("");
            binding.et.setHint("Please Input ID");
        }

        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        mainHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_LOGOUT) {
                    LogUtil.e(TAG, "HomeActivity logout");
                    uiGoLoginLayout();
                }

                if (msg.what == MSG_ADD_CONTACT) {
                    LogUtil.e(TAG, "HomeActivity add contact");
                    uiRefreshContact();
                }

                if(msg.what == MSG_DELETE_CONTACT){
                    LogUtil.e(TAG, "HomeActivity delete contact");
                    Bundle data = msg.getData();
                    boolean ret = MIMCApplication.getInstance().deleteDataBySN(data.getString(CustomKeys.KEY_SN));
                    if(ret){
                        Toast.makeText(HomeActivity.this, "删除联系人成功", Toast.LENGTH_SHORT).show();
                    }
                    uiRefreshContact();
                }
            }
        };

        binding.imageQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "imageQrcode onClick()");
                doClickQrCodeImage();
            }
        });

        binding.imageScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "imageScan onClick()");
                doClickScanImage();
            }
        });

        binding.tvNoContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.e(TAG, "add Contact");
                Intent intent = new Intent(HomeActivity.this, AddContactActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_ADD_CONTACT);
            }
        });
    }

    private void uiRefreshContact() {
        LogUtil.e(TAG, "now ui refresh contact");

        // 获取所有的联系人信息
        List<Contact> contacts = MIMCApplication.getInstance().queryData();
        if (contacts == null || contacts.size() == 0) {
            LogUtil.e(TAG, "no contact");
            binding.tvNoContact.setVisibility(View.VISIBLE);
            binding.rvContactList.setVisibility(View.GONE);
            return;
        }

        contactAdapter = new ContactAdapter(contacts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.rvContactList.setLayoutManager(linearLayoutManager);
        binding.rvContactList.setAdapter(contactAdapter);
        binding.tvNoContact.setVisibility(View.GONE);
        binding.rvContactList.setVisibility(View.VISIBLE);

        binding.llContactList.invalidate();
    }

    private void doClickScanImage() {
        Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
        startActivityForResult(intent, ACTIVITY_RESULT_SCAN);
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

                if(sn.contains(MIMCApplication.getInstance().getSerial())){
                    LogUtil.e(TAG, "detect it's own id, refuse!");
                    Toast.makeText(HomeActivity.this, "无法添加本机为联系人", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean result = MIMCApplication.getInstance().insertData(customName, sn);
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

        if(requestCode == ACTIVITY_RESULT_ADD_CONTACT){
            if(data != null){
                String name = data.getStringExtra(CustomKeys.KEY_USER_NAME);
                String sn = data.getStringExtra(CustomKeys.KEY_SN);

                LogUtil.e(TAG, "Custom Name: " + name);
                LogUtil.e(TAG, "SN is: " + sn);

                boolean ret = MIMCApplication.getInstance().insertData(name, sn);
                if(ret){
                    Message message1 = Message.obtain();
                    message1.what = MSG_ADD_CONTACT;
                    mainHandler.sendMessage(message1);
                }
            }else {
                LogUtil.e(TAG, "add result is null");
                Toast.makeText(HomeActivity.this, "取消增加联系人", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doClickQrCodeImage() {
        QRCodeFragment fragment = QRCodeFragment.newInstance();
        fragment.show(getFragmentManager(), QRCodeFragment.TAG);
    }

    private void doLogin() {
        if (!NetWorkUtils.isNetwork(HomeActivity.this)) {
            Toast.makeText(HomeActivity.this, "未检测到网络, 请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.et.getText().toString().contains("Please Input ID") || TextUtils.isEmpty(binding.et.getText().toString())) {
            Toast.makeText(HomeActivity.this, "请输入有效ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.et.getText().toString().length() > 10) {
            Toast.makeText(HomeActivity.this, "ID过长, 最大长度为10", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果缓存中记录了user name, 使用缓存的user name, 如果没有则提示 Please Input ID
        String name = binding.et.getText().toString();
        mmkv.putString(CustomKeys.KEY_USER_NAME, name);
        MIMCUser user = userManager.newMIMCUser(name);
        boolean result = user.login();
        if (result) {
            hideInput();
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            uiGoNormalLayout();
        } else {
            Toast.makeText(this, "登录失败, 请稍后再试", Toast.LENGTH_SHORT).show();
        }

        binding.tvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialog logoutDialog = LogoutDialog.newInstance();
                logoutDialog.show(getFragmentManager(), LogoutDialog.TAG);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume()");
        checkIfHaveRecent();
        uiRefreshContact();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkIfHaveRecent() {
        LogUtil.e(TAG, "checkIfHaveRecent()");
        String recentContact = mmkv.getString(CustomKeys.KEY_RECENT_CONTACT, "");
        if (recentContact.length() == 0) {
            binding.tvRecentName.setText("暂无");
            binding.swipeLayout.setSwipeEnabled(false);
            binding.llSwipeInfo.setBackgroundColor(getColor(R.color.colorPrimaryDark));
        } else {
            binding.tvRecentName.setText(recentContact);
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
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(HomeActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            MIMCApplication.getInstance().exit();
        }
    }

    private void uiGoLoginLayout() {
        binding.llLogin.setVisibility(View.VISIBLE);
        binding.llInitializing.setVisibility(View.GONE);
        binding.llNormal.setVisibility(View.GONE);
        binding.et.requestFocus();
        binding.et.setSelection(binding.et.getText().toString().length()); // 定位光标到最后一个位置
    }

    private void uiGoInitializingLayout() {
        binding.llLogin.setVisibility(View.GONE);
        binding.llInitializing.setVisibility(View.VISIBLE);
        binding.llNormal.setVisibility(View.GONE);
    }

    private void uiGoNormalLayout() {
        binding.llNormal.setVisibility(View.VISIBLE);
        binding.llInitializing.setVisibility(View.GONE);
        binding.llLogin.setVisibility(View.GONE);

        // 显示 user name
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
        binding.tvUserName.setText(name);
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

}
