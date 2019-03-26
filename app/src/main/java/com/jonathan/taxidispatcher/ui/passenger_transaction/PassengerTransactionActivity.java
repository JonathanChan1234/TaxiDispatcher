package com.jonathan.taxidispatcher.ui.passenger_transaction;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.databinding.ActivityPassengerTransactionBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.DriverFoundEvent;
import com.jonathan.taxidispatcher.event.PassengerShareRideFound;
import com.jonathan.taxidispatcher.factory.PassengerTransactionViewModelFactory;
import com.jonathan.taxidispatcher.service.PassengerSocketService;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class PassengerTransactionActivity extends AppCompatActivity
        implements Injectable, HasSupportFragmentInjector {
    static FragmentManager manager;
    @Inject
    PassengerTransactionViewModelFactory factory;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    PassengerTransactionViewModel viewModel;
    ActivityPassengerTransactionBinding binding;

    boolean isServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_passenger_transaction);
        manager = getSupportFragmentManager();
        viewModel = ViewModelProviders.of(this, factory).get(PassengerTransactionViewModel.class);
        binding.setViewModel(viewModel);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        updateTransactionStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceStarted) {
            Log.d("PassengerSocketService", "service destroyed");
            Intent intent = new Intent(PassengerTransactionActivity.this, PassengerSocketService.class);
            intent.setAction(Constants.ACTION.STOP_FOREGROUND_SERVICE);
            startService(intent);
        }
    }

    /*
       update the status when onResume
    */
    private void updateTransactionStatus() {
        Log.d("TransactionActivity", "status updated");
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.searchForRecentTransaction(Session.getCurrentTransactionID(this));
        viewModel.getTransactionStatus().observe(this, response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                viewModel.setCurrentTranscation(response.body.data);
                if (response.body.data.driver != null) {
                    viewModel.setTransactionDriver(response.body.data.driver);
                }
                // Start the service
                switch (response.body.data.status) {
                    case 100:
                        changeFragment(PassengerWaitingFragment.newInstance(), true);
                        startCommunicationService();
                        break;
                    case 101:
                        changeFragment(PassengerWaitingFragment.newInstance(), true);
                        startCommunicationService();
                        break;
                    case 102:
                        if (response.body.data.driver != null) {
                            viewModel.setTransactionDriver(response.body.data.driver);
                            changeFragment(PassengerDriverFoundFragment.newInstance(), true);
                        }
                        break;
                    case 200:
                        changeFragment(PassengerDriverConnectedFragment.newInstance(), true);
                        startCommunicationService();
                        break;
                    case 201:
                        changeFragment(PassengerDriverConnectedFragment.newInstance(), true);
                        break;
                    case 202: //Driver can cancel the order
                        changeFragment(PassengerDriverConnectedFragment.newInstance(), true);
                        break;
                    case 300:
                        Intent intent = new Intent(this, PassengerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    case 400:
                        // Cancel page
                        break;
                    default:
                        break;
                }
            } else {
                Toast.makeText(PassengerTransactionActivity.this,
                        "Fail to connect to the Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCommunicationService() {
        if (!Utils.isMyServiceRunning(PassengerSocketService.class, this)) {
            isServiceStarted = true;
            Intent intent = new Intent(PassengerTransactionActivity.this, PassengerSocketService.class);
            intent.setAction(Constants.ACTION.START_FOREGROUND_SERVICE);
            startService(intent);
        }
    }

    public static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.passengerTransactionContainer, fragment);
        if (!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commitAllowingStateLoss();
    }

    /*
        When driver is found, go to the waiting page
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onDriverFoundEvent(DriverFoundEvent event) {
        viewModel.setTransactionDriver(event.getDriver());
        changeFragment(PassengerDriverFoundFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShareRideFound(PassengerShareRideFound event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(Integer event) {
        updateTransactionStatus();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
