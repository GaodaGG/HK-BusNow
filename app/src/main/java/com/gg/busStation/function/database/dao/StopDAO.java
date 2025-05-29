package com.gg.busStation.function.database.dao;

import com.gg.busStation.data.bus.Stop;

public interface StopDAO {
    boolean exists(int stopId);

    void insert(Stop stop);

    Stop getStop(int stopId);
}

