package com.jonathan.taxidispatcher.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.jonathan.taxidispatcher.data.model.Driver;

import java.util.List;

@Dao
public abstract class DriverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertDriver(List<Driver> drivers);
}
