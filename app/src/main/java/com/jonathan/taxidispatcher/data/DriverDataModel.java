package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.DriverTransactionType;
import com.jonathan.taxidispatcher.data.model.StandardResponse;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverDataModel {
    private APIInterface apiService;

    @Inject
    public DriverDataModel(APIInterface apiService) {
        this.apiService = apiService;
    }

    public void driverSetOccupied(Integer id, Integer occupied, String requirement, OnDataReadyInterface callback) {
        apiService.setOccupied(id, occupied, requirement)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        callback.onDataReadyCallBack(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        callback.onDataReadyCallBack(new ApiResponse<StandardResponse>(t));
                    }
                });
    }

    public LiveData<ApiResponse<DriverTransactionType>> findCurrentTransaction(Integer id) {
        MutableLiveData<ApiResponse<DriverTransactionType>> res = new MutableLiveData<>();
        apiService.checkDriverTransactionStatus(id)
                .enqueue(new Callback<DriverTransactionType>() {
                    @Override
                    public void onResponse(Call<DriverTransactionType> call, Response<DriverTransactionType> response) {
                        res.setValue(new ApiResponse<DriverTransactionType>(response));
                    }

                    @Override
                    public void onFailure(Call<DriverTransactionType> call, Throwable t) {
                        res.setValue(new ApiResponse<DriverTransactionType>(t));
                    }
                });
        return res;
    }

    public interface OnDataReadyInterface {
        public void onDataReadyCallBack(ApiResponse<StandardResponse> response);
    }
}
