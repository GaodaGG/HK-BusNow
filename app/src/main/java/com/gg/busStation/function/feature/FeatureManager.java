package com.gg.busStation.function.feature;

import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.CloudFeature;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.function.database.dao.FeatureDAO;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.database.dao.RouteDAO;
import com.gg.busStation.function.database.dao.RouteDAOImpl;
import com.gg.busStation.function.database.dao.StopDAO;
import com.gg.busStation.function.database.dao.StopDAOImpl;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.gg.busStation.function.location.LocationHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FeatureManager {
    private static final String dataJsonUrl = "https://static.data.gov.hk/td/routes-fares-geojson/JSON_BUS.json";
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

    public List<CloudFeature> fetchAllFeatures() throws IOException {
        InputStream data = HttpClientHelper.getDataStream(dataJsonUrl);
        List<CloudFeature> features = JsonToBean.parseFeaturesFromStream(data);
        data.close();

        return features;
    }

    public void saveFeatures(List<CloudFeature> features) {
        for (CloudFeature feature : features) {
            saveStopToDB(feature);
            saveFeatureToDB(feature);
            saveRouteToDB(feature);
        }
    }

    private void saveRouteToDB(CloudFeature feature) {
        routeDAO.insert(convertToRoute(feature));
    }

    private void saveFeatureToDB(CloudFeature feature) {
        if (!featureDAO.exists(feature.getProperties().getRouteId())) {
            featureDAO.insert(cloudFeatureToDBFeature(feature));
        }
    }

    private void saveStopToDB(CloudFeature feature) {
        if (!stopDAO.exists(feature.getProperties().getStopId())) {
            Stop stop = convertToStop(feature);
            stopDAO.insert(stop);
        }
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
