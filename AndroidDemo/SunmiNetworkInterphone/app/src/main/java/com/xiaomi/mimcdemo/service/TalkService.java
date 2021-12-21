package com.xiaomi.mimcdemo.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.manager.AudioEventManager;
import com.xiaomi.mimcdemo.ui.ContactAdapter;
import com.xiaomi.mimcdemo.ui.HomeActivity;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;

import java.util.Objects;

public class TalkService extends Service {

    private static final String TAG = TalkService.class.getSimpleName();

    private static final int FOREGROUND_CODE = 10000;

    public static final String ACTION_PTT_KEY_DOWN = "com.sunmi.ptt.key.down"; //侧键按下
    public static final String ACTION_PTT_KEY_UP = "com.sunmi.ptt.key.up"; //侧键松开

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                LogUtil.e(TAG, "TalkService receiver action: " + intent.getAction());

                if (!ContactAdapter.isAnythingInConnection) {
                    LogUtil.e(TAG, "未连接用户!");
                    Toast.makeText(AppUtil.getContext(), "请先连接一位用户", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.equals(intent.getAction(), ACTION_PTT_KEY_UP)) {
                    LogUtil.e(TAG, "TalkService receiver action KEY_UP");

                    if (AudioEventManager.getInstance().isPttSpeaking()) {
                        LogUtil.e(TAG, "speaking should stop now");
                        // todo stop speaking flow
                    } else {
                        LogUtil.e(TAG, "not even speaking");
                        Toast.makeText(TalkService.this, "Busy now, try it later", Toast.LENGTH_SHORT).show();
                    }

                    return;
                }

                if (Objects.equals(intent.getAction(), ACTION_PTT_KEY_DOWN)) {
                    LogUtil.e(TAG, "TalkService receiver action KEY_DOWN");
                    if (AudioEventManager.getInstance().isPttIdle()) {
                        LogUtil.e(TAG, "speaking should start now");
                        // todo start speaking flow
                    } else {
                        LogUtil.e(TAG, "not even idle, we won't do anything");
                        Toast.makeText(TalkService.this, "Busy now, try it later", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
    };

    public DataCallback getCallback() {
        return callback;
    }

    public void setCallback(DataCallback callback) {
        this.callback = callback;
    }

    public interface DataCallback {
        void onDataCallback(Bundle data);
    }

    private DataCallback callback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TalkServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e(TAG, "onCreate()");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PTT_KEY_DOWN);
        intentFilter.addAction(ACTION_PTT_KEY_UP);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e(TAG, "onStartCommand()");
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("foregroundService", "前台服务", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, "foregroundService");
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        Intent i = new Intent(this, HomeActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        builder.setContentTitle("网络对讲机")
                .setContentText("正在后台运行")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi);
        startForeground(FOREGROUND_CODE, builder.build());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy()");
        unregisterReceiver(receiver);
    }

    public class TalkServiceBinder extends Binder {
        public TalkService getService() {
            return TalkService.this;
        }
    }

}
