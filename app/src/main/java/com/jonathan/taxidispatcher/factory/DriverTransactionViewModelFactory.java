package com.jonathan.taxidispatcher.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.DriverDataModel;
import com.jonathan.taxidispatcher.data.LocationRepository;
import com.jonathan.taxidispatcher.data.TaxiRepository;
import com.jonathan.taxidispatcher.data.TransactionRepository;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionViewModel;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainViewModel;

import javax.inject.Inject;

public class DriverTransactionViewModelFactory implements ViewModelProvider.Factory {
    private LocationRepository locationRepository;
    private DriverDataModel driverDataModel;
    private TaxiRepository taxiRepository;
    private TransactionRepository transactionRepository;

    @Inject
    public DriverTransactionViewModelFactory(LocationRepository locationRepository,
                                             DriverDataModel driverDataModel,
                                             TaxiRepository taxiRepository,
                                             TransactionRepository transactionRepository) {
        this.locationRepository = locationRepository;
        this.driverDataModel = driverDataModel;
        this.taxiRepository = taxiRepository;
        this.transactionRepository = transactionRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(DriverTransactionViewModel.class)) {
            return (T) new DriverTransactionViewModel(locationRepository, driverDataModel, taxiRepository, transactionRepository);
        }
        throw new IllegalArgumentException("Unknown View Model class");
    }
}
