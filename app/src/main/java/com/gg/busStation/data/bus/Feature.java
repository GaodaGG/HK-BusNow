package com.gg.busStation.data.bus;

import java.util.Locale;

@lombok.Getter
public final class Feature {
    private final int routeId;
    private final String routeNameC;
    private final String routeNameS;
    private final String routeNameE;
    private final int routeType;
    private final String serviceMode;
    private final int specialType;
    private final String companyCode;
    private final int journeyTime;
    private final String locStartNameC;
    private final String locStartNameS;
    private final String locStartNameE;
    private final String locEndNameC;
    private final String locEndNameS;
    private final String locEndNameE;
    private final double fullFare;

    private static final Locale LOCALE_EN = new Locale("en");
    private static final Locale LOCALE_ZH_HK = new Locale("zh", "HK");

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
        if (LOCALE_EN.getLanguage().equals(language)) {
            return getRouteNameE();
        }

        if (LOCALE_ZH_HK.getLanguage().equals(language)) {
            return getRouteNameC();
        }

        return getRouteNameS();
    }

    public String getStartName(String language) {
        if (LOCALE_EN.getLanguage().equals(language)) {
            return getLocStartNameE();
        }

        if (LOCALE_ZH_HK.getLanguage().equals(language)) {
            return getLocStartNameC();
        }

        return getLocStartNameS();
    }

    public String getEndName(String language) {
        if (LOCALE_EN.getLanguage().equals(language)) {
            return getLocEndNameE();
        }

        if (LOCALE_ZH_HK.getLanguage().equals(language)) {
            return getLocEndNameC();
        }

        return getLocEndNameS();
    }
}
