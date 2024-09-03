package com.gg.busStation.function;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.gg.busStation.function.location.LocationHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataManager {
    public static final String routeUrl = "https://data.etabus.gov.hk/v1/transport/kmb/route/";
    public static final String stopUrl = "https://data.etabus.gov.hk/v1/transport/kmb/stop/";
    public static final String routeToStopUrl = "https://data.etabus.gov.hk/v1/transport/kmb/route-stop/";
    public static final String routeAndStopToETAUrl = "https://data.etabus.gov.hk/v1/transport/kmb/eta/";

    private static long lastUpdateTime = 0;

    private DataManager() {
    }

    public static void initData() throws IOException {
        //判断是否需要更新数据
        Map<String, String> settings = DataBaseManager.getSettings();
        String oldLastUpdateTime = settings.get("lastUpdateTime");
        lastUpdateTime = Long.parseLong(oldLastUpdateTime);

        if (System.currentTimeMillis() <= lastUpdateTime + Long.parseLong(settings.get("updateTime")) && "true".equals(settings.get("isInit"))) {
            return;
        }

        List<Route> routeList = initRoutes();
        List<Stop> stopList = initStops();

        DataBaseManager.initData(routeList, stopList);
    }

    public static List<Route> initRoutes() throws IOException {
        List<Route> routes = new ArrayList<>();
        String data = HttpClientHelper.getData(routeUrl);
        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            routes.add(JsonToBean.jsonToRoute(jsonObject));
        }

        return routes;
    }

    public static List<Stop> initStops() throws IOException {
        List<Stop> stops = new ArrayList<>();
        String data = HttpClientHelper.getData(stopUrl);
        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            stops.add(JsonToBean.jsonToStop(jsonObject));
        }
        return stops;
    }

    public static List<ETA> routeAndStopToETAs(Route route,Stop stop) throws IOException {
        List<ETA> etas = new ArrayList<>();
        String url = routeAndStopToETAUrl + stop.getStop() + "/" + route.getRoute() + "/" + route.getService_type();
        String data = HttpClientHelper.getData(url);

        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            etas.add(JsonToBean.jsonToETA(jsonObject));
        }

        return etas;
    }

    public static List<Stop> routeToStops(Route route) throws IOException {
        String url = routeToStopUrl + route.getRoute() + "/" + route.getBound() + "/" + route.getService_type();
        String data = HttpClientHelper.getData(url);
        List<Stop> itemStops = new ArrayList<>();
        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String stopId = jsonObject.get("stop").getAsString();

            itemStops.add(DataBaseManager.findStop(stopId));
        }

        return itemStops;
    }

    public static int findNearestStopIndex(List<Stop> stops, LatLng location) {
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;

        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            LatLng stopLocation = new LatLng(Double.parseDouble(stop.getLat()), Double.parseDouble(stop.getLong()));

            double distance = LocationHelper.distance(location, stopLocation);

            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    public static long getMinutesRemaining(Date targetDate) {
        Date now = new Date();
        long currentTimeMillis = now.getTime();
        long targetTimeMillis = targetDate.getTime();
        long timeDifferenceMillis = targetTimeMillis - currentTimeMillis;

        // 将毫秒差转换为分钟
        long minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);

        return minutesRemaining;
    }
}
