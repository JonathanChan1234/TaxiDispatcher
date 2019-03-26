package com.jonathan.taxidispatcher.room;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.model.Transcation;

@Database(entities = {Transcation.class}, version = 7)
public abstract class TaxiDb extends RoomDatabase {
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Transcation" +
                    " ADD COLUMN TYPE TEXT" );
        }
    };
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Transcation" +
                    " ADD COLUMN CANCELLED INT" );
        }
    };

    public static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Transcation" +
                    " ADD COLUMN TYPE TEXT" );
            database.execSQL("ALTER TABLE Transcation" +
                    " ADD COLUMN CANCELLED INT" );

        }
    };

    public static TaxiDb getDb(Context context) {
        return Room.databaseBuilder(context, TaxiDb.class, "taxidispatcher.db")
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                .build();
    }

    abstract public TransactionDao transactionDao();
//    abstract public DriverDao driverDao();
//    abstract public RideShareTransactionDao rideShareTransactionDao();
}
