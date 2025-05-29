package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.gg.busStation.data.database.SQLConstants;

public class SettingsDAOImpl implements SettingsDAO {
    private final SQLiteDatabase db;

    public SettingsDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public boolean exists(String key) {
        Cursor cursor = db.query(SQLConstants.settingsDBName, null, "key = ?",
                new String[]{key}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public void insert(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        db.insert(SQLConstants.settingsDBName, null, values);
    }

    @Override
    @NonNull
    public String getValue(String key, @NonNull String defaultValue) {
        Cursor cursor = db.query(SQLConstants.settingsDBName, new String[]{"value"}, "key = ?",
                new String[]{key}, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return defaultValue;
        }

        String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
        cursor.close();
        return value;
    }
}
