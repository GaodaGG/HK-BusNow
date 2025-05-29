package com.gg.busStation;

import android.app.Application;
import android.app.UiModeManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.location.LocationHelper;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        setDarkTheme();
        initData();
    }
    
    private void setDarkTheme(){
        String darkSetting = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_dark_theme", "auto");
        if (Build.VERSION.SDK_INT >= 31){
            UiModeManager uiModeManager = getSystemService(UiModeManager.class);
            switch (darkSetting) {
                case "on":
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                    break;
                case "off":
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                    break;
                default:
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                    break;
            }
        } else {
            switch (darkSetting) {
                case "on":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "off":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
        }
    }

    private void initData() {
        DataBaseHelper.getInstance(this);
//        DataBaseManager.initDB(this);
        LocationHelper.init(this);
    }
}
