package com.jonathan.taxidispatcher.ui.passenger_rideshare;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.PassengerShareRideFound;
import com.jonathan.taxidispatcher.factory.PassengerShareRideViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerCancelFragment;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class PassengerRideShareActivity extends AppCompatActivity
        implements Injectable, HasSupportFragmentInjector {
    static FragmentManager manager;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    PassengerShareRideViewModelFactory factory;
    PassengerRideShareViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_ride_share);
        // Start Running the service if it is not started
        if (!Utils.isMyServiceRunning(PassengerShareRideSocketService.class, this)) {
            Intent intent = new Intent(this, PassengerShareRideSocketService.class);
            intent.setAction(Constants.ACTION.START_FOREGROUND_SERVICE);
            startService(intent);
        }
        Intent intent = new Intent(this, PassengerShareRideSocketService.class);
        intent.setAction(Constants.ACTION.START_FOREGROUND_SERVICE);
        startService(intent);
        viewModel = ViewModelProviders.of(this, factory).get(PassengerRideShareViewModel.class);
        manager = getSupportFragmentManager();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    public static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.passengerRideShareFragment, fragment);
        if (!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        updateStatus();
    }

    private void updateStatus() {
        viewModel.setCurrentTransactionStatus(Session.getShareRideId(this));
        // Update the status of the page
        viewModel.getCurrentTransactionStatus().observe(this, new Observer<ApiResponse<RideShareResource>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<RideShareResource> rideShareResourceApiResponse) {
                if (rideShareResourceApiResponse.isSuccessful()) {
                    viewModel.setRideShare(rideShareResourceApiResponse.body.data);
                    //TODO:add cancel case
                    switch (rideShareResourceApiResponse.body.data.status) {
                        case 100:
                            changeFragment(PassengerRideShareWaitingFragment.newInstance(), true);
                            break;
                        case 101:
                            changeFragment(PassengerRideShareWaitingFragment.newInstance(), true);
                            break;
                        case 200:
                            viewModel.getPairing().observe(PassengerRideShareActivity.this, new Observer<ApiResponse<RideSharePairingResponse>>() {
                                @Override
                                public void onChanged(@Nullable ApiResponse<RideSharePairingResponse> response) {
                                    if(response.isSuccessful()) {
                                        if(response.body.success == 1) {
                                            if(response.body.rideShare.status == 400) {
                                                changeFragment(PassengerCancelFragment.newInstance(),false);
                                            }
                                            requestDriverLocation(response.body.rideShare.driver.id);
                                            viewModel.setRideShareTranscation(response.body.rideShare);
                                            changeFragment(PassengerStartShareRideFragment.newInstance(), true);
                                        }
                                    }
                                }
                            });
                            break;
                        case 400:
                            toMainPage();
                            break;
                        default:
                            toMainPage();
                            break;
                    }
                } else {

                }
            }
        });
    }

    private void toMainPage() {
        Intent intent = new Intent(this, PassengerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestDriverLocation(int id) {
        Intent intent = new Intent(this, PassengerShareRideSocketService.class);
        intent.setAction("getDriverLocation");
        intent.putExtra("driverId", id);
        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShareRideFound(PassengerShareRideFound event) {
        viewModel.setRideShareTranscation(event.transaction);
        changeFragment(PassengerStartShareRideFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusUpdate(Integer integer) {
        updateStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
