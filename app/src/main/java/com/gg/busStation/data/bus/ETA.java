package com.gg.busStation.data.bus;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

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

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public int getService_type() {
        return service_type;
    }

    public void setService_type(int service_type) {
        this.service_type = service_type;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getEta_seq() {
        return eta_seq;
    }

    public void setEta_seq(int eta_seq) {
        this.eta_seq = eta_seq;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getData_timestamp() {
        return data_timestamp;
    }

    public void setData_timestamp(Date data_timestamp) {
        this.data_timestamp = data_timestamp;
    }

    public void setDest_en(String dest_en) {
        this.dest_en = dest_en;
    }

    public void setDest_tc(String dest_tc) {
        this.dest_tc = dest_tc;
    }

    public void setDest_sc(String dest_sc) {
        this.dest_sc = dest_sc;
    }

    public void setRmk_en(String rmk_en) {
        this.rmk_en = rmk_en;
    }

    public void setRmk_tc(String rmk_tc) {
        this.rmk_tc = rmk_tc;
    }

    public void setRmk_sc(String rmk_sc) {
        this.rmk_sc = rmk_sc;
    }
}
