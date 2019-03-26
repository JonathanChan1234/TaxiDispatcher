package com.jonathan.taxidispatcher.event;

public class TranscationCompletedEvent {
    private int response;
    public TranscationCompletedEvent(int response) {
        this.response = response;
    }

    public int getResponse() {
        return response;
    }
}
