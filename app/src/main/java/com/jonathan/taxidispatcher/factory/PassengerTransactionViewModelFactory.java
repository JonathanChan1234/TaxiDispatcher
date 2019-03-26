package com.jonathan.taxidispatcher.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainViewModel;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerTransactionViewModel;

import javax.inject.Inject;

public class PassengerTransactionViewModelFactory implements ViewModelProvider.Factory {
    private LocationRepository locationRepository;
    private TransactionRepository transactionRepository;

    @Inject
    public PassengerTransactionViewModelFactory(LocationRepository locationRepository, TransactionRepository transactionRepository) {
        this.locationRepository = locationRepository;
        this.transactionRepository = transactionRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(PassengerTransactionViewModel.class)) {
            return (T) new PassengerTransactionViewModel(locationRepository, transactionRepository);
        }
        throw new IllegalArgumentException("Unknown View Model class");
    }
}
