package com.xiaomi.mimcdemo.utils;

import android.app.Application;

import java.lang.reflect.Method;

public class AppUtil {
    public static Application getContext() {
        try {
            // 得到当前的ActivityThread对象
            Class<?> atCls = Class.forName("android.app.ActivityThread");
            Method method = atCls.getDeclaredMethod("currentActivityThread");
            method.setAccessible(true);
            Object atObject = method.invoke(null);
            //获取Application对象
            Method method2 = atCls.getDeclaredMethod("getApplication");
            method2.setAccessible(true);

            atCls.getDeclaredFields();
            return (Application) method2.invoke(atObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
