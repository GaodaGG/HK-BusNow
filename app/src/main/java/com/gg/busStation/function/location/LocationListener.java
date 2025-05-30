package com.gg.busStation.function.location;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import lombok.Getter;


public class LocationListener extends BDAbstractLocationListener {
    @Getter
    private static double lastLatitude;
    @Getter
    private static double lastLongitude;
    @Getter
    private static long lastUpdateTime;

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        lastLatitude = bdLocation.getLatitude();
        lastLongitude = bdLocation.getLongitude();

        lastUpdateTime = System.currentTimeMillis();
    }
}
