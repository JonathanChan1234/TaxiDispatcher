package com.jonathan.taxidispatcher.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Transaction;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.data.model.RideShareTransactionResource;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.room.TaxiDb;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TransactionRepository {
    private APIInterface apiService;
    private TransactionDao dao;
    private TaxiDb db;

    @Inject
    public TransactionRepository(APIInterface apiService, TransactionDao dao, TaxiDb db) {
        this.apiService = apiService;
        this.dao = dao;
        this.db = db;
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
        apiService.startTranscation(userid, start_lat, start_long, start_addr,
                des_lat, des_long, des_arr, meet_up_time, requirement)
                .enqueue(new Callback<TranscationResource>() {
                    @Override
                    public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {
                        if(response.body() != null) {
                            //  Write the transaction record to the internal database
                            Thread databaseInsertThread = new Thread(()-> {
                                ArrayList<Transcation> list = new ArrayList<>();
                                list.add(response.body().data);
                                dao.insertTransaction(list);
                            });
                            databaseInsertThread.start();
                        }
                        transaction.setValue(new ApiResponse<TranscationResource>(response));
                    }

                    @Override
                    public void onFailure(Call<TranscationResource> call, Throwable t) {
                        transaction.setValue(new ApiResponse<TranscationResource>(t));
                    }
                });
        return transaction;
    }

    public SingleLiveEvent<ApiResponse<RideShareResource>> makeShareRide(
            Integer userid,
            Double start_lat,
            Double start_long,
            String start_addr,
            Double des_lat,
            Double des_long,
            String des_addr) {
        SingleLiveEvent<ApiResponse<RideShareResource>> res = new SingleLiveEvent<>();
        apiService.startRideShare(userid, start_lat, start_long, start_addr,
                des_lat, des_long, des_addr)
                .enqueue(new Callback<RideShareResource>() {
                    @Override
                    public void onResponse(Call<RideShareResource> call, Response<RideShareResource> response) {
                        res.setValue(new ApiResponse<RideShareResource>(response));
                    }

                    @Override
                    public void onFailure(Call<RideShareResource> call, Throwable t) {
                        res.setValue(new ApiResponse<RideShareResource>(t));
                    }
                });
        return res;
    }

    public LiveData<ApiResponse<RideShareResource>> checkShareRideStatus(Integer id) {
        MutableLiveData<ApiResponse<RideShareResource>> res = new MutableLiveData<>();
        apiService.checkRideShareStatus(id)
                .enqueue(new Callback<RideShareResource>() {
                    @Override
                    public void onResponse(Call<RideShareResource> call, Response<RideShareResource> response) {
                        res.setValue(new ApiResponse<RideShareResource>(response));
                    }

                    @Override
                    public void onFailure(Call<RideShareResource> call, Throwable t) {
                        res.setValue(new ApiResponse<RideShareResource>(t));
                    }
                });
        return res;
    }

    /**
     *
     * @param id
     * @return
     */
    public LiveData<ApiResponse<RideSharePairingResponse>> checkPairing(Integer id) {
        MutableLiveData<ApiResponse<RideSharePairingResponse>> res = new MutableLiveData<>();
        apiService.getShareRidePairing(id)
                .enqueue(new Callback<RideSharePairingResponse>() {
                    @Override
                    public void onResponse(Call<RideSharePairingResponse> call, Response<RideSharePairingResponse> response) {
                        res.setValue(new ApiResponse<RideSharePairingResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<RideSharePairingResponse> call, Throwable t) {
                        res.setValue(new ApiResponse<RideSharePairingResponse>(t));
                    }
                });
        return res;
    }

    /**
     * Cancel the order
     * @param id ride share id
     * @return
     */
    public SingleLiveEvent<ApiResponse<StandardResponse>> cancelShareRideOrder(Integer id) {
        SingleLiveEvent<ApiResponse<StandardResponse>> cancelResponse = new SingleLiveEvent<>();
        apiService.cancelShareRideOrder(id)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        cancelResponse.setValue(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        cancelResponse.setValue(new ApiResponse<StandardResponse>(t));
                    }
                });
        return cancelResponse;
    }

    /**
     * Driver/Passenger cancel the order
     * @param id
     * @return
     */
    public SingleLiveEvent<ApiResponse<StandardResponse>> cancelOrder(Integer id) {
        SingleLiveEvent<ApiResponse<StandardResponse>> cancelResponse = new SingleLiveEvent<>();
        apiService.cancelOrder(id)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        cancelResponse.setValue(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        cancelResponse.setValue(new ApiResponse<StandardResponse>(t));
                    }
                });
        return cancelResponse;
    }

    /**
     * Driver reach the pick up point request
     * @param transcationId
     * @return
     */
    public SingleLiveEvent<ApiResponse<StandardResponse>> driverReachPickupPoint(Integer transcationId) {
        SingleLiveEvent<ApiResponse<StandardResponse>> reachResponse = new SingleLiveEvent<>();
        apiService.driverReachPickupPoint(transcationId)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        reachResponse.setValue(new ApiResponse<StandardResponse>(response));
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        reachResponse.setValue(new ApiResponse<StandardResponse>(t));
                    }
                });
        return reachResponse;
    }

    /**
     * Passenger/Driver check the status of the driver
     * @param id Transaction id
     * @return
     */
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

    /**
     * Driver check the status of share ride transaction
     * @param id share ride id
     * @return
     */
    public LiveData<ApiResponse<RideShareTransactionResource>> driverCheckRideShareStatus(Integer id) {
        MutableLiveData<ApiResponse<RideShareTransactionResource>> res = new MutableLiveData<>();
        apiService.checkRideShareTransactionStatus(id)
                .enqueue(new Callback<RideShareTransactionResource>() {
                    @Override
                    public void onResponse(Call<RideShareTransactionResource> call, Response<RideShareTransactionResource> response) {
                        res.setValue(new ApiResponse<RideShareTransactionResource>(response));
                    }

                    @Override
                    public void onFailure(Call<RideShareTransactionResource> call, Throwable t) {
                        res.setValue(new ApiResponse<RideShareTransactionResource>(t));
                    }
                });
        return res;
    }

    /**
     * Get the latest status of the transaction
     * @param id transaction id
     * @return LiveData<Transcation>
     */
    public LiveData<Transcation> updateTransactionStatus(Integer id) {
        MediatorLiveData<Transcation> transcationMediatorLiveData = new MediatorLiveData<>();
        MutableLiveData<Transcation> transcationMutableLiveData = new MutableLiveData<>();
        db.getInvalidationTracker().addObserver(new InvalidationTracker.Observer("Transcation") {
            @Override
            public void onInvalidated(@NonNull Set<String> tables) {
                for (String table : tables) {
                    if(table.equals("Transcation")) {
                        transcationMutableLiveData.setValue(dao.loadTransactionById(id).getValue());
                    }
                }
            }
        });
        transcationMediatorLiveData.addSource(transcationMutableLiveData, new Observer<Transcation>() {
            @Override
            public void onChanged(@Nullable Transcation transcation) {
                transcationMediatorLiveData.setValue(transcation);
            }
        });
        return transcationMediatorLiveData;
    }
}
