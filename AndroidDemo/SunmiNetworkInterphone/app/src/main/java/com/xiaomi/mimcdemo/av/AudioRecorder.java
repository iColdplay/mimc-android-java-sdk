package com.xiaomi.mimcdemo.av;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.xiaomi.mimcdemo.listener.OnAudioCapturedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by houminjiang on 18-5-28.
 */

public class AudioRecorder implements Capture {
    private AudioCapture audioCapture;
    private Thread captureThread;
    private volatile boolean exit = false;
    private boolean isCaptureStarted = false;
    private OnAudioCapturedListener onAudioCapturedListener;
    private int MAX_BUFF_SIZE = 2 * 1024;
    private static final String TAG = "AudioRecorder";

    public void setOnAudioCapturedListener(OnAudioCapturedListener onAudioCapturedListener) {
        this.onAudioCapturedListener = onAudioCapturedListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean start() {
        if (isCaptureStarted) {
            Log.w(TAG, "Capture has already been started.");
            return false;
        }

        exit = false;
        audioCapture = new AudioCapture();
        boolean result = audioCapture.start();
        if (result) {
            captureThread = new Thread(new AudioCaptureRunnable());
            captureThread.start();
            isCaptureStarted = true;
        }

        return result;
    }

    @Override
    public void stop() {
        if (!isCaptureStarted) {
            return;
        }
        exit = true;
        try {
            captureThread.join(50);
            captureThread = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted exception:", e);
        }
        audioCapture.stop();
        isCaptureStarted = false;
    }

    public volatile static boolean shouldSavePCMData = true;

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {


            FileOutputStream fileOutputStream = null;
            if(shouldSavePCMData) {
                Log.e(TAG, "now we should save pcm data");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String origin = "/storage/emulated/0/Android/data/com.sunmi.interphone" + "/pcm/";
                File dir = new File(origin + sdf.format(new Date(System.currentTimeMillis())));
                if (!dir.exists() || !dir.isDirectory()) {
                    boolean mkdirRet = dir.mkdirs();
                    if (!mkdirRet) {
                        Log.e(TAG, "dir.mkdirs failed!!!");
                    }
                }
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_HH_mm_ss");// HH:mm:ss
                File pcmData = new File(dir, "pcmData" + simpleDateFormat.format(new Date(System.currentTimeMillis())));
                try {
                    boolean createNewFileRet = pcmData.createNewFile();
                    if (!createNewFileRet) {
                        Log.e(TAG, "createNewFile failed!!!");
                    }
                    fileOutputStream = new FileOutputStream(pcmData);
                    Log.e(TAG, "fileOutputStream is ready");
                } catch (IOException e) {
                    Log.e(TAG, "fileOutputStream not ready!!!");
                    e.printStackTrace();
                }
            }


            while (!exit) {
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                byte[] pcmData = new byte[MAX_BUFF_SIZE];
                int result = audioCapture.capture(pcmData, 0, MAX_BUFF_SIZE);
                if (result > 0) {

                    if(shouldSavePCMData) {
                        try {
                            fileOutputStream.write(pcmData, 0, result);
                            fileOutputStream.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (onAudioCapturedListener != null) {
                        onAudioCapturedListener.onAudioCaptured(pcmData);
                    }
                    //Log.d(TAG, String.format("Success captured " + result + "bytes. buffer size:%d", MAX_BUFF_SIZE));
                }
            }
            Log.i(TAG, "Audio capture thread exit.");

            // save pcm bytes into files
            if(shouldSavePCMData) {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
