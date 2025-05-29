package com.gg.busStation.function.database.dao;

import androidx.annotation.NonNull;

public interface SettingsDAO {
    boolean exists(String key);

    void insert(String key, String value);

    String getValue(String key, @NonNull String defaultValue);
}
