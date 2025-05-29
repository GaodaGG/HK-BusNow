package com.gg.busStation.function;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SettingsManager {
    private final SharedPreferences sharedPreferences;
    private static SettingsManager instance;

    private SettingsManager(Context context) {
//        sharedPreferences = context.getSharedPreferences("com.gg.busStation_preferences", Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setInit(boolean isInit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isInit", isInit);
        editor.apply();
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("settings_update_data_lastUpdateTime", lastUpdateTime);
        editor.apply();
    }

    public long getLastUpdateTime() {
        return sharedPreferences.getLong("settings_update_data_lastUpdateTime", 0);
    }

    public boolean isInit() {
        return sharedPreferences.getBoolean("isInit", false);
    }

    public void setUpdateApp(boolean isAutoUpdate) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("settings_update_app", isAutoUpdate);
        editor.apply();
    }

    public boolean isAutoUpdateApp() {
        return sharedPreferences.getBoolean("settings_update_app", true);
    }

    public long getUpdateDataCycleTime() {
        String cycleD = sharedPreferences.getString("settings_update_data_cycle", String.valueOf(7));
        return Long.parseLong(cycleD) * 24 * 60 * 60 * 1000;
    }
}
