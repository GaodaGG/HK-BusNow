package com.gg.busStation.function.database.dao;

import com.gg.busStation.data.bus.Route;

import java.util.List;

public interface HistoryDAO {
    void insert(int routeId, int routeSeq, boolean unPin);

    void delete(int routeId, int routeSeq);

    List<Route> getAllHistory();

    void pinHistory(int routeId, int routeSeq);

    void unpinHistory(int routeId, int routeSeq);

    int getPinnedIndex(int routeId, int routeSeq);
}
