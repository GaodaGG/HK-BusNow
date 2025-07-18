package com.gg.busStation.function.database.dao;

import com.gg.busStation.data.bus.Route;

import java.util.List;

public interface RouteDAO {
    void insert(Route route);

    boolean exists(int routeId, int routeSeq);

    List<Route> getRoutes(int routeId, int routeSeq);

    List<Integer> getRouteSeq(int routeId);
}
