package com.jonathan.taxidispatcher.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleMapAPIClient {
    private static GoogleMapAPIClient mInstance = new GoogleMapAPIClient();
    private GoogleAPIInterface googleAPIService;
    private GoogleMapAPIClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

       googleAPIService = retrofit.create(GoogleAPIInterface.class);
    }

    public static GoogleAPIInterface getGoogleAPIService() {
        return mInstance.googleAPIService;
    }
}
