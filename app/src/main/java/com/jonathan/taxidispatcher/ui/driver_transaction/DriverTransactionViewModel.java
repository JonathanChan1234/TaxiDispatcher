package com.jonathan.taxidispatcher.ui.driver_transaction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.DriverDataModel;
import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TaxiRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.DriverTransactionType;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.RideShareTransactionResource;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;


public class DriverTransactionViewModel extends ViewModel {
    // Event Response
    private SingleLiveEvent<ApiResponse<StandardResponse>> startOnServeResponse = new SingleLiveEvent<>();
    private SingleLiveEvent<ApiResponse<StandardResponse>> signOutTaxiResponse = new SingleLiveEvent<>();
    private SingleLiveEvent<ApiResponse<StandardResponse>> driverReachResponse = new SingleLiveEvent<>();

    // Data Store
    private MutableLiveData<Boolean> onServe = new MutableLiveData<>();
    private MutableLiveData<String> rideType = new MutableLiveData<>();

    // Personal Ride
    private MutableLiveData<Transcation> transcation = new MutableLiveData<>();

    // Share Ride
    private MutableLiveData<RideShareTransaction> rideShare = new MutableLiveData<>();

    // Repository
    private LocationRepository locationRepository;
    private DriverDataModel driverDataModel;
    private TaxiRepository taxiRepository;
    private TransactionRepository transactionRepository;

    //Driver status
    private MutableLiveData<Integer> driverId = new MutableLiveData<>();
    private LiveData<ApiResponse<DriverTransactionType>> driverStatus;
    private MutableLiveData<Integer> transactionId = new MutableLiveData<>();
    private LiveData<ApiResponse<TranscationResource>> transcationResource = new MutableLiveData<>();
    private LiveData<ApiResponse<RideShareTransactionResource>> rideShareResource = new MutableLiveData<>();

    public DriverTransactionViewModel(LocationRepository locationRepository,
                                      DriverDataModel driverDataModel,
                                      TaxiRepository taxiRepository,
                                      TransactionRepository transactionRepository) {
        super();
        this.locationRepository = locationRepository;
        this.driverDataModel = driverDataModel;
        this.taxiRepository  = taxiRepository;
        this.transactionRepository = transactionRepository;

        driverStatus = Transformations.switchMap(driverId, driverDataModel::findCurrentTransaction);
    }

    public void setOccupied(Integer id, Integer occupied, Integer requirement, Integer location) {
        driverDataModel.driverSetOccupied(id, occupied, requirement, location, response -> {
            startOnServeResponse.setValue(response);
        });
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> getStartOnServeResponse() {
        return startOnServeResponse;
    }

    public void signoutTaxi(int taxiID, int driverID) {
        taxiRepository.signoutTaxi(taxiID, driverID, response -> {
            signOutTaxiResponse.setValue(response);
        });
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> getSignOutTaxiResponse() {
        return signOutTaxiResponse;
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> driverReachPickup(Integer id) {
        driverReachResponse = transactionRepository.driverReachPickupPoint(id);
        return driverReachResponse;
    }

    public void checkDriverStatus(Integer id) {
        driverId.setValue(id);
    }

    public LiveData<ApiResponse<DriverTransactionType>> getDriverStatus() {
        return driverStatus;
    }

    public LiveData<ApiResponse<TranscationResource>> getTranscationResource(Integer id){
        transcationResource = transactionRepository.searchForRecentTransaction(id);
        return transcationResource;
    }

    public void setTranscation(Transcation transcation) {
        this.transcation.setValue(transcation);
    }

    public Transcation getTranscation() {
        return transcation.getValue();
    }

    public LiveData<ApiResponse<RideShareTransactionResource>> getRideShareResource(Integer id) {
        rideShareResource = transactionRepository.driverCheckRideShareStatus(id);
        return rideShareResource;
    }

    public void setRideShareResource(RideShareTransaction rideShareResource) {
        rideShare.setValue(rideShareResource);
    }

    public RideShareTransaction getRideShareResource() {
        return rideShare.getValue();
    }

    public void setOnServe(Boolean onServe) {
        this.onServe.setValue(onServe);
    }

    public Boolean getOnServe() {
        return onServe.getValue();
    }

    public void setRideType(String type) {
        this.rideType.setValue(type);
    }

    public String getRideType() {
        return rideType.getValue();
    }
}
