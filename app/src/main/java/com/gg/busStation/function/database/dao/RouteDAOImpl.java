package com.gg.busStation.function.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.database.SQLConstants;

import java.util.ArrayList;
import java.util.List;

public class RouteDAOImpl implements RouteDAO {
    private final SQLiteDatabase db;

    public RouteDAOImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void insert(Route route) {
        ContentValues contentValues = routeToConvert(route);
        db.insertWithOnConflict(SQLConstants.routeDBName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public boolean exists(int routeId, int routeSeq) {
        Cursor cursor = db.query(SQLConstants.routeDBName,
                new String[]{"routeId", "routeSeq"},
                "routeId = ? AND routeSeq = ?",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)},
                null, null, null);

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public List<Route> getRoutes(int routeId, int routeSeq) {
        Cursor cursor = db.query(SQLConstants.routeDBName, null, "routeId = ? AND routeSeq = ?",
                new String[]{String.valueOf(routeId), String.valueOf(routeSeq)}, null, null, "stopSeq ASC");

        List<Route> routes = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            return routes;
        }

        do {
            Route route = convertToRoute(cursor);
            routes.add(route);
        } while (cursor.moveToNext());

        return routes;
    }

    @Override
    public List<Integer> getRouteSeq(int routeId) {
        Cursor cursor = db.query(SQLConstants.routeDBName, new String[]{"routeSeq"}, "routeId = ?",
                new String[]{String.valueOf(routeId)}, "routeSeq", null, null
        );

        List<Integer> routeSeqList = new ArrayList<>();
        if (!cursor.moveToFirst()) {
            cursor.close();
            return routeSeqList;
        }

        int idxRouteSeq = cursor.getColumnIndexOrThrow("routeSeq");
        do {
            int routeSeq = cursor.getInt(idxRouteSeq);
            routeSeqList.add(routeSeq);
        } while (cursor.moveToNext());

        cursor.close();
        return routeSeqList;
    }

    @Override
    public java.util.Map<Integer, List<Integer>> getRouteSeqs(List<Integer> routeIds) {
        java.util.Map<Integer, List<Integer>> result = new java.util.HashMap<>();
        if (routeIds.isEmpty()) {
            return result;
        }

        final int chunkSize = 999;
        for (int i = 0; i < routeIds.size(); i += chunkSize) {
            List<Integer> chunk = routeIds.subList(i, Math.min(i + chunkSize, routeIds.size()));
            StringBuilder placeholders = new StringBuilder();
            String[] args = new String[chunk.size()];
            for (int j = 0; j < chunk.size(); j++) {
                args[j] = String.valueOf(chunk.get(j));
                if (j > 0) placeholders.append(",");
                placeholders.append("?");
            }

            Cursor cursor = db.query(SQLConstants.routeDBName, new String[]{"routeId", "routeSeq"},
                    "routeId IN (" + placeholders + ")",
                    args, null, null, null);

            if (cursor.moveToFirst()) {
                int idxRouteId = cursor.getColumnIndexOrThrow("routeId");
                int idxRouteSeq = cursor.getColumnIndexOrThrow("routeSeq");
                do {
                    int routeId = cursor.getInt(idxRouteId);
                    int routeSeq = cursor.getInt(idxRouteSeq);
                    result.computeIfAbsent(routeId, k -> new ArrayList<>()).add(routeSeq);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return result;
    }

    @NonNull
    private static Route convertToRoute(Cursor cursor) {
        int idxRouteId = cursor.getColumnIndexOrThrow("routeId");
        int idxRouteSeq = cursor.getColumnIndexOrThrow("routeSeq");
        int idxStopSeq = cursor.getColumnIndexOrThrow("stopSeq");
        int idxStopPickDrop = cursor.getColumnIndexOrThrow("stopPickDrop");
        int idxStopId = cursor.getColumnIndexOrThrow("stopId");

        return new Route(
                cursor.getInt(idxRouteId),
                cursor.getInt(idxRouteSeq),
                cursor.getInt(idxStopSeq),
                cursor.getInt(idxStopPickDrop),
                cursor.getInt(idxStopId)
        );
    }

    private static ContentValues routeToConvert(Route route) {
        ContentValues values = new ContentValues();
        values.put("routeId", route.id());
        values.put("routeSeq", route.routeSeq());
        values.put("stopSeq", route.stopSeq());
        values.put("stopPickDrop", route.stopPickDrop());
        values.put("stopId", route.stopId());
        return values;
    }
}
