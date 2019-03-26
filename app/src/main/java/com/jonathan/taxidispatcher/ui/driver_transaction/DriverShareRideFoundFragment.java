package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.databinding.FragmentDriverShareRideFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.ShareRideDriverResponse;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

public class DriverShareRideFoundFragment extends Fragment implements Injectable {
    FragmentDriverShareRideFoundBinding binding;
    DriverTransactionViewModel viewModel;
    @Inject
    DriverTransactionViewModelFactory factory;
    RideShareTransaction transaction;

    public DriverShareRideFoundFragment() {
        // Required empty public constructor
    }

    public static DriverShareRideFoundFragment newInstance() {
        return new DriverShareRideFoundFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverShareRideFoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        initInfoTable();
        binding.acceptButton.setOnClickListener(view -> {
            acceptOrder();
        });
        binding.rejectButton.setOnClickListener(view -> {
            rejectOrder();
        });
    }

    // Initialize the data in the info table
    private void initInfoTable() {
        if(viewModel.getRideShareResource() != null) {
            transaction = viewModel.getRideShareResource();
            RideShare first_transcation = viewModel.getRideShareResource().first_transaction;
            RideShare second_transcation = viewModel.getRideShareResource().second_transaction;

            binding.firstIdText.setText(String.valueOf(first_transcation.id));
            String firstRoute = "From " + first_transcation.startAddr + " To" +
                    first_transcation.desAddr;
            binding.firstRouteText.setText(firstRoute);
            binding.secondIdText.setText(String.valueOf(second_transcation.id));
            String secondRoute = "From " + second_transcation.startAddr + " To " +
                    second_transcation.desAddr;
            binding.secondRouteText.setText(secondRoute);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text = "Please answer with " + String.format("%02d", event.getMinute()) +
                ":" + String.format("%02d", event.getSecond());
        binding.timerText.setText(text);
        if(event.getSecond() == 0 && event.getMinute() == 0) {
            DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
        }
    }

    private void acceptOrder() {
        EventBus.getDefault().post(new ShareRideDriverResponse(1, transaction.id, transaction.driver.id));
        DriverTransactionActivity.changeFragment(DriverStartShareRideFragment.newInstance(), true);
    }

    private void rejectOrder() {
        EventBus.getDefault().post(new ShareRideDriverResponse(0, transaction.id, transaction.driver.id));
        DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
    }
}
