package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TransactionRepository {
    private APIInterface apiService;

    @Inject
    public TransactionRepository(APIInterface apiService) {
        this.apiService = apiService;
    }

    public SingleLiveEvent<ApiResponse<TranscationResource>> makeTransaction(
            Integer userid,
            Double start_lat,
            Double start_long,
            String start_addr,
            Double des_lat,
            Double des_long,
            String des_arr,
            String meet_up_time,
            String requirement) {
        SingleLiveEvent<ApiResponse<TranscationResource>> transaction = new SingleLiveEvent<>();
        apiService.startTranscation(userid, start_lat, start_long, start_addr, des_lat, des_long, des_arr, meet_up_time, requirement)
                .enqueue(new Callback<TranscationResource>() {
                    @Override
                    public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {
                        transaction.setValue(new ApiResponse<TranscationResource>(response));
                    }

                    @Override
                    public void onFailure(Call<TranscationResource> call, Throwable t) {
                        transaction.setValue(new ApiResponse<TranscationResource>(t));
                    }
                });
        return transaction;
    }

    public LiveData<ApiResponse<TranscationResource>> searchForRecentTransaction(Integer id) {
        MutableLiveData<ApiResponse<TranscationResource>> transaction = new MutableLiveData<>();
        apiService.searchForRecentTranscation(id)
                .enqueue(new Callback<TranscationResource>() {
                    @Override
                    public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {
                        transaction.setValue(new ApiResponse<TranscationResource>(response));
                    }

                    @Override
                    public void onFailure(Call<TranscationResource> call, Throwable t) {
                        transaction.setValue(new ApiResponse<TranscationResource>(t));
                    }
                });
        return transaction;
    }
}
