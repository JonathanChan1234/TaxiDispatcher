package com.jonathan.taxidispatcher.event;

public class DriverReachTimerEvent {
    private int minute;
    private int second;

    public DriverReachTimerEvent(int minute, int second) {
        this.minute = minute;
        this.second = second;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }
}
