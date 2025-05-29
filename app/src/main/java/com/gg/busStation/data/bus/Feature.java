package com.gg.busStation.data.bus;

import java.util.Locale;

@lombok.Getter
@lombok.Setter
public class Feature {
    private int routeId;
    private String routeNameC;
    private String routeNameS;
    private String routeNameE;
    private int routeType;
    private String serviceMode;
    private int specialType;
    private String companyCode;
    private int journeyTime;
    private String locStartNameC;
    private String locStartNameS;
    private String locStartNameE;
    private String locEndNameC;
    private String locEndNameS;
    private String locEndNameE;
    private double fullFare;

    public Feature(int routeId, String routeNameC, String routeNameS, String routeNameE,
                   int routeType, String serviceMode, int specialType, String companyCode,
                   int journeyTime, String locStartNameC, String locStartNameS, String locStartNameE,
                   String locEndNameC, String locEndNameS, String locEndNameE, double fullFare) {
        this.routeId = routeId;
        this.routeNameC = routeNameC;
        this.routeNameS = routeNameS;
        this.routeNameE = routeNameE;
        this.routeType = routeType;
        this.serviceMode = serviceMode;
        this.specialType = specialType;
        this.companyCode = companyCode;
        this.journeyTime = journeyTime;
        this.locStartNameC = locStartNameC;
        this.locStartNameS = locStartNameS;
        this.locStartNameE = locStartNameE;
        this.locEndNameC = locEndNameC;
        this.locEndNameS = locEndNameS;
        this.locEndNameE = locEndNameE;
        this.fullFare = fullFare;
    }

    public String getRouteName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return getRouteNameE();
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return getRouteNameC();
        }

        return getRouteNameS();
    }

    public String getStartName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return getLocStartNameE();
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return getLocStartNameC();
        }

        return getLocStartNameS();
    }

    public String getEndName(String language) {
        if (new Locale("en").getLanguage().equals(language)) {
            return getLocEndNameE();
        }

        if (new Locale("zh_HK").getLanguage().equals(language)) {
            return getLocEndNameC();
        }

        return getLocEndNameS();
    }
}
