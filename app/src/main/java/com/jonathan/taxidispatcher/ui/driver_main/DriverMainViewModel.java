package com.jonathan.taxidispatcher.ui.driver_main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;

import com.jonathan.taxidispatcher.api.APIClient;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.TaxiRepository;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Taxis;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

public class DriverMainViewModel extends ViewModel {
    private SingleLiveEvent<ApiResponse<StandardResponse>> signInResponse = new SingleLiveEvent<>();
    private SingleLiveEvent<ApiResponse<StandardResponse>> deleteAccountResponse = new SingleLiveEvent<>();
    private SingleLiveEvent<ApiResponse<StandardResponse>> registerAccountResponse = new SingleLiveEvent<>();
    public MutableLiveData<ApiResponse<Taxis>> taxiList = new MutableLiveData<>();
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    private TaxiRepository taxiRepository;

    public DriverMainViewModel(TaxiRepository taxiRepository) {
        super();
        this.taxiRepository = taxiRepository;
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> taxiSignIn(String plateNumber, String password, Integer id) {
        isLoading.set(true);
        taxiRepository.taxiLogIn(plateNumber, password, id, response -> {
            signInResponse.setValue(response);
            isLoading.set(false);
        });
        return signInResponse;
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> registerNewTaxi(Integer id,
                                                                          String plateNumber,
                                                                          String password) {
        isLoading.set(true);
        taxiRepository.registerNewTaxiAccount(id,
                plateNumber,
                password,
                response -> {
                    registerAccountResponse.setValue(response);
                    isLoading.set(false);
                });
        return registerAccountResponse;
    }

    public SingleLiveEvent<ApiResponse<StandardResponse>> deleteTaxiAccount(String plateNumber, String password) {
        isLoading.set(true);
        taxiRepository.deleteTaxiAccount(plateNumber, password, response -> {
            deleteAccountResponse.setValue(response);
            isLoading.set(false);
        });
        return deleteAccountResponse;
    }

    public MutableLiveData<ApiResponse<Taxis>> getTaxiList(Integer id) {
        taxiList = taxiRepository.getOwnedTaxi(id);
        return taxiList;
    }
}
