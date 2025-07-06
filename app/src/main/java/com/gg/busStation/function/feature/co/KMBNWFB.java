package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public class KMBNWFB implements Company{
    KMB kmb = new KMB();
    NWFB nwfb = new NWFB();

    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        List<ETA> kmbEtas = kmb.getETA(routeId, routeSeq, stopSeq, db);
        List<ETA> ctbEtas = nwfb.getETA(routeId, routeSeq, stopSeq, db);

        if (kmbEtas.isEmpty() && ctbEtas.isEmpty()) {
            return Collections.emptyList();
        }

        kmbEtas.addAll(ctbEtas);
        kmbEtas.sort(Comparator.comparingLong(eta -> eta.getTime().getTime()));
        return kmbEtas;
    }

    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        return kmb.getStopId(routeName, routeSeq, stopSeq) + "," + nwfb.getStopId(routeName, routeSeq, stopSeq);
    }
}
