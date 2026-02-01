package com.gg.busStation.function.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.gg.busStation.R;
import com.gg.busStation.data.reminder.ReminderData;
import com.gg.busStation.ui.activity.MainActivity;

/**
 * 提醒广播接收器
 * 接收提醒触发并显示通知
 */
public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    public static final String CHANNEL_ID_BOARDING = "Boarding_Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ReminderManager.ACTION_REMINDER.equals(intent.getAction())) {
            return;
        }

        ReminderData reminderData;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            reminderData = intent.getSerializableExtra(
                    ReminderManager.EXTRA_REMINDER_DATA, ReminderData.class);
        } else {
            reminderData = (ReminderData) intent.getSerializableExtra(
                    ReminderManager.EXTRA_REMINDER_DATA);
        }

        if (reminderData == null) {
            Log.e(TAG, "Reminder data is null");
            return;
        }

        Log.d(TAG, "Reminder triggered: " + reminderData.getNotificationTitle());

        // 显示通知
        showNotification(context, reminderData);

        // 通知 ReminderManager 提醒已触发
        ReminderManager.getInstance(context).onReminderTriggered(reminderData.getId());
    }

    private void showNotification(Context context, ReminderData reminderData) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知渠道
        createNotificationChannel(context, notificationManager);

        // 创建点击通知后的 Intent
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reminderData.getId(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID_BOARDING)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
                .setContentTitle(reminderData.getNotificationTitle())
                .setContentText(reminderData.getNotificationText())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH);

        // 设置大文本样式以显示更多信息
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                .bigText(reminderData.getNotificationText() +
                        "\n路线: " + reminderData.getRouteName() +
                        "\n站点: " + reminderData.getStopName());
        builder.setStyle(bigTextStyle);

        // 发送通知
        notificationManager.notify(reminderData.getId(), builder.build());
    }

    private void createNotificationChannel(Context context, NotificationManager manager) {
        String channelName = context.getString(R.string.notification_channel_boarding_reminder);
        String channelDescription = context.getString(R.string.notification_channel_boarding_reminder_desc);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID_BOARDING,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(channelDescription);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});

        // 设置提示音
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);

        manager.createNotificationChannel(channel);
    }
}
