package com.gg.busStation.function;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Tools {
    private Tools() {
    }

    public static int dp2px(Context context, int i) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (i * scale + 0.5f);
    }

    public static boolean isMIUI() {
        String miuiName;
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class);
            miuiName = (String) get.invoke(clazz, "ro.miui.ui.version.name");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            return false;
        }

        return miuiName != null && !miuiName.isEmpty();
    }
}
