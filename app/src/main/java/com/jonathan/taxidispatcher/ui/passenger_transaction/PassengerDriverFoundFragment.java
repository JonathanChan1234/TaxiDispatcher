package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerDriverFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.DriverResponseEvent;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.PassengerTransactionViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class PassengerDriverFoundFragment extends Fragment implements Injectable {
    FragmentPassengerDriverFoundBinding binding;
    PassengerTransactionViewModel viewModel;
    Transcation transcation;
    Driver driver;
    Handler failHandler;

    @Inject
    PassengerTransactionViewModelFactory factory;

    public PassengerDriverFoundFragment() {
        // Required empty public constructor
    }

    public static PassengerDriverFoundFragment newInstance() {
        return new PassengerDriverFoundFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerDriverFoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        failHandler = new Handler();
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerTransactionViewModel.class);
        transcation = viewModel.getCurrentTranscation();
        driver = viewModel.getDriver();
        if(transcation != null) {
            binding.pickUpPointText.setText(transcation.startAddr);
            binding.destinationText.setText(transcation.desAddr);
            binding.statusText.setText(String.valueOf(transcation.status));
            binding.requirementText.setText(transcation.requirement);
            binding.idText.setText(String.valueOf(transcation.id));
        }
        if(driver != null) {
            binding.driverIdText.setText(String.valueOf(driver.id));
            binding.driverUsernameText.setText(driver.username);
        }
        binding.acceptDriverButton.setOnClickListener(view -> {
            acceptDriver();
        });
        binding.rejectDriverButton.setOnClickListener(view -> {
            rejectDriver();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void acceptDriver() {
        // Fire the accept event to passenger socket service
        EventBus.getDefault().post(new DriverResponseEvent(transcation,  driver, 1));
        PassengerTransactionActivity.changeFragment(PassengerDriverConnectedFragment.newInstance(), true);
    }

    public void rejectDriver() {
        // Fire the reject event to passenger socket service
        EventBus.getDefault().post(new DriverResponseEvent(transcation, driver, 0));
        PassengerTransactionActivity.changeFragment(PassengerWaitingFragment.newInstance(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text = "Please answer with " + String.format("%02d", event.getMinute()) +
                ":" + String.format("%02d", event.getSecond());
        binding.timerText.setText(text);
    }
}
