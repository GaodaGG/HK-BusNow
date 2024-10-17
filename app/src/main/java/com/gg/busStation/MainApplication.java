package com.gg.busStation;

import android.app.Application;

import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.location.LocationHelper;
import com.google.android.material.color.DynamicColors;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this);

        initData();
    }

    private void initData() {
        DataBaseManager.initDB(this);
        LocationHelper.init(this);
    }
}
