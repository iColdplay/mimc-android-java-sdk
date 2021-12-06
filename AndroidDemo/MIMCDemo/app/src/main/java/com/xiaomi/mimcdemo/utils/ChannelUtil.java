package com.xiaomi.mimcdemo.utils;

public class ChannelUtil {

    private static final String TAG = ChannelUtil.class.getSimpleName();

    public static String CALL_ID = "";

    public static String CALL_KEY = "";

    public static String[] getChannelInfo() {
        if (CALL_ID.length() == 0 || CALL_KEY.length() == 0) {
            return null;
        }
        return new String[]{CALL_ID, CALL_KEY};
    }
}


