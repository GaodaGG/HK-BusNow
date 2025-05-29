package com.gg.busStation.function.feature.co;

import android.database.sqlite.SQLiteDatabase;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.feature.FeatureManager;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.internet.JsonToBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

public class CTB implements Company {
    public static final String routeToStopUrl = "https://rt.data.gov.hk/v2/transport/citybus/route-stop/ctb/";
    public static final String routeAndStopToETAUrl = "https://rt.data.gov.hk/v2/transport/citybus/eta/ctb/";

    @SneakyThrows
    @Override
    public List<ETA> getETA(int routeId, int routeSeq, int stopSeq, SQLiteDatabase db) {
        List<ETA> etas = new ArrayList<>();
        String routeName = new FeatureDAOImpl(db).getFeature(routeId).getRouteName("en");

        String stop = getStopId(routeName, routeSeq, stopSeq);
        String url = routeAndStopToETAUrl + stop + "/" + routeName;
        String data = HttpClientHelper.getData(url);

        for (JsonElement jsonElement : JsonToBean.extractJsonArray(data)) {
            String bound = routeSeq == FeatureManager.outbound ? "I" : "O";
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!"".equals(jsonObject.get("eta").getAsString()) && bound.equals(jsonObject.get("dir").getAsString())) {
                ETA eta = JsonToBean.jsonToETA(jsonObject);
                etas.add(eta);
            }
        }

        return etas;
    }

    // TODO 将巴士id放入缓存以便二次使用
    @SneakyThrows
    @Override
    public String getStopId(String routeName, int routeSeq, int stopSeq) {
        String stopUrl = routeToStopUrl + routeName + "/" + (routeSeq == FeatureManager.inbound ? FeatureManager.Out : FeatureManager.In);
        String stopData = HttpClientHelper.getData(stopUrl);
        JsonArray jsonElements = JsonToBean.extractJsonArray(stopData);

        return jsonElements.get(stopSeq).getAsJsonObject().get("stop").getAsString();
    }
}
