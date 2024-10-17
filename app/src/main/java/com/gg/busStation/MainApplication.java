package com.gg.busStation;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
