package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.DriverFoundResponse;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.driver.DriverShareRideFoundEvent;
import com.jonathan.taxidispatcher.event.driver.PassengerFoundEvent;
import com.jonathan.taxidispatcher.event.TranscationCompletedEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.service.DriverSocketService;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class DriverTransactionActivity extends AppCompatActivity
        implements Injectable, HasSupportFragmentInjector {

    public static final String DRIVER_TRANSACTION = "Driver Transaction";
    public static final String REPORT_POSITION_TO_PASSENGER = "reportPositionToPassenger";
    public static final String STOP_TIMER = "STOP_TIMER";
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;
    static FragmentManager manager;
    boolean mBound = false;
    DriverSocketService service;
    int previousStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_transaction);

        manager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.driverTransactionContainer, DriverWaitingFragment.newInstance())
                    .commitNow();
        }
        viewModel = ViewModelProviders.of(this, factory).get(DriverTransactionViewModel.class);
        Intent intent = new Intent(this, DriverSocketService.class);
        intent.setAction(Constants.ACTION.START_FOREGROUND_SERVICE);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DRIVER_TRANSACTION, "Activity on start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DRIVER_TRANSACTION, "Activity on destroy");
        if (mBound) {
            unbindService(conn);
        }
        Intent intent = new Intent(this, DriverSocketService.class);
        intent.setAction(Constants.ACTION.STOP_FOREGROUND_SERVICE);
        startService(intent);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        refreshPage();
    }

    private void refreshPage() {
        if (mBound) {
            viewModel.checkDriverStatus(Session.getUserId(this));
            viewModel.getDriverStatus().observe(this, response -> {
                if (response.isSuccessful()) {
                    if (response.body.occupied == 1) {
                        viewModel.setOnServe(false);
                    } else {
                        viewModel.setOnServe(true);
                    }
                    if (response.body.success == 1) {
                        if (response.body.type.equals("s")) {
                            Session.saveCurrentShareRideId(this, response.body.transactionId);
                            Session.saveCurrentTransactionID(this, 0);
                            viewModel.getRideShareResource(response.body.transactionId).observe(this, rideShare -> {
                                if (rideShare.isSuccessful()) {
                                    viewModel.setRideShareResource(rideShare.body.data);
                                    switch (rideShare.body.data.status) {
                                        case 101:
                                            changeFragment(DriverShareRideFoundFragment.newInstance(), true);
                                            break;
                                        case 200:
                                            sendPositionToPassenger();
                                            changeFragment(DriverStartShareRideFragment.newInstance(), true);
                                            break;
                                        case 201:
                                            sendPositionToPassenger();
                                            changeFragment(DriverStartShareRideFragment.newInstance(), true);
                                            break;
                                        case 202:
                                            sendPositionToPassenger();
                                            changeFragment(DriverStartShareRideFragment.newInstance(), true);
                                            break;
                                        case 400:
                                            changeFragment(DriverShareRideFoundFragment.newInstance(), true);
                                            break;
                                    }
                                }
                            });
                        } else {
                            Session.saveCurrentTransactionID(this, response.body.transactionId);
                            Session.saveCurrentShareRideId(this, 0);
                            viewModel.getTranscationResource(response.body.transactionId).observe(this, transaction -> {
                                if (transaction.isSuccessful()) {
                                    viewModel.setTranscation(transaction.body.data);
                                    switch (transaction.body.data.status) {
                                        case 101:
                                            changeFragment(DriverPassengerFoundFragment.newInstance(), true);
                                            break;
                                        case 102:
                                            changeFragment(DriverWaitingReplyFragment.newInstance(), true);
                                            break;
                                        case 200:
                                            changeFragment(DriverStartRideFragment.newInstance(), true);
                                            break;
                                        case 201:
                                            changeFragment(DriverStartRideFragment.newInstance(), true);
                                            break;
                                        case 202:
                                            changeFragment(DriverStartRideFragment.newInstance(), true);
                                            break;
                                        case 400:
                                            changeFragment(DriverWaitingFragment.newInstance(), true);
                                            break;
                                        default:
                                            changeFragment(DriverWaitingFragment.newInstance(), true);
                                            break;
                                    }
                                }
                            });
                        }
                    } else {
                        changeFragment(DriverWaitingFragment.newInstance(), true);
                        Intent intent = new Intent(this, DriverSocketService.class);
                        intent.setAction(STOP_TIMER);
                        startService(intent);
                    }
                } else {
//                    changeFragment(DriverWaitingFragment.newInstance(), true);
                    Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(Integer integer) {
        refreshPage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPassengerFoundEvent(PassengerFoundEvent event) {
        // Convert the JSON to POJO
        Gson gson = new Gson();
        DriverFoundResponse data = gson.fromJson(event.getData(), DriverFoundResponse.class);
        viewModel.setTranscation(data.transcation);
        viewModel.setRideType("p");
        changeFragment(DriverPassengerFoundFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShareRideFound(DriverShareRideFoundEvent event) {
        viewModel.setRideShareResource(event.transcation);
        changeFragment(DriverShareRideFoundFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionCompletedEvent(TranscationCompletedEvent event) {
        if (event.getResponse() == 1) {
            changeFragment(DriverStartRideFragment.newInstance(), true);
        } else {
            changeFragment(DriverWaitingFragment.newInstance(), true);
        }
    }

    private void sendPositionToPassenger() {
        Intent intent = new Intent(this, DriverSocketService.class);
        intent.setAction(REPORT_POSITION_TO_PASSENGER);
        startService(intent);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DriverSocketService.DriverServiceBinder binder = (DriverSocketService.DriverServiceBinder) iBinder;
            mBound = true;
            service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.driverTransactionContainer, fragment);
        if (!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
