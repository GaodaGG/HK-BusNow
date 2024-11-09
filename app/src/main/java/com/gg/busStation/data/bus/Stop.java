package com.gg.busStation.data.bus;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class Stop {
    private String stop;

    private String name_en;
    private String name_tc;
    private String name_sc;

    private String lat;
    @SerializedName("long")
    private String lon;

    public Stop(String stop, String name_en, String name_tc, String name_sc, String lat, String lon) {
        this.stop = stop;
        this.name_en = name_en;
        this.name_tc = name_tc;
        this.name_sc = name_sc;
        this.lat = lat;
        this.lon = lon; // 确保参数名称正确
    }


    public String getName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return name_en;
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return name_tc;
        }

        return name_sc;
    }

    public String getLong() {
        return lon;
    }

    public void setLong(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public void setName_tc(String name_tc) {
        this.name_tc = name_tc;
    }

    public void setName_sc(String name_sc) {
        this.name_sc = name_sc;
    }
}
