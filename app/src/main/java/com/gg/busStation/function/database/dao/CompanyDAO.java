package com.gg.busStation.function.database.dao;

import com.gg.busStation.data.bus.CompanyData;

public interface CompanyDAO {
    boolean exists(String code);
    void insert(CompanyData company);
}
