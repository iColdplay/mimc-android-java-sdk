package com.xiaomi.mimcdemo.utils;

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public volatile static int LEVEL = VERBOSE;

    public static void setLevel(int Level) {
        LEVEL = Level;
    }

    public static void closeLog(){
        LEVEL = Integer.MAX_VALUE;
    }

    public static void v(String TAG, String msg) {
        if (LEVEL <= VERBOSE && !TextUtils.isEmpty(msg)) {
            log(VERBOSE, TAG, msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (LEVEL <= DEBUG && !TextUtils.isEmpty(msg)) {
            log(DEBUG, TAG, msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (LEVEL <= INFO && !TextUtils.isEmpty(msg)) {
            log(INFO, TAG, msg);
        }
    }

    public static void w(String TAG, String msg) {
        if (LEVEL <= WARN && !TextUtils.isEmpty(msg)) {
            log(WARN, TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (LEVEL <= ERROR && !TextUtils.isEmpty(msg)) {
            log(ERROR, TAG, msg);
        }
    }

    private static void log(int type, String TAG, String msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        int index = 4;
        String className = stackTrace[index].getFileName();
        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();

        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        String logStr = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] " +
                msg;

        switch (type) {
            case VERBOSE:
                Log.v(TAG, logStr);
                break;
            case DEBUG:
                Log.d(TAG, logStr);
                break;
            case INFO:
                Log.i(TAG, logStr);
                break;
            case WARN:
                Log.w(TAG, logStr);
                break;
            case ERROR:
                Log.e(TAG, logStr);
                break;
            default:
                break;
        }

    }
}
