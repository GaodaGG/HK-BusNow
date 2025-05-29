package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.database.SQLConstants;

import java.util.ArrayList;
import java.util.List;

public class HistoryDAOImpl implements HistoryDAO {
    private SQLiteDatabase db;

    public HistoryDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void insert(int routeId, int routeSeq, boolean unPin) {
        ContentValues contentValues = getContentValues(routeId, routeSeq, unPin ? 0 : getPinnedIndex(routeId, routeSeq));
        db.insertWithOnConflict(SQLConstants.historyDBName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void delete(int routeId, int routeSeq) {
        db.delete(SQLConstants.historyDBName, "routeId = ? AND routeSeq = ?",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)});
    }

    @Override
    public List<Route> getAllHistory() {
        Cursor cursor = db.query(SQLConstants.historyDBName, null, null, null, null, null, "pinnedIndex DESC, timestamp DESC");
        List<Route> historyList = new ArrayList<>();

        if (!cursor.moveToFirst()) {
            cursor.close();
            return historyList;
        }

        do {
            int routeId = cursor.getInt(cursor.getColumnIndexOrThrow("routeId"));
            int routeSeq = cursor.getInt(cursor.getColumnIndexOrThrow("routeSeq"));
            historyList.add(new Route(routeId, routeSeq, 0, 0, 0));

        } while (cursor.moveToNext());

        cursor.close();
        return historyList;
    }

    @Override
    public void pinHistory(int routeId, int routeSeq) {
        int pinnedIndex = 0;

        Cursor cursor = db.query(SQLConstants.historyDBName,
                new String[]{"MAX(pinnedIndex)"},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            pinnedIndex = cursor.getInt(0) + 1;
        }

        cursor.close();

        ContentValues contentValues = getContentValues(routeId, routeSeq, pinnedIndex);
        db.insertWithOnConflict(SQLConstants.historyDBName, null,
                contentValues,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void unpinHistory(int routeId, int routeSeq) {
        insert(routeId, routeSeq, true);
    }

    @Override
    public int getPinnedIndex(int routeId, int routeSeq) {
        try (Cursor cursor = db.query(SQLConstants.historyDBName, new String[]{"pinnedIndex"},
                "routeId = ? AND routeSeq = ? AND pinnedIndex > 0",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)},
                null, null, null)) {

            if (!cursor.moveToFirst()) {
                return 0;
            }

            return cursor.getInt(cursor.getColumnIndexOrThrow("pinnedIndex"));
        }
    }

    @NonNull
    private static ContentValues getContentValues(int routeId, int routeSeq, int pinnedIndex) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("routeId", routeId);
        contentValues.put("routeSeq", routeSeq);
        contentValues.put("timestamp", System.currentTimeMillis());
        contentValues.put("pinnedIndex", pinnedIndex);
        return contentValues;
    }
}
