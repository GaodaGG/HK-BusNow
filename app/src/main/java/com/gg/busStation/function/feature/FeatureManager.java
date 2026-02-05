package com.gg.busStation.function.feature;

import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.CloudFeature;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FeatureDAO;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.database.dao.RouteDAO;
import com.gg.busStation.function.database.dao.RouteDAOImpl;
import com.gg.busStation.function.database.dao.StopDAO;
import com.gg.busStation.function.database.dao.StopDAOImpl;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.gg.busStation.function.location.LocationHelper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FeatureManager {
    private static final String busDataUrl = "https://static.data.gov.hk/td/routes-fares-geojson/JSON_BUS.json";
    private static final String gmbDataUrl = "https://static.data.gov.hk/td/routes-fares-geojson/JSON_GMB.json";
    public static final int outbound = 1;
    public static final int inbound = 2;

    public static final String Out = "outbound";
    public static final String In = "inbound";

    private final FeatureDAO featureDAO;
    private final StopDAO stopDAO;
    private final RouteDAO routeDAO;

    public FeatureManager(SQLiteDatabase db) {
        this.featureDAO = new FeatureDAOImpl(db);
        this.stopDAO = new StopDAOImpl(db);
        this.routeDAO = new RouteDAOImpl(db);
    }

    public static int findNearestStopIndex(List<Stop> stops, LatLng location) {
        double minDistance = Double.MAX_VALUE;
        int nearestIndex = -1;

        if (location.latitude == Double.MIN_VALUE || location.latitude == 0) {
            return 0;
        }

        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);
            LatLng stopLocation = new LatLng(stop.lat(), stop.lon());
            LatLng latLng = LocationHelper.coordinateConvert(stopLocation);

            double distance = LocationHelper.distance(location, latLng);

            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    public static String getBoundString(int bound) {
        return bound == outbound ? Out : In;
    }

    public void syncFeatures(DataBaseHelper dataBaseHelper) {
        Gson gson = new Gson();
        String[] DATA_URLS = {busDataUrl, gmbDataUrl};
        final int BATCH_SIZE = 1000;
        SQLiteDatabase db = dataBaseHelper.getDatabase();

        db.beginTransaction();
        try {
            for (String url : DATA_URLS) {
                InputStream inputStream = HttpClientHelper.getDataStream(url);
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("features")) {
                        reader.beginArray();
                        int count = 0;
                        while (reader.hasNext()) {
                            CloudFeature feature = gson.fromJson(reader, CloudFeature.class);

                            saveStopToDB(feature);
                            saveFeatureToDB(feature);
                            saveRouteToDB(feature);
                            count++;

                            // 分批提交事务
                            if (count % BATCH_SIZE == 0) {
                                db.setTransactionSuccessful();
                                db.endTransaction();
                                db.beginTransaction();
                            }
                        }
                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                reader.close();
                inputStream.close();
            }
            db.setTransactionSuccessful();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    private void saveRouteToDB(CloudFeature feature) {
        routeDAO.insert(convertToRoute(feature));
    }

    private void saveFeatureToDB(CloudFeature feature) {
        featureDAO.insert(cloudFeatureToDBFeature(feature));
    }

    private void saveStopToDB(CloudFeature feature) {
        stopDAO.insert(convertToStop(feature));
    }

    private Feature cloudFeatureToDBFeature(CloudFeature feature) {
        return new Feature(
                feature.getProperties().getRouteId(),
                feature.getProperties().getRouteNameC(),
                feature.getProperties().getRouteNameS(),
                feature.getProperties().getRouteNameE(),
                feature.getProperties().getRouteType(),
                feature.getProperties().getServiceMode(),
                feature.getProperties().getSpecialType(),
                feature.getProperties().getCompanyCode(),
                feature.getProperties().getJourneyTime(),
                feature.getProperties().getLocStartNameC(),
                feature.getProperties().getLocStartNameS(),
                feature.getProperties().getLocStartNameE(),
                feature.getProperties().getLocEndNameC(),
                feature.getProperties().getLocEndNameS(),
                feature.getProperties().getLocEndNameE(),
                feature.getProperties().getFullFare()
        );
    }

    private Stop convertToStop(CloudFeature feature) {
        CloudFeature.Properties props = feature.getProperties();
        CloudFeature.Geometry geometry = feature.getGeometry();
        return new Stop(
                props.getStopId(),
                props.getStopNameE(),
                props.getStopNameC(),
                props.getStopNameS(),
                geometry.getCoordinates()[1],
                geometry.getCoordinates()[0]
        );
    }

    private Route convertToRoute(CloudFeature feature) {
        CloudFeature.Properties props = feature.getProperties();
        return new Route(
                props.getRouteId(),
                props.getRouteSeq(),
                props.getStopSeq(),
                props.getStopPickDrop(),
                props.getStopId()
        );
    }
}
