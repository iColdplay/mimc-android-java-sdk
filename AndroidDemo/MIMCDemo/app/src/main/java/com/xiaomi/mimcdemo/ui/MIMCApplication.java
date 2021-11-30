package com.xiaomi.mimcdemo.ui;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mmkv.MMKV;
import com.xiaomi.mimc.logger.Logger;
import com.xiaomi.mimc.logger.MIMCLog;
import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.database.DBHelper;
import com.xiaomi.mimcdemo.utils.LogUtil;
import com.xiaomi.mimcdemo.utils.SystemPropertiesUtil;

import org.xml.sax.ext.LexicalHandler;

import java.util.ArrayList;
import java.util.List;


public class MIMCApplication extends Application {

    private static final String TAG = MIMCApplication.class.getSimpleName();

    private static Context context;

    private static MIMCApplication instance;

    public static MIMCApplication getInstance() {
        return instance;
    }

    private DBHelper mHelper;
    private SQLiteDatabase mDatabase;

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

        // database
        mHelper = new DBHelper(this);
        mDatabase = mHelper.getWritableDatabase();

        instance = this;

        LogUtil.e(TAG, "Device SN is " + getSerial());
    }

    public static Context getContext() {
        return context;
    }

    public void exit(){
        System.exit(0);
    }

    public boolean insertData(String customName, String sn){
        if(customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)){
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna insert data, customName: " + customName + " sn: " + sn);
        ContentValues values = new ContentValues();
        values.put(DBHelper.CUSTOM_NAME, customName);
        values.put(DBHelper.SN, sn);
        long index = mDatabase.insert(DBHelper.TABLE_NAME, null, values);
        if(index == -1){
            LogUtil.e(TAG, "insertData failed!!!");
            return false;
        }else {
            LogUtil.e(TAG, "insertData success!!!");
            return true;
        }
    }

    public boolean deleteDataByCustomName(String customName) {
        if (customName == null || TextUtils.isEmpty(customName)) {
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna delete data, customName: " + customName);

        int count = mDatabase.delete(DBHelper.TABLE_NAME, DBHelper.CUSTOM_NAME + " = ?", new String[]{customName});
        LogUtil.e(TAG, "delete result: " + count);
        return true;
    }

    public boolean deleteDataBySN(String sn){
        if (sn == null || TextUtils.isEmpty(sn)) {
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna delete data, sn: " + sn);

        int count = mDatabase.delete(DBHelper.TABLE_NAME, DBHelper.SN + " = ?", new String[]{sn});
        LogUtil.e(TAG, "delete result: " + count);
        return true;
    }

    public boolean updateData(String customName, String sn){
        if(customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)){
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna update data, customName: " + customName + " sn: " + sn);
        ContentValues values = new ContentValues();
        values.put(DBHelper.CUSTOM_NAME, customName);
        values.put(DBHelper.SN, sn);
        int count = mDatabase.update(DBHelper.TABLE_NAME, values, DBHelper.CUSTOM_NAME + " = ?", new String[]{customName});
        LogUtil.e(TAG, "update result: " + count);
        return true;

    }

    public List<Contact> queryData(){
        List<Contact> list = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("select * from table_person", null);
        if(cursor.moveToFirst()){
            do {
                String customName = cursor.getString(cursor.getColumnIndex(DBHelper.CUSTOM_NAME));
                LogUtil.e(TAG, customName);
                String sn = cursor.getString(cursor.getColumnIndex(DBHelper.SN));
                LogUtil.e(TAG, sn);
                Contact contact = new Contact(customName, sn);
                list.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if(list.size() == 0){
            LogUtil.e(TAG, "queryData but nothing found");
            return null;
        }
        return list;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getSerial() {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                return Build.getSerial();
//            }
//            return SystemPropertiesUtil.get("ro.serialno");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
