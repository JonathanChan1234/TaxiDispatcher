package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.api.GoogleAPIInterface;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.PlaceResource;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class LocationRepository {
    private GoogleAPIInterface googleAPIService;
    @Inject
    public LocationRepository(GoogleAPIInterface service) {
        this.googleAPIService = service;
    }

    public SingleLiveEvent<ApiResponse<PlaceResource>> searchPlace(LatLng position) {
        SingleLiveEvent<ApiResponse<PlaceResource>> places = new SingleLiveEvent<>();
        googleAPIService.getNearbyPlace("zh-TW", String.valueOf(position.latitude) + "," + String.valueOf(position.longitude),
                "50", Constants.API_key)
                .enqueue(new Callback<PlaceResource>() {
                    @Override
                    public void onResponse(Call<PlaceResource> call, Response<PlaceResource> response) {
                        places.setValue(new ApiResponse<PlaceResource>(response));
                    }

                    @Override
                    public void onFailure(Call<PlaceResource> call, Throwable t) {
                        places.setValue(new ApiResponse<PlaceResource>(t));
                    }
                });
        return places;
    }

    public SingleLiveEvent<ApiResponse<DirectionModel>> searchRoute(LatLng origin, LatLng destination) {
        SingleLiveEvent<ApiResponse<DirectionModel>> route = new SingleLiveEvent<>();
        googleAPIService.getRoute(origin.latitude + "," + origin.longitude,
                destination.latitude + "," + destination.longitude,
                Constants.API_key)
                .enqueue(new Callback<DirectionModel>() {
                    @Override
                    public void onResponse(Call<DirectionModel> call, Response<DirectionModel> response) {
                        route.setValue(new ApiResponse<DirectionModel>(response));
                    }

                    @Override
                    public void onFailure(Call<DirectionModel> call, Throwable t) {
                        route.setValue(new ApiResponse<DirectionModel>(t));
                    }
                });
        return route;
    }
}
