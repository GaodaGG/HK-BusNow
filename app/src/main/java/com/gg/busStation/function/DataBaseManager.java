package com.gg.busStation.function;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.database.DBOpenHelper;
import com.gg.busStation.data.database.SQLConstants;
import com.gg.busStation.function.location.LocationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataBaseManager {
    private static SQLiteDatabase db;

    private DataBaseManager() {
    }

    public static void initDB(Context context) {
        int version;
        try {
            version = (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            version = 0;
        }
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context, "userdata.db", null, version);
        db = dbOpenHelper.getWritableDatabase();
    }

    public static void initData(List<Route> routes, List<Stop> stops, Map<String, String> fares) {
        //清除老数据
        db.delete(SQLConstants.routeDBName, null, null);
        db.delete(SQLConstants.stopDBName, null, null);

        db.beginTransaction();
        try {
            //更新路线数据
            if (routes != null) {
                insertRoutes(routes);
                routes.clear();
            }

            //更新站点数据
            if (stops != null) {
                insertStops(stops);
                stops.clear();
            }

            //更新票价数据
            if (!fares.isEmpty()) {
                ContentValues contentValues = new ContentValues();
                fares.forEach((key, value) -> {
                    String[] split = key.split("_");
                    String route = split[0];
                    String bound = split[1];
                    contentValues.put("route", route);
                    contentValues.put("bound", bound);
                    contentValues.put("service_type", "1");
                    contentValues.put("fare", value);
                    db.insert(SQLConstants.fareDBName, null, contentValues);
                    contentValues.clear();
                });
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        updateTime();
    }

    private static void insertRoutes(List<Route> routes) {
        routes.sort(DataBaseManager::naturalOrderCompare);
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
    }

    public static void updateSetting(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", key);
        contentValues.put("value", value);
        db.update(SQLConstants.settingsDBName, contentValues, "key=?", new String[]{key});
    }

    public static Map<String, String> getSettings() {
        if (db == null) {
            throw new NullPointerException("DataBase not Init");
        }

        Cursor cursor = db.query(SQLConstants.settingsDBName, null, null, null, null, null, null);
        Map<String, String> settingsMap = new HashMap<>();

        if (cursor.getCount() <= 0) {
            return settingsMap;
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
        String selection = "route LIKE ? AND service_type = 1";
        String[] selectionArgs = {routeId + "%"};
        Cursor cursor = db.query(SQLConstants.routeDBName, null, selection, selectionArgs, null, null, null);

        List<Route> routes = getRoutes(cursor);
        cursor.close();
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
            location = LocationHelper.getLocation(false);
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
    public static Route findRoute(String co, String routeId, String bound, String service_type) {
        String selection = "route = ? AND co = ? AND bound = ? AND service_type = ?";
        String[] selectionArgs = {routeId, co, bound, service_type};
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

    public static List<String> getRouteNthCharacters(String filterStr, int index) {
        Set<String> charactersSet = new HashSet<>();
        String query = "SELECT Route FROM " + SQLConstants.routeDBName;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor == null) {
            return new ArrayList<>();
        }

        while (cursor.moveToNext()) {
            String route = cursor.getString(0);
            if (route.length() < index) {
                continue;
            }

            String charAtN = String.valueOf(route.charAt(index - 1));
            if (index > 1) {
                if (route.startsWith(filterStr)) {
                    charactersSet.add(charAtN);
                }
                continue;
            }

            charactersSet.add(charAtN);
        }
        cursor.close();

        return new ArrayList<>(charactersSet);
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

    public static List<Stop> findStopsByLocation(LatLng currentLocation, int distance) {
        List<Stop> stops = new ArrayList<>();
        // 当前的经纬度
        Cursor cursor = db.rawQuery("SELECT * FROM " + SQLConstants.stopDBName, null);

        // 处理查询结果
        while (cursor.moveToNext()) {
            String lat = cursor.getString(cursor.getColumnIndexOrThrow("lat"));
            String aLong = cursor.getString(cursor.getColumnIndexOrThrow("long"));
            LatLng stopLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(aLong));
            double stopDistance = LocationHelper.distance(currentLocation, stopLocation);
            if (stopDistance >= distance) {
                continue;
            }
            Stop stop = new Stop(cursor.getString(cursor.getColumnIndexOrThrow("stop")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_en")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_tc")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name_sc")),
                    lat,
                    aLong
            );
            stops.add(stop);
        }
        cursor.close();

        return stops;
    }

    public static String findFare(String route, String bound) {
        String selection = "route = ? AND bound = ? AND service_type = ?";
        String[] selectionArgs = {route, bound, "1"};
        Cursor cursor = db.query(SQLConstants.fareDBName, null, selection, selectionArgs, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String fare = cursor.getString(cursor.getColumnIndexOrThrow("fare"));
        cursor.close();
        return fare;
    }

    public static void addRoutesHistory(String co, String routeId, String bound, String service_type) {
        long timestamp = 0;
        if (isPinRoutesHistory(co, routeId, bound, service_type)) {
            timestamp = System.currentTimeMillis();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("co", co);
        contentValues.put("route", routeId);
        contentValues.put("bound", bound);
        contentValues.put("service_type", service_type);
        contentValues.put("timestamp", timestamp != 0 ? null : System.currentTimeMillis());

        db.insertWithOnConflict(SQLConstants.routesHistoryDBName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static List<Route> getRoutesHistory() {
        Cursor cursor = db.query(SQLConstants.routesHistoryDBName, null, null, null, null, null, "timestamp DESC");
        List<Route> routes = new ArrayList<>();
        if (cursor.getCount() < 1) {
            cursor.close();
            return routes;
        }
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            Route route = findRoute(
                    cursor.getString(cursor.getColumnIndexOrThrow("co")),
                    cursor.getString(cursor.getColumnIndexOrThrow("route")),
                    cursor.getString(cursor.getColumnIndexOrThrow("bound")),
                    cursor.getString(cursor.getColumnIndexOrThrow("service_type"))
            );

            routes.add(route);
            cursor.moveToNext();
        }

        cursor.close();

        return routes;
    }

    public static boolean deleteRoutesHistory(String co, String routeId, String bound, String service_type) {
        int delete = db.delete(SQLConstants.routesHistoryDBName, "co = ? AND route = ? AND bound = ? AND service_type = ?", new String[]{co, routeId, bound, service_type});
        return delete > 0;
    }

    public static void pinRoutesHistory(String co, String routeId, String bound, String service_type) {
        int count = 0;
        Cursor cursor = db.query(SQLConstants.routesHistoryDBName, null, null, null, null, null, "timestamp DESC");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                if (timestamp > 9000000000000L) {
                    count++;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();


        ContentValues contentValues = new ContentValues();
        contentValues.put("co", co);
        contentValues.put("route", routeId);
        contentValues.put("bound", bound);
        contentValues.put("service_type", service_type);
        contentValues.put("timestamp", 9000000000000L + count);
        db.insertWithOnConflict(SQLConstants.routesHistoryDBName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void unpinRoutesHistory(String co, String routeId, String bound, String service_type) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("co", co);
        contentValues.put("route", routeId);
        contentValues.put("bound", bound);
        contentValues.put("service_type", service_type);
        contentValues.put("timestamp", System.currentTimeMillis());
        db.insertWithOnConflict(SQLConstants.routesHistoryDBName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static boolean isPinRoutesHistory(String co, String routeId, String bound, String service_type) {
        String selection = "co = ? AND route = ? AND bound = ? AND service_type = ? AND timestamp >= 9000000000000";
        String[] selectionArgs = {co, routeId, bound, service_type};
        Cursor cursor = db.query(SQLConstants.routesHistoryDBName, null, selection, selectionArgs, null, null, null);
        boolean isPin = cursor.getCount() > 0;
        cursor.close();
        return isPin;
    }

    private static int naturalOrderCompare(Route routeA, Route routeB) {
        int i = 0, j = 0;
        String routeAId = routeA.getRoute();
        String routeBId = routeB.getRoute();
        while (i < routeAId.length() && j < routeBId.length()) {
            char charA = routeAId.charAt(i);
            char charB = routeBId.charAt(j);

            // 比较数字部分
            if (Character.isDigit(charA) && Character.isDigit(charB)) {
                int startA = i, startB = j;

                // 提取数字部分
                while (i < routeAId.length() && Character.isDigit(routeAId.charAt(i))) i++;
                while (j < routeBId.length() && Character.isDigit(routeBId.charAt(j))) j++;

                // 转换为数字进行比较
                Integer numA = Integer.parseInt(routeAId.substring(startA, i));
                Integer numB = Integer.parseInt(routeBId.substring(startB, j));

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
        return routeAId.length() - routeBId.length();
    }
}
