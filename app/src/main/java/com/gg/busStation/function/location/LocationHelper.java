package com.gg.busStation.function.location;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;

public class LocationHelper {
    private static LocationClient mLocationClient;
    private static LocationClientOption mLocationClientOption;
    private static final String apiKey = "D4GLNJOTDV8d2JooN0EMm3qdHLLr7pao";

    public static void init(Context context) throws Exception {
        LocationClient.setKey(apiKey);
        SDKInitializer.setApiKey(apiKey);
        LocationClient.setAgreePrivacy(true);
        SDKInitializer.setAgreePrivacy(context.getApplicationContext(),true);

        SDKInitializer.initialize(context.getApplicationContext());
        mLocationClient = new LocationClient(context.getApplicationContext());
        mLocationClientOption = new LocationClientOption();

        mLocationClientOption.setLocationMode(LocationClientOption.LocationMode.Fuzzy_Locating);
        mLocationClientOption.setScanSpan(0);

        mLocationClient.setLocOption(mLocationClientOption);

        mLocationClient.registerLocationListener(new LocationListener());

        mLocationClient.start();
    }

    public static LatLng coordinateConvert(LatLng latLng) {
        CoordinateConverter coord = new CoordinateConverter()
                .from(CoordinateConverter.CoordType.GPS)
                .coord(latLng);

        return coord.convert();
    }

    public static LatLng getLocation() {
        if (System.currentTimeMillis() - LocationListener.getLastUpdateTime() >= 60000) {
            mLocationClient.requestLocation();
        }

        double latitude = getLatitude();
        double longitude = getLongitude();
        return new LatLng(latitude, longitude);
    }

    public static double distance(LatLng latLngA, LatLng latLngB){
        return DistanceUtil.getDistance(latLngA, latLngB);
    }

    private static double getLatitude() {
        return LocationListener.getLastLatitude();
    }

    private static double getLongitude() {
        return LocationListener.getLastLongitude();
    }
}
