package com.jonathan.taxidispatcher.event;

public class TimerEvent {
    private int minute;
    private int second;

    public TimerEvent(int minute, int second) {
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
