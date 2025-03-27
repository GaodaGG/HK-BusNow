package com.gg.busStation.function;

import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.gg.busStation.function.location.LocationHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BusDataManager {
    private static final String govJsonUrl = "https://static.data.gov.hk/td/routes-fares-geojson/JSON_BUS.json";
    private static final String faresJsonUrl = "https://static.data.gov.hk/td/pt-headway-tc/fare_attributes.txt";
    private static Map<Integer, String> routeIdToName = new HashMap<>();

    private BusDataManager() {
    }

    public static void initData(OnDataInitListener onDataInitListener, boolean updateNow) throws IOException {
        //判断是否需要更新数据
        if (!updateNow) {
            Map<String, String> settings = DataBaseManager.getSettings();
            String oldLastUpdateTime = settings.get("lastUpdateTime");
            long lastUpdateTime = Long.parseLong(oldLastUpdateTime);

            if ((System.currentTimeMillis() <= lastUpdateTime + Long.parseLong(settings.get("updateTime")) && "true".equals(settings.get("isInit"))) || "0".equals(settings.get("updateTime"))) {
                return;
            }
        }

        onDataInitListener.start();

        List<Route> routeList = initRoutes();
        List<Stop> stopList = initStops();
        Map<String, String> fareMap = initFares();
        DataBaseManager.initData(routeList, stopList, fareMap);

        onDataInitListener.finish();
    }

    private static List<Route> initRoutes() throws IOException {
        List<Route> routes = new ArrayList<>();

        // 获取路线资料
        List<String> bothRoutes = getBothRoutes();

        // 获取九巴路线列表
        KMB.initRoutes(routes, bothRoutes);

        // 获取城巴路线列表
        CTB.initRoutes(routes, bothRoutes);

        bothRoutes.clear();

        return routes;
    }

    private static @NonNull List<String> getBothRoutes() throws IOException {
        ArrayList<Feature> features = getRoutes();
        List<String> bothRoutes = new ArrayList<>();

        features.forEach(feature -> {
            Integer routeId = feature.properties.routeId;
            String routeName = feature.properties.routeNameC;
            String companyCode = feature.properties.companyCode;
            //存入Route id和name的对应关系
            routeIdToName.put(routeId, routeName);

            //获取重复路线
            if (bothRoutes.contains(routeName)) {
                return;
            }

            if ("KMB+CTB".equals(companyCode)) {
                bothRoutes.add(routeName);
            }
        });
        return bothRoutes;
    }

    private static @NonNull ArrayList<Feature> getRoutes() throws IOException {
        ArrayList<Feature> bothRoute = new ArrayList<>();
        InputStream data = HttpClientHelper.getDataStream(govJsonUrl);
        List<Feature> features = JsonToBean.parseFeaturesFromStream(data);
        data.close();
        features.forEach(feature -> {
            String routeName = feature.properties.routeNameC;
            String companyCode = feature.properties.companyCode;

            if (bothRoute.contains(routeName)) {
                return;
            }

            if ("KMB".equals(companyCode) || "CTB".equals(companyCode) || "KMB+CTB".equals(companyCode)) {
                bothRoute.add(feature);
            }
        });
        return bothRoute;
    }

    private static List<Stop> initStops() throws IOException {
        List<Stop> stops = new ArrayList<>();

        // 获取九巴站点数据
        String data = HttpClientHelper.getData(KMB.stopUrl);
        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            stops.add(JsonToBean.jsonToStop(jsonObject));
        }
        return stops;
    }

    public static List<ETA> routeAndStopToETAs(Route route, Stop stop, int seq) throws IOException {
        List<ETA> etas = new ArrayList<>();
        if (route.getCo().equals(Route.coKMB) || route.getCo().equals(Route.coBoth)) {
            KMB.routeAndStopToETAs(route, stop, etas);
        }

        if (route.getCo().equals(Route.coCTB) || route.getCo().equals(Route.coBoth)) {
            CTB.routeAndStopToETAs(route, seq, etas);
        }

        etas.sort((eta1, eta2) -> {
            if (eta1.getTime() == null || eta2.getTime() == null) {
                return 0; // 处理空值（如果有）
            }
            return eta1.getTime().compareTo(eta2.getTime());
        });
        return etas;
    }

    // TODO: 增加CTB站点
    public static List<ETA> stopToETAs(Stop stop) throws IOException {
        List<ETA> etasKMB = new ArrayList<>();
        KMB.stopToETAs(stop, etasKMB);
        return etasKMB;
    }

    private static Map<String, String> initFares() throws IOException {
        if (routeIdToName.isEmpty()) {
            getBothRoutes();
        }

        InputStream dataStream = HttpClientHelper.getDataStream(faresJsonUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream))) {
            Map<String, String> fares = new HashMap<>();
            String line;

            // 跳过表头行
            reader.readLine();

            String lastRouteId = "";
            String lastBound = "";
            String lastFare = "";
            String startPickStop = "";
            String lastPickStop = "";
            StringBuilder routeFare = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                String[] fare = line.split(",");

                if (!fare[5].equals("KMB") && !fare[5].equals("CTB") && !fare[5].equals("KMB+CTB")) {
                    continue;
                }

                String[] fareId = fare[0].split("-");

                String routeId = fareId[0];
                String bound = fareId[1];
                String currentPickStop = fareId[2];
                String fareValue = fare[1];

                if (!lastRouteId.equals(routeId) || !lastBound.equals(bound)) {
                    // 如果为新的路线或方向则保存之前的数据
                    if (!lastRouteId.isEmpty()) {
                        routeFare.append(startPickStop).append("-").append(lastPickStop)
                                .append(",").append(lastFare);

                        String routeName = routeIdToName.get(Integer.parseInt(lastRouteId));
                        String routeBound = lastBound.equals("1") ? Route.Out : Route.In;
                        fares.put(routeName + "_" + routeBound, routeFare.toString());
                    }

                    // 为新的路线或方向重置
                    routeFare.setLength(0);
                    lastRouteId = routeId;
                    lastBound = bound;
                    startPickStop = currentPickStop;
                    lastPickStop = currentPickStop;
                    lastFare = fareValue;
                }

                // 将fare附加到 StringBuilder
                if (!lastPickStop.equals(currentPickStop) && !fareValue.equals(lastFare)) {
                    routeFare.append(startPickStop).append("-").append(lastPickStop)
                            .append(",").append(lastFare).append(";");
                    startPickStop = currentPickStop;
                    lastFare = fareValue;
                }

                lastPickStop = currentPickStop;

            }

            return fares;
        }
    }

    public static List<Route> getNearRoutes(int distance) {
        LatLng location = LocationHelper.getLocation(false);
        List<Stop> stopsByLocation = DataBaseManager.findStopsByLocation(location, distance);
        List<Route> routes = new ArrayList<>();

        if (stopsByLocation.isEmpty()) {
            return routes;
        }

        List<ETA> etas = new ArrayList<>();
        stopsByLocation.forEach(stop -> {
            try {
                etas.addAll(BusDataManager.stopToETAs(stop));
            } catch (IOException e) {
                Log.e("Stops", "Error while getting ETAs", e);
            }
        });

        //对etas根据eta.getRoute()进行filter 去重
        Set<String> seenIds = new HashSet<>();

        etas.stream().filter(eta -> eta.getTime() == null).collect(Collectors.toList());
        etas.sort((eta1, eta2) -> {
            if (eta1.getTime() == null || eta2.getTime() == null) {
                return 0; // 处理空值（如果有）
            }

            return eta1.getTime().compareTo(eta2.getTime());
        });
        etas.forEach(eta -> Log.d("ETA", eta.getRoute() + " " + eta.getTime()));
        etas.stream()
                .filter(eta -> seenIds.add(eta.getRoute()))
                .collect(Collectors.toList());
        etas.forEach(eta -> Log.d("ETAFilter", eta.getRoute() + " " + eta.getTime()));

        for (ETA eta : etas) {
            Route route = DataBaseManager.findRoute(eta.getCo(), eta.getRoute(), "O", String.valueOf(eta.getService_type()));
            if (route != null) {
                routes.add(route);
            }
        }

        return routes;
    }

    public static List<Stop> routeToStops(Route route) throws IOException {
        List<Stop> itemStops = new ArrayList<>();

        if (Route.coKMB.equals(route.getCo()) || Route.coBoth.equals(route.getCo())) {
            return KMB.routeToStops(route, itemStops);
        }

        if (Route.coCTB.equals(route.getCo())) {
            return CTB.routeToStops(route, itemStops);
        }

        return itemStops;
    }

    public static int findNearestStopIndex(List<Stop> stops, LatLng location) {
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;

        if (location.latitude == Double.MIN_VALUE || location.latitude == 0) {
            return 0;
        }

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

        long minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long remainderMillis = timeDifferenceMillis % TimeUnit.MINUTES.toMillis(1);

        // 四舍五入
        if (remainderMillis >= 1000 * 30) {
            minutesRemaining++;
        }

        return minutesRemaining;
    }

    public static List<ListItemData> routesToListItemData(List<Route> routes) {
        String language = Locale.getDefault().getLanguage();

        List<ListItemData> data = new ArrayList<>();
        for (Route route : routes) {
            StringBuilder headline = new StringBuilder().append(route.getOrig(language))
                    .append(" -> ")
                    .append(route.getDest(language));

            if (headline.length() > 20) {
                headline = new StringBuilder().append(route.getOrig(language))
                        .append(" ->\n")
                        .append(route.getDest(language));
            }

            String tips = Route.getLocalizedCoName(route.getCo(), language).replace("+", " / ");
            ListItemData listItemData = new ListItemData(route.getCo(),
                    route.getRoute(),
                    headline.toString(),
                    "",
//                        BusDataManager.serviceTypeToName(route.getService_type()),
                    route.getBound(),
                    route.getService_type(),
                    tips);
            data.add(listItemData);
        }
        return data;
    }

    public static String serviceTypeToName(String serviceType) {
        return switch (serviceType) {
            case "1" -> "特定时段或非每天服务路线";
            case "2" -> "设假日及公共假期收费";
            case "3" -> "特定时段或非每天服务路线及设假日及公共假期收费";
            default -> "";
        };
    }

    //    TODO 错误处理
    public interface OnDataInitListener {
        void start();

        void finish();
    }

    private static class KMB {
        public static final String routeUrl = "https://data.etabus.gov.hk/v1/transport/kmb/route/";
        public static final String stopUrl = "https://data.etabus.gov.hk/v1/transport/kmb/stop/";
        public static final String routeToStopUrl = "https://data.etabus.gov.hk/v1/transport/kmb/route-stop/";
        public static final String routeAndStopToETAUrl = "https://data.etabus.gov.hk/v1/transport/kmb/eta/";
        public static final String StopToETAUrl = "https://data.etabus.gov.hk/v1/transport/kmb/stop-eta/";

        private static void initRoutes(List<Route> routes, List<String> bothRoutes) throws IOException {
            String kmbData = HttpClientHelper.getData(KMB.routeUrl);
            for (JsonElement jsonElement : JsonToBean.extractJsonArray(kmbData)) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Route route = JsonToBean.jsonToRoute(jsonObject);
                route.setCo(bothRoutes.contains(route.getRoute()) ? Route.coBoth : Route.coKMB);
                routes.add(route);
            }
        }

        public static List<Stop> routeToStops(Route route, List<Stop> itemStops) throws IOException {
            String url = KMB.routeToStopUrl + route.getRoute() + "/" + route.getBound() + "/" + route.getService_type();
            String data = HttpClientHelper.getData(url);
            for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String stopId = jsonObject.get("stop").getAsString();

                itemStops.add(DataBaseManager.findStop(stopId));
            }
            return itemStops;
        }

        public static void routeAndStopToETAs(Route route, Stop stop, List<ETA> etas) throws IOException {
            String url = KMB.routeAndStopToETAUrl + stop.getStop() + "/" + route.getRoute() + "/" + route.getService_type();
            String data = HttpClientHelper.getData(url);

            for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
                String bound = route.getBound().equals(Route.Out) ? "O" : "I";
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.get("eta").isJsonNull() && bound.equals(jsonObject.get("dir").getAsString())) {
                    ETA eta = JsonToBean.jsonToETA(jsonObject);
                    etas.add(eta);
                }
            }
        }

        public static void stopToETAs(Stop stop, List<ETA> etas) throws IOException {
            String url = KMB.StopToETAUrl + stop.getStop();
            String data = HttpClientHelper.getData(url);

            for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                ETA eta = JsonToBean.jsonToETA(jsonObject);
                etas.add(eta);
            }
        }
    }

    private static class CTB {
        public static final String routeUrl = "https://rt.data.gov.hk/v2/transport/citybus/route/ctb";
        public static final String stopUrl = "https://rt.data.gov.hk/v2/transport/citybus/stop/";
        public static final String routeToStopUrl = "https://rt.data.gov.hk/v2/transport/citybus/route-stop/ctb/";
        public static final String routeAndStopToETAUrl = "https://rt.data.gov.hk/v2/transport/citybus/eta/ctb/";

        public static void initRoutes(List<Route> routes, List<String> bothRoutes) throws IOException {
            String ctbData = HttpClientHelper.getData(CTB.routeUrl);
            for (JsonElement jsonElement : JsonToBean.extractJsonArray(ctbData)) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (bothRoutes.contains(jsonObject.get("route").getAsString())) {
                    continue;
                }

                Route route = JsonToBean.jsonToRoute(jsonObject);
                route.setBound(Route.In);
                route.setService_type("1");

                Route routeOut = createOppositeRoute(route);

                routes.add(route);
                routes.add(routeOut);
            }
        }

        private static Route createOppositeRoute(Route route) {
            Route routeOut = new Route(route);
            routeOut.setBound(Route.Out);
            routeOut.setDest_en(route.getOrig("en"));
            routeOut.setDest_tc(route.getOrig("zh_HK"));
            routeOut.setDest_sc(route.getOrig("zh_CN"));
            routeOut.setOrig_en(route.getDest("en"));
            routeOut.setOrig_tc(route.getDest("zh_HK"));
            routeOut.setOrig_sc(route.getDest("zh_CN"));
            return routeOut;
        }

        public static List<Stop> routeToStops(Route route, List<Stop> itemStops) throws IOException {
            String url = CTB.routeToStopUrl + route.getRoute() + "/" + (Route.In.equals(route.getBound()) ? Route.Out : Route.In);
            String data = HttpClientHelper.getData(url);
            JsonArray jsonElements = JsonToBean.extractJsonArray(data);

            for (JsonElement jsonElement : jsonElements) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String stopId = jsonObject.get("stop").getAsString();

                itemStops.add(getStop(stopId));
            }
            return itemStops;
        }

        public static Stop getStop(String stopId) throws IOException {
            String url = CTB.stopUrl + stopId;
            String data = HttpClientHelper.getData(url);
            JsonObject jsonObject = JsonToBean.extractJsonObject(data);
            return JsonToBean.jsonToStop(jsonObject);
        }

        public static void routeAndStopToETAs(Route route, int seq, List<ETA> etas) throws IOException {
            String stopUrl = CTB.routeToStopUrl + route.getRoute() + "/" + (Route.In.equals(route.getBound()) ? Route.Out : Route.In);
            String stopData = HttpClientHelper.getData(stopUrl);
            JsonArray jsonElements = JsonToBean.extractJsonArray(stopData);

            if (jsonElements.isEmpty()) {
                return;
            }

            String stop = jsonElements.get(seq - 1).getAsJsonObject().get("stop").getAsString();

            String url = CTB.routeAndStopToETAUrl + stop + "/" + route.getRoute();
            String data = HttpClientHelper.getData(url);

            for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
                String bound = route.getBound().equals(Route.Out) ? "I" : "O";
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!"".equals(jsonObject.get("eta").getAsString()) && bound.equals(jsonObject.get("dir").getAsString())) {
                    ETA eta = JsonToBean.jsonToETA(jsonObject);
                    etas.add(eta);
                }
            }
        }
    }
}
