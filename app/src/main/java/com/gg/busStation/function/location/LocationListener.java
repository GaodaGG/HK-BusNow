package com.gg.busStation.function.location;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

public class LocationListener extends BDAbstractLocationListener {
    private static double lastLatitude;
    private static double lastLongitude;
    private static long lastUpdateTime;

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        lastLatitude = bdLocation.getLatitude();
        lastLongitude = bdLocation.getLongitude();

        lastUpdateTime = System.currentTimeMillis();
    }

    public static long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public static double getLastLatitude() {
        return lastLatitude;
    }

    public static double getLastLongitude() {
        return lastLongitude;
    }
}
