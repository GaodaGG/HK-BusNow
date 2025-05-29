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
        db.insert(SQLConstants.routeDBName, null, contentValues);
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

        do {
            int routeSeq = cursor.getInt(cursor.getColumnIndexOrThrow("routeSeq"));
            routeSeqList.add(routeSeq);
        } while (cursor.moveToNext());

        cursor.close();
        return routeSeqList;
    }

    @NonNull
    private static Route convertToRoute(Cursor cursor) {
        return new Route(
                cursor.getInt(cursor.getColumnIndexOrThrow("routeId")),
                cursor.getInt(cursor.getColumnIndexOrThrow("routeSeq")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stopSeq")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stopPickDrop")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stopId"))
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
