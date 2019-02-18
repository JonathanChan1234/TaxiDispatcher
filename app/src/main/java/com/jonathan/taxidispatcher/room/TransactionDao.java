package com.jonathan.taxidispatcher.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.jonathan.taxidispatcher.data.model.Transcation;

import java.util.List;

@Dao
public abstract class TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTransaction(List<Transcation> transcations);

    @Query("SELECT * FROM Transcation")
    public abstract LiveData<List<Transcation>> loadTransactionHistory();

    @Query("SELECT * FROM Transcation WHERE id in (:transcationId)")
    public abstract LiveData<Transcation> loadTransactionById(Integer transcationId);

    @Query("UPDATE Transcation SET status = :status, driver_id = :driverId, driver_phonenumber = :phonenumber" +
            ", driver_username = :username, driver_email = :email WHERE id=:transcationId")
    public abstract void updateTranscationStatus(Integer status, Integer driverId,
                                                 String phonenumber, String username,
                                                 String email, Integer transcationId);
}
