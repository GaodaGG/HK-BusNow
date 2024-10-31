package com.gg.busStation.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {
    private final String[] tableNames = {SQLConstants.routeDBName, SQLConstants.stopDBName, SQLConstants.routesHistoryDBName, SQLConstants.fareDBName};
    private final String[] tableCommands = {SQLConstants.createRouteDBCommand, SQLConstants.createStopDBCommand, SQLConstants.createRoutesHistoryDBCommand, SQLConstants.createFareDBCommand};

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static boolean isTableExist(SQLiteDatabase db, String tableName) {
        String checkTableSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        Cursor cursor = db.rawQuery(checkTableSQL, new String[]{tableName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!isTableExist(db, SQLConstants.settingsDBName)) {
            db.execSQL(SQLConstants.createSettingsDBCommand);

            String[] keys = {"lastUpdateTime", "updateTime", "isInit", "dontUpdate"};
            String[] values = {String.valueOf(System.currentTimeMillis()), String.valueOf(1000 * 60 * 60 * 24 * 7), String.valueOf(false), String.valueOf(false)};
            for (int i = 0; i < keys.length; i++) {
                insertSettings(db, keys[i], values[i]);
            }
        }

        for (int i = 0; i < tableNames.length; i++) {
            if (!isTableExist(db, tableNames[i])) {
                db.execSQL(tableCommands[i]);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 20241017) {
            insertSettings(db, "dontUpdate", String.valueOf(false));
        }
    }

    private void insertSettings(SQLiteDatabase db, String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", key);
        contentValues.put("value", value);
        db.insert(SQLConstants.settingsDBName, null, contentValues);
    }
}
