package com.jonathan.taxidispatcher.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainViewModel;

import javax.inject.Inject;

public class PassengerMainViewModelFactory implements ViewModelProvider.Factory {
    private LocationRepository locationRepository;
    private TransactionRepository transactionRepository;

    @Inject
    public PassengerMainViewModelFactory(LocationRepository locationRepository, TransactionRepository transactionRepository) {
        this.locationRepository = locationRepository;
        this.transactionRepository = transactionRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(PassengerMainViewModel.class)) {
            return (T) new PassengerMainViewModel(locationRepository, transactionRepository);
        }
        throw new IllegalArgumentException("Unknown View Model class");
    }
}
