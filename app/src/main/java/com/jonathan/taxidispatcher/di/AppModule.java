package com.jonathan.taxidispatcher.di;

import android.arch.persistence.room.Room;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.GoogleAPIInterface;
import com.jonathan.taxidispatcher.room.DriverDao;
import com.jonathan.taxidispatcher.room.RideShareTransactionDao;
import com.jonathan.taxidispatcher.room.TaxiDb;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.service.DriverSyncAdapter;
import com.jonathan.taxidispatcher.session.Session;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {

    @Provides
    @Singleton
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(HttpLoggingInterceptor interceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    GoogleAPIInterface provideGoogleAPIService(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(GoogleAPIInterface.class);
    }

    @Provides
    @Singleton
    APIInterface provideAPIService(OkHttpClient client, TaxiApp app) {
        return new Retrofit.Builder()
                .baseUrl("http://10.64.197.239:8000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(APIInterface.class);
    }

    @Provides
    @Singleton
    TaxiDb provideDb(TaxiApp app) {
        return TaxiDb.getDb(app);
    }


    @Provides
    @Singleton
    TransactionDao provideTransactionDao(TaxiDb db) {
        return db.transactionDao();
    }

    @Provides
    @Singleton
    RideShareTransactionDao provideRideShareTransactionDao(TaxiDb db) {
        return db.rideShareTransactionDao();
    }

//    @Providesl
//    @Singleton
//    DriverDao provideDriverDao(TaxiDb db) {
//        return db.driverDao();
//    }
}
