package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.database.SQLConstants;

import java.util.Collections;
import java.util.List;

public class StopDAOImpl implements StopDAO {
    private final SQLiteDatabase db;

    public StopDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public boolean exists(int stopId) {
        Cursor cursor = db.query(
                SQLConstants.stopDBName,
                new String[]{"id"},
                "id = ?",
                new String[]{String.valueOf(stopId)},
                null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @Override
    public void insert(Stop stop) {
        ContentValues values = stopToConvert(stop);
        db.insert(SQLConstants.stopDBName, null, values);
    }

    @Override
    public Stop getStop(int stopId) {
        Cursor cursor = db.query(SQLConstants.stopDBName, null, "id = ?",
                new String[]{String.valueOf(stopId)}, null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Stop stop = convertToStop(cursor);
        cursor.close();
        return stop;
    }

    private Stop convertToStop(Cursor cursor) {
        return new Stop(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nameE")),
                cursor.getString(cursor.getColumnIndexOrThrow("nameC")),
                cursor.getString(cursor.getColumnIndexOrThrow("nameS")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("long"))
        );
    }

    private static String splitAndGetFirstLine(String input) {
        if (input == null) return "";
        String[] parts = input.split("/<br>", 2);
        return parts[0].trim(); // 去除首尾空格
    }

    private static ContentValues stopToConvert(Stop stop) {
        ContentValues values = new ContentValues();
        values.put("id", stop.id());
        values.put("nameC", splitAndGetFirstLine(stop.nameC()));
        values.put("nameS", splitAndGetFirstLine(stop.nameS()));
        values.put("nameE", splitAndGetFirstLine(stop.nameE()));
        values.put("lat", stop.lat());
        values.put("long", stop.lon());
        return values;
    }
}
