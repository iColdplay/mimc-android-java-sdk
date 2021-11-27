package com.xiaomi.mimcdemo.ui;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.MIMCUser;
import com.xiaomi.mimc.common.MIMCConstant;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.CustomKeys;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.databinding.ActivityHomeBinding;
import com.xiaomi.mimcdemo.utils.LogUtil;

public class HomeActivity extends Activity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private ActivityHomeBinding binding;

    private UserManager userManager;

    private MMKV mmkv;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e(TAG, "onCreate()");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        userManager = UserManager.getInstance();

        uiGoInitializingLayout();

        if(NetWorkUtils.isNetwork(this)){
            uiGoLoginLayout();
        }else {
            Toast.makeText(this, "未检测到网络, 请连接网络后重试...", Toast.LENGTH_SHORT).show();
            Thread gonnaFinishThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep( 3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            };gonnaFinishThread.start();
        }

        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCallSomebody();
            }
        });


        requestPermissions(new String[]{"android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MODIFY_AUDIO_SETTINGS"}, 0);

        mmkv = MMKV.defaultMMKV();
        String name = mmkv.getString(CustomKeys.KEY_USER_NAME, "");
        assert name != null;
        if(name.length() > 0 && !TextUtils.isEmpty(name)){
            binding.et.setText(name);
        }else {
            binding.et.setText("");
            binding.et.setHint("Please Input ID");
        }

        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    private void doLogin(){
        if(!NetWorkUtils.isNetwork(HomeActivity.this)){
            Toast.makeText(HomeActivity.this, "未检测到网络, 请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }
        if(binding.et.getText().toString().contains("Please Input ID") || TextUtils.isEmpty(binding.et.getText().toString())){
            Toast.makeText(HomeActivity.this, "请输入有效ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if(binding.et.getText().toString().length() > 10){
            Toast.makeText(HomeActivity.this, "ID过长, 最大长度为10", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = binding.et.getText().toString();
        mmkv.putString(CustomKeys.KEY_USER_NAME, name);
        MIMCUser user = userManager.newMIMCUser(name);
        boolean result = user.login();
        if (result) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            uiGoNormalLayout();
        } else {
            Toast.makeText(this, "登录失败, 请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy()");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtil.e(TAG, "onBackPressed()");
    }

    private void getLogin() {
        String name = getAppCountText();
        if (isNull(name) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "ID无效", Toast.LENGTH_SHORT).show();
        } else {
            MIMCUser user = userManager.newMIMCUser(name);
            boolean result = user.login();
            if (result) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                uiGoNormalLayout();
            } else {
                Toast.makeText(this, "登录失败, 请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goCallSomebody() {
        String toWho = binding.etToWho.getText().toString();
        if (!TextUtils.isEmpty(toWho)) {
            if (UserManager.getInstance().getStatus() == MIMCConstant.OnlineStatus.ONLINE) {
                VoiceCallActivity.actionStartActivity(this, toWho);
            } else {
                Toast.makeText(this, "not even login, please login", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void uiGoLoginLayout() {
        binding.llLogin.setVisibility(View.VISIBLE);
        binding.llInitializing.setVisibility(View.GONE);
        binding.llNormal.setVisibility(View.GONE);
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
    }

    private String getAppCountText() {
        LogUtil.e(TAG, "getAppCountText(0 invoke()");
        String text = binding.et.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            LogUtil.e(TAG, "the app name is: " + text);
            return text;
        } else {
            return null;
        }
    }

    private boolean isNull(Object x) {
        return x == null;
    }
}
