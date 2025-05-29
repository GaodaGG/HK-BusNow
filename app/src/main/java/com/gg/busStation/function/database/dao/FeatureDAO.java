package com.gg.busStation.function.database.dao;

import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.function.feature.CompanyManager;

import java.util.List;

public interface FeatureDAO {
    boolean exists(int routeId);

    void insert(Feature feature);

    Feature getFeature(int routeId);

    Feature getFeature(String routeName);

    List<Feature> getFeatures(CompanyManager.CompanyEnum company);

    List<Feature> fuzzySearchFeature(String routeName);

    List<String> getFeatureNthCharacters(String routeName, int index);
}

