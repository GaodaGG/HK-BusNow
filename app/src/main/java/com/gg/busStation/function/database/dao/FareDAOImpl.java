package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.database.SQLConstants;

public class FareDAOImpl implements FareDAO {
    private final SQLiteDatabase db;

    public FareDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public boolean exists(int routeId, int routeSeq) {
        Cursor cursor = db.query(SQLConstants.fareDBName,
                new String[]{"routeId", "routeSeq"},
                "routeId = ? AND routeSeq = ?",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)},
                null, null, null);

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public void insert(int routeId, int routeSeq, String fare) {
        ContentValues values = fareToConvert(routeId, routeSeq, fare);
        db.insert(SQLConstants.fareDBName, null, values);
    }

    @Override
    public String getFare(int routeId, int routeSeq) {
        Cursor cursor = db.query(
                SQLConstants.fareDBName,
                new String[]{"fare"},
                "routeId = ? AND routeSeq = ?",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)},
                null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String fare = cursor.getString(0);
        cursor.close();
        return fare;
    }

    private static ContentValues fareToConvert(int routeId, int routeSeq, String fare) {
        ContentValues values = new ContentValues();
        values.put("routeId", Integer.valueOf(routeId));
        values.put("routeSeq", routeSeq);
        values.put("fare", fare);
        return values;
    }
}
