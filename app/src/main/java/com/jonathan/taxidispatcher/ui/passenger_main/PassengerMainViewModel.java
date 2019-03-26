package com.jonathan.taxidispatcher.ui.passenger_main;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.jonathan.taxidispatcher.api.APIClient;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.api.GoogleMapAPIClient;
import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.PlaceResource;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.utils.AbsentLiveData;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PassengerMainViewModel extends ViewModel {
    private LiveData<ApiResponse<PlaceResource>> pickupPointResponse;
    private LiveData<ApiResponse<PlaceResource>> destinationPointResponse;
    private LiveData<ApiResponse<DirectionModel>> routeData;
    private LiveData<ApiResponse<TranscationResource>> transactionResource = new MutableLiveData<>();
    public SingleLiveEvent<ApiResponse<RideShareResource>> rideShareResource = new SingleLiveEvent<>();

    private MutableLiveData<LatLng> pickUpPoint = new MutableLiveData<>();
    private MutableLiveData<LatLng> destinationPoint = new MutableLiveData<>();

    public final ObservableBoolean isLoading = new ObservableBoolean(false);

    // transition data
    public MutableLiveData<Bundle> transcationData = new MutableLiveData<>();
    private LocationRepository locationRepository;
    private TransactionRepository transactionRepository;

    public PassengerMainViewModel(LocationRepository locationRepository, TransactionRepository transactionRepository) {
        super();
        this.locationRepository = locationRepository;
        this.transactionRepository = transactionRepository;
        pickupPointResponse = Transformations.switchMap(pickUpPoint, new Function<LatLng, LiveData<ApiResponse<PlaceResource>>>() {
            @Override
            public LiveData<ApiResponse<PlaceResource>> apply(LatLng input) {
                isLoading.set(false);
                if(input == null) {
                    return AbsentLiveData.create();
                } else {
                    return locationRepository.searchPlace(input);
                }
            }
        });
        destinationPointResponse = Transformations.switchMap(destinationPoint, new Function<LatLng, LiveData<ApiResponse<PlaceResource>>>() {
            @Override
            public LiveData<ApiResponse<PlaceResource>> apply(LatLng input) {
                isLoading.set(false);
                if(input == null) {
                    return AbsentLiveData.create();
                } else {
                    return locationRepository.searchPlace(input);
                }
            }
        });
    }

    public void searchPickupPoint(LatLng latLng) {
        isLoading.set(true);
        pickUpPoint.setValue(latLng);
    }

    public void searchDestination(LatLng latLng) {
        isLoading.set(true);
        destinationPoint.setValue(latLng);
    }

    public LiveData<ApiResponse<PlaceResource>> getPickupPointResponse() {
        return pickupPointResponse;
    }

    public LiveData<ApiResponse<PlaceResource>> getDestinationPointResponse() {
        return destinationPointResponse;
    }

    public LiveData<Bundle> getTranscationData() {
        return transcationData;
    }

    public LiveData<ApiResponse<DirectionModel>> getRouteData(LatLng origin, LatLng direction) {
        routeData = locationRepository.searchRoute(origin, direction);
        return routeData;
    }

    public LiveData<ApiResponse<TranscationResource>> makeTransaction(Integer userid,
                                                                      Double start_lat,
                                                                      Double start_long,
                                                                      String start_addr,
                                                                      Double des_lat,
                                                                      Double des_long,
                                                                      String des_arr,
                                                                      String meet_up_time,
                                                                      String requirement) {
        transactionResource = transactionRepository.makeTransaction(userid, start_lat, start_long, start_addr, des_lat, des_long,
                des_arr, meet_up_time, requirement);
        return transactionResource;
    }

    public SingleLiveEvent<ApiResponse<RideShareResource>> makeRideShare(Integer userid,
                                                                         Double start_lat,
                                                                         Double start_long,
                                                                         String start_addr,
                                                                         Double des_lat,
                                                                         Double des_long,
                                                                         String des_addr) {
        rideShareResource = transactionRepository.makeShareRide(userid, start_lat, start_long, start_addr, des_lat, des_long, des_addr);
        return rideShareResource;
    }

}