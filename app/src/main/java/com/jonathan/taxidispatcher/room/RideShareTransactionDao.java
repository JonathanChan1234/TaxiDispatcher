package com.jonathan.taxidispatcher.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.jonathan.taxidispatcher.data.model.RideShareTransaction;

import java.util.List;

@Dao
public abstract class RideShareTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRideShareTransaction(List<RideShareTransaction> transactions);

    @Query("SELECT * FROM RideShareTransaction")
    public abstract LiveData<List<RideShareTransaction>> loadRideShareHistory();
}
