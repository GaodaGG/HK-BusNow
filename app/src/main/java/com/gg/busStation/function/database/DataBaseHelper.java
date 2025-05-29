package com.gg.busStation.function.database;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.database.DBOpenHelper;

public class DataBaseHelper {
    private final SQLiteDatabase db;
    private static DataBaseHelper instance;

    private DataBaseHelper(Context context) {
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context, "userdata.db", null, getVersion(context));
        db = dbOpenHelper.getWritableDatabase();
    }

    private static int getVersion(Context context) {
        int version;
        try {
            version = (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            version = 0;
        }
        return version;
    }

    public static synchronized DataBaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void executeTransaction(Runnable transaction) {
        db.beginTransaction();
        try {
            transaction.run(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public interface Runnable {
        void run(SQLiteDatabase db);
    }
}
