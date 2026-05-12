package com.gg.busStation.function.internet;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.CloudFeature;
import com.gg.busStation.data.bus.Stop;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonToBean {
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();

    private JsonToBean() {
    }

    public static Stop jsonToStop(JsonObject json) {
        return gson.fromJson(json, Stop.class);
    }

    public static ETA jsonToETA(JsonObject json) {
        return gson.fromJson(json, ETA.class);
    }

    public static JsonObject extractJsonObject(String json) {
        return JsonParser.parseString(json)
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
    }

    public static JsonArray extractJsonArray(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = jsonObject.getAsJsonArray("data");
        return data;
    }

    /**
     * @deprecated This method loads the entire JSON file into memory, causing high memory usage.
     *             Use streaming parsing ({@link com.google.gson.stream.JsonReader}) instead.
     */
    @Deprecated
    public static List<CloudFeature> parseFeaturesFromStream(InputStream stream) {
        List<CloudFeature> features = new ArrayList<>();
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.beginObject();
            while (reader.hasNext()) {
                features.addAll(processStream(reader));
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return features;
    }

    private static List<CloudFeature> processStream(JsonReader reader) throws IOException {
        List<CloudFeature> features = new ArrayList<>();
        String key = reader.nextName();

        if (key.equals("features")) {
            reader.beginArray();
            while (reader.hasNext()) {
                CloudFeature feature = gson.fromJson(reader, CloudFeature.class);
                features.add(feature);
            }
            reader.endArray();
        } else {
            reader.skipValue();
        }

        return features;
    }
}
