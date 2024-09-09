package com.gg.busStation.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!isTableExist(db, SQLConstants.settingsDBName)) {
            db.execSQL(SQLConstants.createSettingsDBCommand);

            //默认更新data时间
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", "lastUpdateTime");
            contentValues.put("value", String.valueOf(System.currentTimeMillis()));
            db.insert(SQLConstants.settingsDBName, null, contentValues);

            contentValues.clear();
            contentValues.put("key", "updateTime");
            contentValues.put("value", String.valueOf(1000*60*60*24*7));
            db.insert(SQLConstants.settingsDBName, null, contentValues);

            contentValues.clear();
            contentValues.put("key", "isInit");
            contentValues.put("value", String.valueOf(false));
            db.insert(SQLConstants.settingsDBName, null, contentValues);
        }

        if (!isTableExist(db, SQLConstants.routeDBName)) {
            db.execSQL(SQLConstants.createRouteDBCommand);
        }

        if (!isTableExist(db, SQLConstants.stopDBName)) {
            db.execSQL(SQLConstants.createStopDBCommand);
        }

        if (!isTableExist(db, SQLConstants.routesHistoryDBName)) {
            db.execSQL(SQLConstants.createRoutesHistoryDBCommand);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static boolean isTableExist(SQLiteDatabase db, String tableName) {
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        Cursor cursor = db.rawQuery(checkTableSQL, new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
