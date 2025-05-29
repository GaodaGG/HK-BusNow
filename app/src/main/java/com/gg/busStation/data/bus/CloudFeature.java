package com.gg.busStation.data.bus;

@lombok.ToString
@lombok.Getter
@lombok.Setter
public class CloudFeature {
    private String type;
    private Geometry geometry;
    private Properties properties;

    //该点为巴士站点的坐标
    @lombok.Getter
    @lombok.Setter
    public static class Geometry {
        private String type;
        private double[] coordinates;
    }

    @lombok.Getter
    @lombok.Setter
    public static class Properties {
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
        private int routeSeq;
        private int stopSeq;
        private int stopId;
        private int stopPickDrop;
        private String stopNameC;
        private String stopNameS;
        private String stopNameE;
    }
}
