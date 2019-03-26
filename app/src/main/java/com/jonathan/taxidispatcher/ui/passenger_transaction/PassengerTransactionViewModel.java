package com.jonathan.taxidispatcher.ui.passenger_transaction;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

public class PassengerTransactionViewModel extends ViewModel {
    // Repository
    private LocationRepository locationRepository;
    private TransactionRepository transactionRepository;

    // Update transaction status
    private LiveData<ApiResponse<TranscationResource>> transactionStatus;
    private MutableLiveData<Integer> transactionId = new MutableLiveData<>();

    // Event
    private SingleLiveEvent<ApiResponse<StandardResponse>> cancelResponse = new SingleLiveEvent<>();

    // Data store (Driver and transaction)
    private MutableLiveData<Transcation> currentTranscation = new MutableLiveData<>();
    private MutableLiveData<Driver> driver = new MutableLiveData<>();

    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public MutableLiveData<LatLng> origin = new MutableLiveData<>();
    public LatLng destination;
    public LiveData<ApiResponse<DirectionModel>> route;

    public PassengerTransactionViewModel(LocationRepository locationRepository,
                                         TransactionRepository transactionRepository) {
        super();
        this.locationRepository = locationRepository;
        this.transactionRepository = transactionRepository;
        transactionStatus = Transformations.switchMap(transactionId, new Function<Integer, LiveData<ApiResponse<TranscationResource>>>() {
            @Override
            public LiveData<ApiResponse<TranscationResource>> apply(Integer input) {
                isLoading.setValue(false);
                return transactionRepository.searchForRecentTransaction(input);
            }
        });
        route = Transformations.switchMap(origin, new Function<LatLng, LiveData<ApiResponse<DirectionModel>>>() {
            @Override
            public LiveData<ApiResponse<DirectionModel>> apply(LatLng input) {
                return locationRepository.searchRoute(input, destination);
            }
        });
    }

    public void searchRoute(LatLng position) {
        isLoading.setValue(true);
        if(getCurrentTranscation() != null) {
            destination = new LatLng(Double.parseDouble(getCurrentTranscation().startLat),
                    Double.parseDouble(getCurrentTranscation().startLong));
            origin.setValue(position);
        }
    }

    public void trackLocation(LatLng position) {
        isLoading.setValue(true);
        if(getCurrentTranscation() != null) {
            destination = new LatLng(Double.parseDouble(getCurrentTranscation().desLat),
                    Double.parseDouble(getCurrentTranscation().desLong));
            origin.setValue(position);
        }
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> cancelTranscation(Integer id) {
        cancelResponse = transactionRepository.cancelOrder(id);
        return cancelResponse;
    }

    public LiveData<ApiResponse<DirectionModel>> getRoute() {
        return route;
    }

    // Update the transaction status
    public void searchForRecentTransaction(Integer id) {
        isLoading.setValue(true);
        transactionId.setValue(id);
    }

    public LiveData<ApiResponse<TranscationResource>> getTransactionStatus() {
        return transactionStatus;
    }

    //Save the data in the view model
    public Transcation getCurrentTranscation() {
        return currentTranscation.getValue();
    }

    public void setCurrentTranscation(Transcation transcation) {
        currentTranscation.setValue(transcation);
    }

    public void setTransactionDriver(Driver driver) {
        this.driver.setValue(driver);
    }

    public Driver getDriver() {
        return driver.getValue();
    }
}
