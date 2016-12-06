package com.tanmay.androidservices.models;

/**
 * Created by TaNMay on 20/06/16.
 */
public class AlarmItem {

    String name, time;
    int alarmId;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }
}
