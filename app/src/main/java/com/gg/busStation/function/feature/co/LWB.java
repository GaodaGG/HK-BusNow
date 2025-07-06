package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;

import java.util.List;

@SuppressWarnings("unused")
public class LWB implements Company {
    KMB kmb = new KMB();

    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        return kmb.getETA(routeId, routeSeq, stopSeq, db);
    }

    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        return kmb.getStopId(routeName, routeSeq, stopSeq);
    }
}
