package com.gg.busStation.data.bus;

public class Route {
    public static String Out = "outbound";
    public static String In = "inbound";

    private String route;
    private String bound;

    private String service_type;

    private String orig_en;
    private String orig_tc;
    private String orig_sc;
    private String dest_en;
    private String dest_tc;
    private String dest_sc;

    public Route(String route, String bound, String service_type,
                 String orig_en, String orig_tc, String orig_sc,
                 String dest_en, String dest_tc, String dest_sc) {
        this.route = route;
        this.bound = bound;
        this.service_type = service_type;
        this.orig_en = orig_en;
        this.orig_tc = orig_tc;
        this.orig_sc = orig_sc;
        this.dest_en = dest_en;
        this.dest_tc = dest_tc;
        this.dest_sc = dest_sc;
    }

    public String getOrig(String language) {
        if ("en".equals(language)) {
            return orig_en;
        }

        if ("zh_HK".equals(language)) {
            return orig_tc;
        }

        return orig_sc;
    }

    public String getDest(String language) {
        if ("en".equals(language)) {
            return dest_en;
        }

        if ("zh_HK".equals(language)) {
            return dest_tc;
        }

        return dest_sc;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getBound() {
        if("O".equals(bound)) {
            return Out;
        }

        return In;
    }

    public void setBound(String bound) {
        this.bound = bound;
    }

    public String getService_type() {
        return service_type;
    }

    public void setService_type(String service_type) {
        this.service_type = service_type;
    }

    public void setOrig_en(String orig_en) {
        this.orig_en = orig_en;
    }

    public void setOrig_tc(String orig_tc) {
        this.orig_tc = orig_tc;
    }

    public void setOrig_sc(String orig_sc) {
        this.orig_sc = orig_sc;
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
}
