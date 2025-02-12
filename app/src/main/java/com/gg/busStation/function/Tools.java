package com.gg.busStation.function;

import android.content.Context;

public class Tools {
    private Tools() {
    }

    public static int dp2px(Context context, int i) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (i * scale + 0.5f);
    }
}
