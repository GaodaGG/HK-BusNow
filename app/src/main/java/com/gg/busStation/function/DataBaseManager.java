package com.gg.busStation.function;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.database.DBOpenHelper;
import com.gg.busStation.data.database.SQLConstants;
import com.gg.busStation.function.location.LocationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseManager {
    private static SQLiteDatabase db;
    private static Context context;

    public static void initDB(Context context) {
        DataBaseManager.context = context;
        int version;
        try {
            version = (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context, "userdata.db", null, version);
        db = dbOpenHelper.getWritableDatabase();
    }

    public static void initData(List<Route> routes, List<Stop> stops) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, "正在更新数据", Toast.LENGTH_SHORT).show());

        //清除老数据
        db.delete(SQLConstants.routeDBName, null, null);
        db.delete(SQLConstants.stopDBName, null, null);

        db.beginTransaction();
        try {
            //更新路线数据
            if (routes != null) {
                insertRoutes(routes);
            }

            //更新站点数据
            if (stops != null) {
                insertStops(stops);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }


        updateTime();
    }

    private static void insertRoutes(List<Route> routes) {
        ContentValues contentValues = new ContentValues();
        for (Route route : routes) {
            contentValues.put("co", route.getCo());
            contentValues.put("route", route.getRoute());
            contentValues.put("bound", route.getBound());
            contentValues.put("service_type", route.getService_type());
            contentValues.put("orig_en", route.getOrig("en"));
            contentValues.put("orig_tc", route.getOrig("zh_HK"));
            contentValues.put("orig_sc", route.getOrig("zh_CN"));
            contentValues.put("dest_en", route.getDest("en"));
            contentValues.put("dest_tc", route.getDest("zh_HK"));
            contentValues.put("dest_sc", route.getDest("zh_CN"));

            db.insert(SQLConstants.routeDBName, null, contentValues);
            contentValues.clear();
        }
    }

    private static void insertStops(List<Stop> stops) {
        ContentValues contentValues = new ContentValues();
        for (Stop stop : stops) {
            contentValues.put("stop", stop.getStop());
            contentValues.put("name_en", stop.getName("en"));
            contentValues.put("name_tc", stop.getName("zh_HK"));
            contentValues.put("name_sc", stop.getName("zh_CN"));
            contentValues.put("lat", stop.getLat());
            contentValues.put("long", stop.getLong());

            db.insert(SQLConstants.stopDBName, null, contentValues);
            contentValues.clear();
        }
    }

    private static void updateTime() {
        //默认更新data时间
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", "lastUpdateTime");
        contentValues.put("value", String.valueOf(System.currentTimeMillis()));
        db.update(SQLConstants.settingsDBName, contentValues, "key=?", new String[]{"lastUpdateTime"});


        contentValues.clear();
        contentValues.put("key", "isInit");
        contentValues.put("value", String.valueOf(true));
        db.update(SQLConstants.settingsDBName, contentValues, "key=?", new String[]{"isInit"});
    }

    public static Map<String, String> getSettings() {
        if (db == null) {
            throw new NullPointerException("DataBase not Init");
        }

        Cursor cursor = db.query(SQLConstants.settingsDBName, null, null, null, null, null, null);
        Map<String, String> settingsMap = new HashMap<>();

        if (cursor.getCount() <= 0) {
            return null;
        }

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String key = cursor.getString(cursor.getColumnIndexOrThrow("key"));
            String value = cursor.getString(cursor.getColumnIndexOrThrow("value"));
            settingsMap.put(key, value);
            cursor.moveToNext();
        }

        cursor.close();

        return settingsMap;
    }

    @NonNull
    public static List<Route> getRoutes(String routeId) {
        String selection = "route LIKE ?";
        String[] selectionArgs = {routeId + "%"};
        Cursor cursor = db.query(SQLConstants.routeDBName, null, selection, selectionArgs, null, null, null);


        List<Route> routes = getRoutes(cursor);
        // 使用自然顺序对 routes 列表进行排序
        routes.sort((r1, r2) -> naturalOrderCompare(r1.getRoute(), r2.getRoute()));

        return routes;
    }

    private static int naturalOrderCompare(String routeA, String routeB) {
        int i = 0, j = 0;
        while (i < routeA.length() && j < routeB.length()) {
            char charA = routeA.charAt(i);
            char charB = routeB.charAt(j);

            // 比较数字部分
            if (Character.isDigit(charA) && Character.isDigit(charB)) {
                int startA = i, startB = j;

                // 提取数字部分
                while (i < routeA.length() && Character.isDigit(routeA.charAt(i))) i++;
                while (j < routeB.length() && Character.isDigit(routeB.charAt(j))) j++;

                // 转换为数字进行比较
                Integer numA = Integer.parseInt(routeA.substring(startA, i));
                Integer numB = Integer.parseInt(routeB.substring(startB, j));

                if (!numA.equals(numB)) {
                    return numA.compareTo(numB);
                }
            } else {
                // 比较字母部分
                if (charA != charB) {
                    return Character.compare(charA, charB);
                }
                i++;
                j++;
            }
        }

        // 比较剩余部分
        return routeA.length() - routeB.length();
    }

    @NonNull
    public static List<Route> getRoutes(int rows) {
        Cursor cursor;
        if (rows == 0) {
            //获取所有数据
            cursor = db.rawQuery("SELECT * FROM " + SQLConstants.routeDBName, null);
        } else {
            cursor = db.query(SQLConstants.routeDBName, null, null, null, null, null, null, String.valueOf(rows));
        }

        List<Route> routes = getRoutes(cursor);

        // 使用自然顺序对 routes 列表进行排序
        routes.sort((r1, r2) -> naturalOrderCompare(r1.getRoute(), r2.getRoute()));

        return routes;
    }

    private static List<Route> getRoutes(Cursor cursor) {
        List<Route> routes = new ArrayList<>();
        if (cursor.getCount() < 1) {
            cursor.close();
            return routes;
        }
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            Route route = new Route(
                    cursor.getString(cursor.getColumnIndexOrThrow("co")),
                    cursor.getString(cursor.getColumnIndexOrThrow("route")),
                    cursor.getString(cursor.getColumnIndexOrThrow("bound")),
                    cursor.getString(cursor.getColumnIndexOrThrow("service_type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("orig_en")),
                    cursor.getString(cursor.getColumnIndexOrThrow("orig_tc")),
                    cursor.getString(cursor.getColumnIndexOrThrow("orig_sc")),
                    cursor.getString(cursor.getColumnIndexOrThrow("dest_en")),
                    cursor.getString(cursor.getColumnIndexOrThrow("dest_tc")),
                    cursor.getString(cursor.getColumnIndexOrThrow("dest_sc"))
            );

            routes.add(route);
            cursor.moveToNext();
        }

        cursor.close();

        return routes;
    }

    public static List<Stop> getStops(int rows, int maxDistance) {

        Cursor cursor;
        LatLng location = null;
        if (rows == 0) {
            //获取所有数据
            cursor = db.rawQuery("SELECT * FROM " + SQLConstants.stopDBName, null);
            location = LocationHelper.getLocation();
        } else {
            cursor = db.query(SQLConstants.stopDBName, null, null, null, null, null, null, String.valueOf(rows));
        }

        List<Stop> stops = new ArrayList<>();
        if (cursor.getCount() < 1) {
            cursor.close();
            return stops;
        }
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            String lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"));
            String lon = cursor.getString(cursor.getColumnIndexOrThrow("long"));

            if (rows == 0) {
                LatLng stopLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                double distance = LocationHelper.distance(location, stopLocation);

                if (distance > maxDistance) {
                    cursor.moveToNext();
                    continue;
                }
            }

            Stop stop = new Stop(
                    cursor.getString(cursor.getColumnIndexOrThrow("stop")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_en")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_tc")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_sc")),
                    lat,
                    lon
            );

            stops.add(stop);
            cursor.moveToNext();
        }

        cursor.close();

        return stops;
    }


    @Nullable
    public static Route findRoute(String routeId, String bound, String service_type) {
        String selection = "route = ? AND bound = ? AND service_type = ?";
        String[] selectionArgs = {routeId, bound, service_type};
        Cursor cursor = db.query(SQLConstants.routeDBName, null, selection, selectionArgs, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Route route = new Route(
                cursor.getString(cursor.getColumnIndexOrThrow("co")),
                cursor.getString(cursor.getColumnIndexOrThrow("route")),
                cursor.getString(cursor.getColumnIndexOrThrow("bound")),
                cursor.getString(cursor.getColumnIndexOrThrow("service_type")),
                cursor.getString(cursor.getColumnIndexOrThrow("orig_en")),
                cursor.getString(cursor.getColumnIndexOrThrow("orig_tc")),
                cursor.getString(cursor.getColumnIndexOrThrow("orig_sc")),
                cursor.getString(cursor.getColumnIndexOrThrow("dest_en")),
                cursor.getString(cursor.getColumnIndexOrThrow("dest_tc")),
                cursor.getString(cursor.getColumnIndexOrThrow("dest_sc"))
        );

        cursor.close();
        return route;
    }

    public static Stop findStop(String stopId) {
        String selection = "stop = ?";
        String[] selectionArgs = {stopId};
        Cursor cursor = db.query(SQLConstants.stopDBName, null, selection, selectionArgs, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        Stop stop = new Stop(cursor.getString(cursor.getColumnIndexOrThrow("stop")),
                cursor.getString(cursor.getColumnIndexOrThrow("name_en")),
                cursor.getString(cursor.getColumnIndexOrThrow("name_tc")),
                cursor.getString(cursor.getColumnIndexOrThrow("name_sc")),
                cursor.getString(cursor.getColumnIndexOrThrow("lat")),
                cursor.getString(cursor.getColumnIndexOrThrow("long"))
        );

        cursor.close();
        return stop;
    }
}
