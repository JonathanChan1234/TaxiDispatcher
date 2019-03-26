package com.jonathan.taxidispatcher.di;

import com.jonathan.taxidispatcher.service.DriverSocketService;
import com.jonathan.taxidispatcher.service.PassengerSocketService;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.ui.driver_main.DriverManageTaxiFragment;
import com.jonathan.taxidispatcher.ui.driver_main.DriverScanQRFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverPassengerFoundFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverShareRideFoundFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverStartRideFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverStartShareRideFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverWaitingFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverWaitingReplyFragment;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerConfirmFragment;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMakeCallFragment;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerRideShareActivity;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerRideSharePairingFragment;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerRideShareWaitingFragment;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerShareRideSocketService;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerStartShareRideFragment;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerDriverConnectedFragment;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerDriverFoundFragment;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerTransactionActivity;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerWaitingFragment;
import com.jonathan.taxidispatcher.ui.start_main.CreateAccountFragment;
import com.jonathan.taxidispatcher.ui.start_main.MainFragment;
import com.jonathan.taxidispatcher.ui.start_main.StartActivity;

import org.checkerframework.checker.units.qual.C;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BuilderModule {
    // Launch
    @ContributesAndroidInjector
    abstract StartActivity contributeStartActivity();

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    abstract CreateAccountFragment contributeCreateAccountFragment();

    // Passenger Main
    @ContributesAndroidInjector
    abstract PassengerMainActivity contributePassengerMainActivity();

    @ContributesAndroidInjector
    abstract PassengerMakeCallFragment contributePassengerMakeCallFragment();

    @ContributesAndroidInjector
    abstract PassengerConfirmFragment confirmPassengerConfirmFragment();

    //Driver Main
    @ContributesAndroidInjector
    abstract DriverMainActivity contributeDriverMainActivity();

    @ContributesAndroidInjector
    abstract DriverScanQRFragment contributeDriverScanQRFragment();

    @ContributesAndroidInjector
    abstract DriverManageTaxiFragment confirmDriverManageTaxiFragment();

    //Passenger Transaction
    @ContributesAndroidInjector
    abstract PassengerTransactionActivity contributePassengerTransactionActivity();

    @ContributesAndroidInjector
    abstract PassengerDriverConnectedFragment contributePassengerDriverConnectedFragment();

    @ContributesAndroidInjector
    abstract PassengerDriverFoundFragment contributePassengerDriverFoundFragment();

    @ContributesAndroidInjector
    abstract PassengerWaitingFragment contributePassengerWaitingFragment();

    //Driver Transaction
    @ContributesAndroidInjector
    abstract DriverTransactionActivity contributeDriverTransactionActivity();

    @ContributesAndroidInjector
    abstract DriverPassengerFoundFragment contributeDriverPassengerFoundFragment();

    @ContributesAndroidInjector
    abstract DriverStartRideFragment contributeDriverStartRideFragment();

    @ContributesAndroidInjector
    abstract DriverWaitingFragment contributeDriverWaitingFragment();

    @ContributesAndroidInjector
    abstract DriverWaitingReplyFragment contributeDriverWaitingReplyFragment();

    @ContributesAndroidInjector
    abstract DriverShareRideFoundFragment contributeDriverShareRideFoundFragment();

    @ContributesAndroidInjector
    abstract DriverStartShareRideFragment contributeDriverStartShareRideFragment();

    // Service
    @ContributesAndroidInjector
    abstract PassengerSocketService contributePassengerSocketService();

    @ContributesAndroidInjector
    abstract DriverSocketService contributeDriverSocketService();

    @ContributesAndroidInjector
    abstract PassengerShareRideSocketService contributePassengerShareRideSocketService();

    // Passenger Share Ride
    @ContributesAndroidInjector
    abstract PassengerRideShareActivity contributePassengerRideShareActivity();

    @ContributesAndroidInjector
    abstract PassengerRideShareWaitingFragment contributePassengerRideShareWaitingFragment();

    @ContributesAndroidInjector
    abstract PassengerRideSharePairingFragment contributePassengerRideSharePairingFragment();

    @ContributesAndroidInjector
    abstract PassengerStartShareRideFragment contributePassengerStartShareRideFragment();
}
