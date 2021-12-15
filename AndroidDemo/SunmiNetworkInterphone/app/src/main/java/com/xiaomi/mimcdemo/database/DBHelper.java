package com.xiaomi.mimcdemo.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = DBHelper.class.getSimpleName();

    public static final String DB_NAME = "database.db";

    public static final String TABLE_NAME = "table_person";

    public static final int DB_VERSION = 1;

    public static final String CUSTOM_NAME = "custom_name";
    public static final String SN = "sn";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        String sql = "create table " +
                TABLE_NAME +
                "(_id integer primary key autoincrement, " +
                CUSTOM_NAME + " varchar, " +
                SN + " varchar"
                + ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
