package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.function.feature.FeatureManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KMBCTB implements Company{
    KMB kmb = new KMB();
    CTB ctb = new CTB();

    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        int ctbRouteSeq = (routeSeq == FeatureManager.inbound ? FeatureManager.outbound : FeatureManager.inbound);

        List<ETA> kmbEtas = kmb.getETA(routeId, routeSeq, stopSeq, db);
        List<ETA> ctbEtas = ctb.getETA(routeId, ctbRouteSeq, stopSeq, db);

        if (kmbEtas.isEmpty() && ctbEtas.isEmpty()) {
            return Collections.emptyList();
        }

        kmbEtas.addAll(ctbEtas);
        kmbEtas.sort(Comparator.comparingLong(eta -> eta.getTime().getTime()));
        return kmbEtas;
    }

    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        int ctbRouteSeq = (routeSeq == FeatureManager.inbound ? FeatureManager.outbound : FeatureManager.inbound);
        return kmb.getStopId(routeName, routeSeq, stopSeq) + "," + ctb.getStopId(routeName, ctbRouteSeq, stopSeq);
    }
}
