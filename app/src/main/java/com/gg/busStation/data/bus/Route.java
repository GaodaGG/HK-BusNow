package com.gg.busStation.data.bus;

import java.util.HashMap;
import java.util.Locale;

public class Route {
    public static String Out = "outbound";
    public static String In = "inbound";

    public static String coKMB = "KMB";
    public static String coCTB = "CTB";
    public static String coBoth = "KMB+CTB";

    private String co;
    private String route;
    private String bound;

    private String service_type;

    private String orig_en;
    private String orig_tc;
    private String orig_sc;
    private String dest_en;
    private String dest_tc;
    private String dest_sc;

    public Route(String co, String route, String bound, String service_type,
                 String orig_en, String orig_tc, String orig_sc,
                 String dest_en, String dest_tc, String dest_sc) {
        this.co = co;
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

    public Route(Route route) {
        this.co = route.co;
        this.route = route.route;
        this.bound = route.bound;
        this.service_type = route.service_type;
        this.orig_en = route.orig_en;
        this.orig_tc = route.orig_tc;
        this.orig_sc = route.orig_sc;
        this.dest_en = route.dest_en;
        this.dest_tc = route.dest_tc;
        this.dest_sc = route.dest_sc;
    }

    public String getOrig(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return orig_en;
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return orig_tc;
        }

        return orig_sc;
    }

    public String getDest(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return dest_en;
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
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
        if ("O".equals(bound) || Out.equals(bound)) {
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

    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    public static String getLocalizedCoName(String co, String language) {
        HashMap<String, String> zhMap = new HashMap<>();
        zhMap.put("KMB", "九巴");
        zhMap.put("CTB", "城巴");
        zhMap.put("KMB+CTB", "九巴+城巴");

        if (new Locale("en").getLanguage().equals(language)) {
            return co;
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return zhMap.get(co);
        }

        return zhMap.get(co);
    }
}
