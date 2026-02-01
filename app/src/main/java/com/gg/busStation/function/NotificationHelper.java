package com.gg.busStation.function;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.DrawableRes;

import com.gg.busStation.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class NotificationHelper {
    @lombok.Getter
    private static final String[] channelIDs = {"Site_Reminder"};
    private static final int[] channelNames = {R.string.notification_channel_site_reminder};
    private static final int[] channelImportances = {NotificationManager.IMPORTANCE_HIGH};

    private NotificationHelper() {
    }

    public static void postNotification(Activity activity, int notificationId, String channelId, String title, CharSequence text, @DrawableRes int resId) {
        if (checkPermission(activity)) {
            for (int i = 0; i < channelIDs.length; i++) {
                createChannel(activity, channelIDs[i], activity.getString(channelNames[i]), channelImportances[i]);
            }
        } else {
            registerPermission(activity);
        }

        Notification.Builder builder = new Notification.Builder(activity, channelId)
                .setSmallIcon(Icon.createWithResource(activity, resId))
                .setContentTitle(title)
                .setContentText(text);

        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    public static void registerPermission(Activity activity) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_permission_notification_message)
                .setNegativeButton(R.string.dialog_permission_decline, (dialog, which) -> {
                })
                .setPositiveButton(R.string.dialog_permission_accept, (dialog, which) -> launchResult(activity))
                .show();

    }

    private static void launchResult(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
    }

    public static void createChannel(Activity activity, String id, CharSequence name, int importance) {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    public static boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

}
