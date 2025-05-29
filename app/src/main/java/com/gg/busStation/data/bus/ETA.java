package com.gg.busStation.data.bus;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ETA {
    private String co;
    private String route;
    private int service_type;

    private int seq;
    private int eta_seq;
    @SerializedName("eta")
    private Date time;

    private String dest_en;
    private String dest_tc;
    private String dest_sc;

    private String rmk_en;
    private String rmk_tc;
    private String rmk_sc;

    private Date data_timestamp;

    public String getDest(String language) {
        if ("en".equals(language)) {
            return dest_en;
        }

        if ("zh_HK".equals(language)) {
            return dest_tc;
        }

        return dest_sc;
    }

    public String getRmk(String language) {
        if ("en".equals(language)) {
            return rmk_en;
        }

        if ("zh_HK".equals(language)) {
            return rmk_tc;
        }

        return rmk_sc;
    }
}
