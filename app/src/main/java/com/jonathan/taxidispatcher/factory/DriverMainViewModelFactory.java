package com.jonathan.taxidispatcher.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.TaxiRepository;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainViewModel;

import javax.inject.Inject;

public class DriverMainViewModelFactory implements ViewModelProvider.Factory {
   private TaxiRepository taxiRepository;

    @Inject
    public DriverMainViewModelFactory(TaxiRepository taxiRepository) {
        this.taxiRepository = taxiRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(DriverMainViewModel.class)) {
            return (T) new DriverMainViewModel(taxiRepository);
        }
        throw new IllegalArgumentException("Unknown View Model class");
    }
}
