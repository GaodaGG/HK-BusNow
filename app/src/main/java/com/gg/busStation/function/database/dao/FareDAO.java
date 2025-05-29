package com.gg.busStation.function.database.dao;

public interface FareDAO {
    boolean exists(int routeId, int routeSeq);

    void insert(int routeId,int routeSeq, String fare);

    String getFare(int routeId, int routeSeq);
}
