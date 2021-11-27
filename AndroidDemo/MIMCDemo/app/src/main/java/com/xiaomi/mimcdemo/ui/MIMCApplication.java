package com.xiaomi.mimcdemo.ui;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.logger.Logger;
import com.xiaomi.mimc.logger.MIMCLog;
import com.xiaomi.mimcdemo.utils.LogUtil;


public class MIMCApplication extends Application {

    private static final String TAG = MIMCApplication.class.getSimpleName();

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

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
    }

    public static Context getContext() {
        return context;
    }
}
