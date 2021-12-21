package com.xiaomi.mimcdemo.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xiaomi.mimcdemo.database.Contact;
import com.xiaomi.mimcdemo.database.DBHelper;
import com.xiaomi.mimcdemo.utils.AppUtil;
import com.xiaomi.mimcdemo.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;


/***
 * 联系人管理
 */
public class ContactManager {

    private static final String TAG = ContactManager.class.getSimpleName();

    private static ContactManager contactManager;

    private ContactManager() {
        // database
        mHelper = new DBHelper(AppUtil.getContext());
        mDatabase = mHelper.getWritableDatabase();
    }

    public synchronized static ContactManager getInstance() {
        if (contactManager == null) {
            contactManager = new ContactManager();
        }
        return contactManager;
    }

    private DBHelper mHelper;
    private SQLiteDatabase mDatabase;

    public boolean insertData(String customName, String sn) {
        if (customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)) {
            LogUtil.e(TAG, "param error");
            return false;
        }

        List<Contact> currentAllContact = queryData();
        boolean matchAny = false;
        if (currentAllContact != null && currentAllContact.size() > 0) {
            for (Contact contact : currentAllContact) {
                if (contact.getSn().equals(sn) && contact.getCustomName().equals(customName)) {
                    matchAny = true;
                    break;
                }
            }
        }
        if (matchAny) {
            LogUtil.e(TAG, "contact already in, no more need to add");
            return true;
        }

        LogUtil.e(TAG, "gonna insert data, customName: " + customName + " sn: " + sn);
        ContentValues values = new ContentValues();
        values.put(DBHelper.CUSTOM_NAME, customName);
        values.put(DBHelper.SN, sn);
        values.put(DBHelper.MISS_CALL, 0);
        long index = mDatabase.insert(DBHelper.TABLE_NAME, null, values);
        if (index == -1) {
            LogUtil.e(TAG, "insertData failed!!!");
            return false;
        } else {
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

    public boolean deleteDataBySN(String sn) {
        if (sn == null || TextUtils.isEmpty(sn)) {
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna delete data, sn: " + sn);

        int count = mDatabase.delete(DBHelper.TABLE_NAME, DBHelper.SN + " = ?", new String[]{sn});
        LogUtil.e(TAG, "delete result: " + count);
        return true;
    }

    public boolean updateData(String customName, String sn) {
        if (customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)) {
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

    public boolean updateDataByMissCallHappened(String customName, String sn) {
        if (customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)) {
            LogUtil.e(TAG, "param error");
            return false;
        }

        LogUtil.e(TAG, "gonna update data, customName: " + customName + " sn: " + sn);

        List<Contact> list = queryData();
        boolean everMatch = false;
        int currentMissCall = 0;
        if (list != null || list.size() > 0) {
            for (Contact contact : list){
                if(contact.getCustomName().equals(customName) && contact.getSn().equals(sn)){
                    LogUtil.e(TAG, "find target data");
                    currentMissCall = contact.getMissCall();
                    everMatch = true;
                    break;
                }
            }
        }
        if(!everMatch){
            LogUtil.e(TAG, "this miss call we don't need");
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(DBHelper.CUSTOM_NAME, customName);
        values.put(DBHelper.SN, sn);
        values.put(DBHelper.MISS_CALL, currentMissCall + 1);
        int count = mDatabase.update(DBHelper.TABLE_NAME, values, DBHelper.CUSTOM_NAME + " = ?", new String[]{customName});
        LogUtil.e(TAG, "update result: " + count);
        return true;
    }

    public boolean updateMissCallTo0(String customName, String sn){
        if (customName == null || TextUtils.isEmpty(customName) || sn == null || TextUtils.isEmpty(sn)) {
            LogUtil.e(TAG, "param error");
            return false;
        }
        LogUtil.e(TAG, "gonna update data, customName: " + customName + " sn: " + sn);

        List<Contact> list = queryData();
        boolean everMatch = false;
        if (list != null || list.size() > 0) {
            for (Contact contact : list){
                if(contact.getCustomName().equals(customName) && contact.getSn().equals(sn)){
                    LogUtil.e(TAG, "find target data");
                    everMatch = true;
                    break;
                }
            }
        }
        if(!everMatch){
            LogUtil.e(TAG, "this miss call we don't need");
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(DBHelper.CUSTOM_NAME, customName);
        values.put(DBHelper.SN, sn);
        values.put(DBHelper.MISS_CALL, 0);
        int count = mDatabase.update(DBHelper.TABLE_NAME, values, DBHelper.CUSTOM_NAME + " = ?", new String[]{customName});
        LogUtil.e(TAG, "update result: " + count);
        return true;
    }

    public List<Contact> queryData() {
        List<Contact> list = new ArrayList<>();
        Cursor cursor = mDatabase.rawQuery("select * from table_person", null);
        if (cursor.moveToFirst()) {
            do {
                String customName = cursor.getString(cursor.getColumnIndex(DBHelper.CUSTOM_NAME));
                LogUtil.e(TAG, customName);
                String sn = cursor.getString(cursor.getColumnIndex(DBHelper.SN));
                LogUtil.e(TAG, sn);
                int missCall = cursor.getInt(cursor.getColumnIndex(DBHelper.MISS_CALL));
                Contact contact = new Contact(customName, sn, missCall);
                list.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (list.size() == 0) {
            LogUtil.e(TAG, "queryData but nothing found");
            return null;
        }
        return list;
    }

}
