package com.gg.busStation.data.bus;

public class Feature {
    public Properties properties;

    @Override
    public String toString() {
        return "Feature{" +
                "properties=" + properties +
                '}';
    }


    public static class Properties {
        public Integer routeId;
        public String routeNameC;
        public String companyCode;

        @Override
        public String toString() {
            return "Properties{" +
                    "routeId='" + routeId + '\'' +
                    "routeNameC='" + routeNameC + '\'' +
                    ", companyCode='" + companyCode + '\'' +
                    '}';
        }
    }
}
