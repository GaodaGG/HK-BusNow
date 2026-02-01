package com.gg.busStation.function.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.reminder.ReminderData;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 提醒管理器
 * 负责创建、管理和取消提醒
 */
public class ReminderManager {
    private static final String TAG = "ReminderManager";
    private static ReminderManager instance;
    private final Context context;
    private final Map<Integer, ReminderData> activeReminders;
    private final AlarmManager alarmManager;

    public static final String ACTION_REMINDER = "com.gg.busStation.ACTION_REMINDER";
    public static final String EXTRA_REMINDER_DATA = "reminder_data";

    private ReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.activeReminders = new HashMap<>();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static synchronized ReminderManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReminderManager(context);
        }
        return instance;
    }

    /**
     * 创建上车提醒
     * @param routeName 路线名称
     * @param stopName 站点名称
     * @param co 公司代码
     * @param routeId 路线ID
     * @param bound 方向
     * @param stopSeq 站点序号
     * @param eta ETA数据
     * @param minutesBefore 提前多少分钟提醒
     * @return 是否创建成功
     */
    public boolean createBoardingReminder(String routeName, String stopName, String co,
                                          int routeId, int bound, int stopSeq,
                                          ETA eta, int minutesBefore) {
        if (eta == null || eta.getTime() == null) {
            Log.e(TAG, "ETA data is null");
            return false;
        }

        ReminderData reminderData = new ReminderData(
                ReminderData.TYPE_BOARDING,
                routeName,
                stopName,
                co,
                routeId,
                bound,
                stopSeq,
                minutesBefore,
                eta.getTime()
        );

        return scheduleReminder(reminderData);
    }


    /**
     * 安排提醒
     */
    private boolean scheduleReminder(ReminderData reminderData) {
        try {
            // 计算提醒时间
            long etaTimeMillis = reminderData.getEtaTime().getTime();
            long reminderTimeMillis = etaTimeMillis - (reminderData.getReminderMinutes() * 60 * 1000L);
            long currentTimeMillis = System.currentTimeMillis();

            // 如果提醒时间已过，直接返回失败
            if (reminderTimeMillis <= currentTimeMillis) {
                Log.w(TAG, "Reminder time has already passed");
                return false;
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction(ACTION_REMINDER);
            intent.putExtra(EXTRA_REMINDER_DATA, reminderData);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderData.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 设置精确闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                } else {
                    // 没有精确闹钟权限，使用非精确闹钟
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                );
            }

            activeReminders.put(reminderData.getId(), reminderData);
            Log.d(TAG, "Reminder scheduled: " + reminderData.getNotificationTitle() +
                    " at " + new Date(reminderTimeMillis));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule reminder", e);
            return false;
        }
    }

    /**
     * 取消提醒
     */
    public void cancelReminder(int reminderId) {
        ReminderData reminderData = activeReminders.get(reminderId);
        if (reminderData != null) {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction(ACTION_REMINDER);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            activeReminders.remove(reminderId);
            Log.d(TAG, "Reminder cancelled: " + reminderId);
        }
    }

    /**
     * 取消所有提醒
     */
    public void cancelAllReminders() {
        for (Integer reminderId : activeReminders.keySet()) {
            cancelReminder(reminderId);
        }
        activeReminders.clear();
    }

    /**
     * 检查是否有激活的提醒
     */
    public boolean hasActiveReminder(int routeId, int bound, int stopSeq, int reminderType) {
        for (ReminderData data : activeReminders.values()) {
            if (data.getRouteId() == routeId &&
                    data.getBound() == bound &&
                    data.getStopSeq() == stopSeq &&
                    data.getReminderType() == reminderType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取活跃的提醒数量
     */
    public int getActiveReminderCount() {
        return activeReminders.size();
    }

    /**
     * 提醒已触发，从活跃列表中移除
     */
    public void onReminderTriggered(int reminderId) {
        activeReminders.remove(reminderId);
    }
}
