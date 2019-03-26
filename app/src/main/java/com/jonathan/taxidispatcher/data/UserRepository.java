package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.MutableLiveData;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class UserRepository {
    private APIInterface apiService;

    @Inject
    public UserRepository(APIInterface apiService) {
         this.apiService = apiService;
    }

    public MutableLiveData<ApiResponse<AccountUserResponse>> passengerLogIn(String phoneNumber, String password, String token) {
        MutableLiveData<ApiResponse<AccountUserResponse>> res = new MutableLiveData<>();
        apiService.passengerSignIn(phoneNumber, password, token)
                .enqueue(new Callback<AccountUserResponse>() {
                    @Override
                    public void onResponse(Call<AccountUserResponse> call, Response<AccountUserResponse> response) {
                        res.setValue(new ApiResponse<AccountUserResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<AccountUserResponse> call, Throwable t) {
                        res.setValue(new ApiResponse<AccountUserResponse>(t));
                    }
                });
        return res;
    }

    public MutableLiveData<ApiResponse<AccountUserResponse>> passengerRegister(
            String username, String password, String phoneNumber, String email, String img, String token) {
        MutableLiveData<ApiResponse<AccountUserResponse>> res = new MutableLiveData<>();
        apiService.passengerCreateAccount(username, password, phoneNumber, email, img, token)
                .enqueue(new Callback<AccountUserResponse>() {
                    @Override
                    public void onResponse(Call<AccountUserResponse> call, Response<AccountUserResponse> response) {
                        res.setValue(new ApiResponse<AccountUserResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<AccountUserResponse> call, Throwable t) {
                        res.setValue(new ApiResponse<AccountUserResponse>(t));
                    }
                });
        return res;
    }

    public MutableLiveData<ApiResponse<AccountDriverResponse>> driverLogIn(String phoneNumber, String password, String token) {
        MutableLiveData<ApiResponse<AccountDriverResponse>> res = new MutableLiveData<>();
        apiService.driverSignIn(phoneNumber, password, token)
                .enqueue(new Callback<AccountDriverResponse>() {
                    @Override
                    public void onResponse(Call<AccountDriverResponse> call, Response<AccountDriverResponse> response) {
                        res.setValue(new ApiResponse<AccountDriverResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<AccountDriverResponse> call, Throwable t) {
                        res.setValue(new ApiResponse<AccountDriverResponse>(t));
                    }
                });
        return res;
    }

    public MutableLiveData<ApiResponse<AccountDriverResponse>> driverRegister(
            String username, String password, String phoneNumber, String email, String img, String token) {
        MutableLiveData<ApiResponse<AccountDriverResponse>> res = new MutableLiveData<>();
        apiService.driverCreateAccount(username, password, phoneNumber, email, img, token)
                .enqueue(new Callback<AccountDriverResponse>() {
                    @Override
                    public void onResponse(Call<AccountDriverResponse> call, Response<AccountDriverResponse> response) {
                        res.setValue(new ApiResponse<AccountDriverResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<AccountDriverResponse> call, Throwable t) {
                        res.setValue(new ApiResponse<AccountDriverResponse>(t));
                    }
                });
        return res;
    }
}
