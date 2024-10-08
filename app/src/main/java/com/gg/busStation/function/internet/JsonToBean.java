package com.gg.busStation.function.internet;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.bus.Route;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Feature> parseFeaturesFromString(String jsonString) {
        List<Feature> features = new ArrayList<>();

        try (JsonReader reader = new JsonReader(new StringReader(jsonString))) {
            reader.beginObject();  // 开始解析 JSON 对象
            while (reader.hasNext()) {
                String key = reader.nextName();  // 获取键名
                if (key.equals("features")) {
                    reader.beginArray();  // 进入 features 数组
                    while (reader.hasNext()) {
                        Feature feature = parseSingleFeature(reader);  // 解析单个 Feature 对象
                        features.add(feature);  // 将解析后的 Feature 对象添加到列表中
                    }
                    reader.endArray();  // 结束 features 数组
                } else {
                    reader.skipValue();  // 跳过其他键值对
                }
            }
            reader.endObject();  // 结束 JSON 对象
        } catch (IOException e) {
            e.printStackTrace();
        }

        return features;  // 返回解析后的 Feature 列表
    }

    // 解析单个 Feature 对象
    private static Feature parseSingleFeature(JsonReader reader) throws IOException {
        Feature feature = new Feature();
        feature.properties = new Feature.Properties();

        reader.beginObject();  // 开始解析 Feature 对象
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("properties")) {
                parseProperties(reader, feature.properties);  // 解析 properties 对象
            } else {
                reader.skipValue();  // 跳过其他不需要的字段
            }
        }
        reader.endObject();  // 结束 Feature 对象

        return feature;
    }

    // 解析 Properties 对象
    private static void parseProperties(JsonReader reader, Feature.Properties properties) throws IOException {
        reader.beginObject();  // 开始解析 Properties 对象
        while (reader.hasNext()) {
            String key = reader.nextName();
            switch (key) {
                case "routeNameC":
                    properties.routeNameC = reader.nextString();
                    break;
                case "companyCode":
                    properties.companyCode = reader.nextString();
                    break;
                default:
                    reader.skipValue();  // 跳过其他不需要的字段
            }
        }
        reader.endObject();  // 结束 Properties 对象
    }
}
