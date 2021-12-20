package com.xiaomi.mimcdemo.ui;


import android.annotation.SuppressLint;
import android.app.Application;

import android.content.Context;

import android.os.Vibrator;
import android.provider.Settings;

import android.util.Log;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.logger.Logger;
import com.xiaomi.mimc.logger.MIMCLog;
import com.xiaomi.mimcdemo.manager.AudioEventManager;
import com.xiaomi.mimcdemo.manager.ContactManager;
import com.xiaomi.mimcdemo.manager.SDKUserBehaviorManager;
import com.xiaomi.mimcdemo.manager.SunmiMissCallManager;
import com.xiaomi.mimcdemo.utils.LogUtil;


public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();

    private static Context context;

    private static MainApplication instance;

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();

        MIMCLog.setLogger(new Logger() {
            @Override
            public void d(String tag, String msg) {
                Log.d(tag, msg);
            }

            @Override
            public void d(String tag, String msg, Throwable th) {
                Log.d(tag, msg, th);
            }

            @Override
            public void i(String tag, String msg) {
                Log.i(tag, msg);
            }

            @Override
            public void i(String tag, String msg, Throwable th) {
                Log.i(tag, msg, th);
            }

            @Override
            public void w(String tag, String msg) {
                Log.w(tag, msg);
            }

            @Override
            public void w(String tag, String msg, Throwable th) {
                Log.w(tag, msg, th);
            }

            @Override
            public void e(String tag, String msg) {
                Log.e(tag, msg);
            }

            @Override
            public void e(String tag, String msg, Throwable th) {
                Log.e(tag, msg, th);
            }
        });
        MIMCLog.setLogPrintLevel(MIMCLog.DEBUG);
        MIMCLog.setLogSaveLevel(MIMCLog.DEBUG);

        // mmkv
        String rootDir = MMKV.initialize(this);
        LogUtil.e(TAG, "rootDir is: " + rootDir);

        // user login
        boolean ret = SDKUserBehaviorManager.getInstance().loginSDKUser();
        if (!ret) {
            LogUtil.e(TAG, "登录失败, 请检查网络...");
            Toast.makeText(this, "请检查网络后重试...", Toast.LENGTH_SHORT).show();
        }
        LogUtil.e(TAG, "Device SN is " + getSerial());

        // AudioEventManager 初始化
        AudioEventManager.getInstance();

        // SunmiMissCallManager 初始化
        SunmiMissCallManager.getInstance();

        // ContactManager 初始化
        ContactManager.getInstance();
    }

    public static Context getContext() {
        return context;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getSerial() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

}
