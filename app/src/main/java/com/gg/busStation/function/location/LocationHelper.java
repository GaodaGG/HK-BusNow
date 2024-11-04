package com.gg.busStation.function.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

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
    
    private static Context mContext;

    private LocationHelper() {
    }

    public static void init(Context context){
        mContext = context;
        
        LocationClient.setKey(apiKey);
        SDKInitializer.setApiKey(apiKey);
        LocationClient.setAgreePrivacy(true);
        Context applicationContext = context.getApplicationContext();
        SDKInitializer.setAgreePrivacy(applicationContext,true);

        SDKInitializer.initialize(applicationContext);
        try {
            mLocationClient = new LocationClient(applicationContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static LatLng getLocation(boolean updateNow) {
        if (System.currentTimeMillis() - LocationListener.getLastUpdateTime() >= 60000 || updateNow) {
            mLocationClient.requestLocation();
            mLocationClient.start();
        }

        double latitude = getLatitude();
        double longitude = getLongitude();
        if (latitude != 0 && latitude != Double.MIN_VALUE) {
            return new LatLng(latitude, longitude);
        }

        return getLastLocation();
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

    @SuppressLint("MissingPermission")
    private static LatLng getLastLocation() {
        try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                return new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }

            return new LatLng(0, 0);
        } catch (Exception e) {
            return new LatLng(Double.MIN_VALUE, Double.MIN_VALUE);
        }
    }
}
