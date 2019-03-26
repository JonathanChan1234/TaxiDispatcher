package com.jonathan.taxidispatcher.service;


import android.arch.persistence.room.InvalidationTracker;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.di.TaxiApp;
import com.jonathan.taxidispatcher.room.TaxiDb;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.session.Session;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This customize sync class will be used in synchronizing the local data
 * and the web server data
 */
@Singleton
public class DriverSyncAdapter {
    private TaxiApp app;
    private TransactionDao dao;
    private APIInterface apiService;
    private TaxiDb db;

    @Inject
    public DriverSyncAdapter(TaxiApp app, TransactionDao dao, APIInterface apiService) {
        this.app = app;
        this.dao = dao;
        this.apiService = apiService;
    }

    public void insertNewTransaction(Transcation transaction, OnCallBackInterface callback) {
        Session.saveCurrentTransaction(app, transaction.id, transaction);
        ArrayList<Transcation> list = new ArrayList<>();
        apiService.searchForRecentTranscation(transaction.id)
                .enqueue(new Callback<TranscationResource>() {
                    @Override
                    public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {
                        if (response.body() != null) {
                            if (response.body().data.status == 101) {
                                if (response.body().data.driver != null &&
                                        response.body().data.driver.id == Session.getUserId(app)) {
                                    list.add(response.body().data);

                                    Thread databaseInsertThread = new Thread(()-> {
                                        dao.insertTransaction(list);
                                    });
                                    databaseInsertThread.start();

                                    callback.onDataCallback(1, "valid");
                                } else {
                                    callback.onDataCallback(0, "invalid");
                                }
                            } else {
                                callback.onDataCallback(0, "invalid");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TranscationResource> call, Throwable t) {
                        callback.onDataCallback(0, t.getMessage());
                    }
                });
    }


    public void updateTransactionStatus(Transcation transaction) {
        // If the current transaction is the same one that receive notification
        if (transaction.id == Session.getCurrentTransactionID(app)) {
            dao.updateTransactionStatus(transaction.status, transaction.id);
        } else { //If not, double check the status in the web server
            apiService.searchForRecentTranscation(transaction.id).
                    enqueue(new Callback<TranscationResource>() {
                        @Override
                        public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {
                            if (response.body() != null) {
                                if (response.body().data.id == Session.getCurrentTransactionID(app)) {
                                    dao.updateTransactionStatus(response.body().data.status, transaction.id);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<TranscationResource> call, Throwable t) {

                        }
                    });
        }
    }

interface OnCallBackInterface {
    public void onDataCallback(int success, String message);
}
}
