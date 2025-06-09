package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LWBCTB implements Company {
    private final LWB lwb = new LWB();
    private final CTB ctb = new CTB();

    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        List<ETA> lwbEtas = lwb.getETA(routeId, routeSeq, stopSeq, db);
        List<ETA> ctbEtas = ctb.getETA(routeId, routeSeq, stopSeq, db);

        if (lwbEtas.isEmpty() && ctbEtas.isEmpty()) {
            return Collections.emptyList();
        }

        lwbEtas.addAll(ctbEtas);
        lwbEtas.sort(Comparator.comparingLong(eta -> eta.getTime().getTime()));
        return lwbEtas;
    }

    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        return lwb.getStopId(routeName, routeSeq, stopSeq) + "," + ctb.getStopId(routeName, routeSeq, stopSeq);
    }
}
