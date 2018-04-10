package com.applozic.mobicomkit.api.conversation.database.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SecureDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;


    public SecureDbHelper(Context context) {
        super(context, "secure_db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table schat\n" +
                "(id integer primary key autoincrement,\n" +
                " message text,\n" +
                " is_my boolean,\n" +
                " cend_date date, " +
                " mkey text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
