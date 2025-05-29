package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.feature.FeatureManager;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class KMB implements Company {
    public static final String routeAndStopToETAUrl = "https://data.etabus.gov.hk/v1/transport/kmb/eta/";
    public static final String routeToStopUrl = "https://data.etabus.gov.hk/v1/transport/kmb/route-stop/";

    @SneakyThrows
    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        List<ETA> etas = new ArrayList<>();

        Feature feature = new FeatureDAOImpl(db).getFeature(routeId);
        String stopId = getStopId(feature.getRouteNameE(), routeSeq, stopSeq);
        String url = routeAndStopToETAUrl + stopId + "/" + feature.getRouteNameE() + "/1";
        String data = HttpClientHelper.getData(url);

        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            String bound = routeSeq == FeatureManager.outbound ? "O" : "I";
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.get("eta").isJsonNull() && bound.equals(jsonObject.get("dir").getAsString())) {
                ETA eta = JsonToBean.jsonToETA(jsonObject);
                etas.add(eta);
            }
        }

        return etas;
    }

    @SneakyThrows
    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        String url = routeToStopUrl + routeName + "/" + FeatureManager.getBoundString(routeSeq) + "/1";
        String data = HttpClientHelper.getData(url);
        JsonElement jsonElement = JsonToBean.extractJsonArray(data).get(stopSeq);
        return jsonElement.getAsJsonObject().get("stop").getAsString();
    }
}
