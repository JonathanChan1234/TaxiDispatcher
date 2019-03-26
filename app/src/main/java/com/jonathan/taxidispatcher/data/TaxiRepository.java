package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.MutableLiveData;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.TaxiSignInResponse;
import com.jonathan.taxidispatcher.data.model.Taxis;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TaxiRepository {
    private APIInterface apiService;

    @Inject
    public TaxiRepository(APIInterface apiService) {
        this.apiService = apiService;
    }

    public void taxiLogIn(String phoneNumber, String password, Integer driverId, SignInResponseCallBack callBack) {
        apiService.signInTaxi(phoneNumber, password, driverId)
                .enqueue(new Callback<TaxiSignInResponse>() {
                    @Override
                    public void onResponse(Call<TaxiSignInResponse> call, Response<TaxiSignInResponse> response) {
                        callBack.onCallBack(new ApiResponse<TaxiSignInResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<TaxiSignInResponse> call, Throwable t) {
                        callBack.onCallBack(new ApiResponse<TaxiSignInResponse>(t));
                    }
                });
    }

    public MutableLiveData<ApiResponse<Taxis>> getOwnedTaxi(Integer id) {
        MutableLiveData<ApiResponse<Taxis>> res = new MutableLiveData<>();
        apiService.getTaxiList(id)
                .enqueue(new Callback<Taxis>() {
                    @Override
                    public void onResponse(Call<Taxis> call, Response<Taxis> response) {
                        res.setValue(new ApiResponse<Taxis>(response));
                    }

                    @Override
                    public void onFailure(Call<Taxis> call, Throwable t) {
                        res.setValue(new ApiResponse<Taxis>(t));
                    }
                });
        return res;
    }

    public void registerNewTaxiAccount(Integer id,
                                       String plateNumber,
                                       String password,
                                       OnDataReadyInterface onDataReadyInterface) {
        apiService.registerNewTaxi(plateNumber, password, id)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(t));
                    }
                });
    }

    public void deleteTaxiAccount(String plateNumber, String password, OnDataReadyInterface onDataReadyInterface) {
        apiService.deleteTaxiAccount(plateNumber, password)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(t));
                    }
                });
    }

    public void signoutTaxi(Integer taxiID, Integer driverID, OnDataReadyInterface onDataReadyInterface) {
        apiService.signoutTaxi(taxiID, driverID)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        onDataReadyInterface.onDataReady(new ApiResponse<StandardResponse>(t));
                    }
                });
    }

    public interface OnDataReadyInterface {
        public void onDataReady(ApiResponse<StandardResponse> response);
    }

    public interface SignInResponseCallBack {
        public void onCallBack(ApiResponse<TaxiSignInResponse> response);
    }
}
