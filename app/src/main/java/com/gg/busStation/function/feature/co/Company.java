package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;

import java.util.List;

public interface Company {
    List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db);

    String getStopId(String routeName, int routeSeq, int stopSeq);
}
