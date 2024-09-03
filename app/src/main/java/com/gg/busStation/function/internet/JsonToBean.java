package com.gg.busStation.function.internet;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.bus.Route;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonToBean {
    public static Route jsonToRoute(JsonObject json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Route.class);
    }

    public static Stop jsonToStop(JsonObject json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Stop.class);
    }

    public static ETA jsonToETA(JsonObject json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ETA.class);
    }

    public static JsonObject extractJsonObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject().get("data").getAsJsonObject();
    }

    public static JsonArray extractJsonArray(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = jsonObject.getAsJsonArray("data");
        return data;
    }
}
