package com.jonathan.taxidispatcher.utils;

import android.arch.lifecycle.LiveData;

public class AbsentLiveData extends LiveData {
    public AbsentLiveData() {
        postValue(null);
    }

    public static <T> LiveData<T> create() {
        return new AbsentLiveData();
    }
}
