package com.gg.busStation.data.reminder;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 提醒数据类
 * 用于存储上车提醒和到站提醒的相关信息
 */
@Getter
@Setter
public class ReminderData implements Serializable {
    public static final int TYPE_BOARDING = 0;  // 上车提醒

    private int id;
    private int reminderType;       // 提醒类型
    private String routeName;       // 路线名称
    private String stopName;        // 站点名称
    private String co;              // 公司代码
    private int routeId;            // 路线ID
    private int bound;              // 方向
    private int stopSeq;            // 站点序号
    private int reminderMinutes;    // 提前多少分钟提醒
    private Date etaTime;           // 预计到站时间
    private boolean isActive;       // 是否激活

    public ReminderData() {
        this.isActive = true;
    }

    public ReminderData(int reminderType, String routeName, String stopName, String co,
                        int routeId, int bound, int stopSeq, int reminderMinutes, Date etaTime) {
        this.reminderType = reminderType;
        this.routeName = routeName;
        this.stopName = stopName;
        this.co = co;
        this.routeId = routeId;
        this.bound = bound;
        this.stopSeq = stopSeq;
        this.reminderMinutes = reminderMinutes;
        this.etaTime = etaTime;
        this.isActive = true;
        this.id = generateId();
    }

    private int generateId() {
        return (routeId + "_" + bound + "_" + stopSeq + "_" + reminderType).hashCode();
    }

    public String getNotificationTitle() {
        return "上车提醒 - " + routeName;
    }

    public String getNotificationText() {
        return "巴士即将到达 " + stopName + "，请准备上车";
    }
}
