package com.jonathan.taxidispatcher.di;

import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.ui.driver_main.DriverManageTaxiFragment;
import com.jonathan.taxidispatcher.ui.driver_main.DriverScanQRFragment;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerConfirmFragment;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMakeCallFragment;
import com.jonathan.taxidispatcher.ui.start_main.CreateAccountFragment;
import com.jonathan.taxidispatcher.ui.start_main.MainFragment;
import com.jonathan.taxidispatcher.ui.start_main.StartActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BuilderModule {
    @ContributesAndroidInjector
    abstract StartActivity contributeStartActivity();

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    abstract CreateAccountFragment contributeCreateAccountFragment();

    @ContributesAndroidInjector
    abstract PassengerMainActivity contributePassengerMainActivity();

    @ContributesAndroidInjector
    abstract PassengerMakeCallFragment contributePassengerMakeCallFragment();

    @ContributesAndroidInjector
    abstract PassengerConfirmFragment confirmPassengerConfirmFragment();

    @ContributesAndroidInjector
    abstract DriverMainActivity contributeDriverMainActivity();

    @ContributesAndroidInjector
    abstract DriverScanQRFragment contributeDriverScanQRFragment();

    @ContributesAndroidInjector
    abstract DriverManageTaxiFragment confirmDriverManageTaxiFragment();
}
