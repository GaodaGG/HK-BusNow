package com.gg.busStation.data.bus;

public class Feature {
    public Properties properties;

    @Override
    public String toString() {
        return "Feature{" +
                "properties=" + properties +
                '}';
    }

    // 定义 Properties 类，只包含需要的字段
    public static class Properties {
        public String routeNameC;
        public String companyCode;

        @Override
        public String toString() {
            return "Properties{" +
                    "routeNameC='" + routeNameC + '\'' +
                    ", companyCode='" + companyCode + '\'' +
                    '}';
        }
    }
}
