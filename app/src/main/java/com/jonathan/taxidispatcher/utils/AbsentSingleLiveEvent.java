package com.jonathan.taxidispatcher.utils;

public class AbsentSingleLiveEvent<T> extends SingleLiveEvent {
    public AbsentSingleLiveEvent() {
        postValue(null);
    }

    public static <T> AbsentSingleLiveEvent<T> create() {
        return new AbsentSingleLiveEvent();
    }
}
