package com.xiaomi.mimcdemo.utils;

public class ViewUtil {

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 1000) {
            lastClickTime = time;
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
