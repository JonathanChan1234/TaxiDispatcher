package com.jonathan.taxidispatcher.ui.passenger_rideshare;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

public class PassengerRideShareViewModel extends ViewModel {
    public TransactionRepository transactionRepository;
    public LocationRepository locationRepository;

    public MutableLiveData<Integer> transactionID = new MutableLiveData<>();
    public LiveData<ApiResponse<RideShareResource>> transaction;
    public LiveData<ApiResponse<RideSharePairingResponse>> pairing;
    public SingleLiveEvent<ApiResponse<StandardResponse>> cancelResponse;

    public MutableLiveData<RideShare> res = new MutableLiveData<>();
    public MutableLiveData<RideShareTransaction> shareRideTransaction = new MutableLiveData<>();

    LatLng destination;
    public LiveData<ApiResponse<DirectionModel>> route;
    public MutableLiveData<LatLng> origin = new MutableLiveData<>();

    public PassengerRideShareViewModel(TransactionRepository transactionRepository,
                                       LocationRepository locationRepository) {
        super();
        this.transactionRepository = transactionRepository;
        this.locationRepository = locationRepository;
        transaction = Transformations.switchMap(transactionID, transactionRepository::checkShareRideStatus);
        route = Transformations.switchMap(origin, input -> locationRepository.searchRoute(input, destination));
    }

    public void searchRoute(LatLng position) {
        if(getRideShare() != null) {
            destination = new LatLng(Double.parseDouble(getRideShare().startLat),
                    Double.parseDouble(getRideShare().startLong));
            origin.setValue(position);
        }
    }

    public void trackLocation(LatLng position) {
        if(getRideShare() != null) {
            destination = new LatLng(Double.parseDouble(getRideShare().desLat),
                    Double.parseDouble(getRideShare().desLong));
            origin.setValue(position);
        }
    }

    public LiveData<ApiResponse<DirectionModel>> getRoute() {
        return route;
    }

    public void setCurrentTransactionStatus(Integer transactionId) {
        this.transactionID.setValue(transactionId);
    }

    public LiveData<ApiResponse<RideShareResource>> getCurrentTransactionStatus() {
        return transaction;
    }

    public void setRideShare(RideShare rideShare) {
        res.setValue(rideShare);
    }

    public RideShare getRideShare()  {
        return res.getValue();
    }

    public void setRideShareTranscation(RideShareTransaction transcation) {
        shareRideTransaction.setValue(transcation);
    }

    public RideShareTransaction getRideShareTranscation() {
        return shareRideTransaction.getValue();
    }

    public LiveData<ApiResponse<StandardResponse>> cancelShareRideOrder(Integer id) {
        cancelResponse = transactionRepository.cancelShareRideOrder(id);
        return cancelResponse;
    }

    public LiveData<ApiResponse<RideSharePairingResponse>> getPairing() {
        if(res.getValue() != null) {
            pairing = transactionRepository.checkPairing(res.getValue().id);
            return pairing;
        }
        return null;
    }
}
