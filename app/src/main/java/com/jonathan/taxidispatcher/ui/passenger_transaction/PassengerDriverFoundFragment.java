package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerDriverFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.DriverResponseEvent;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.PassengerTransactionViewModelFactory;
import com.jonathan.taxidispatcher.service.PassengerSocketService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.jonathan.taxidispatcher.service.PassengerSocketService.STOP_TIMER;


public class PassengerDriverFoundFragment extends Fragment implements Injectable {
    FragmentPassengerDriverFoundBinding binding;
    PassengerTransactionViewModel viewModel;
    Transcation transcation;
    Driver driver;
    Handler failHandler;

    @Inject
    APIInterface apiService;

    @Inject
    PassengerTransactionViewModelFactory factory;
    Context mContext;

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
            confirmRide(1);
        });
        binding.rejectDriverButton.setOnClickListener(view -> {
            confirmRide(0);
        });
        mContext = getActivity();
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

    private void confirmRide(int res) {
        binding.processingBar.setVisibility(View.VISIBLE);
        apiService.passengerConfirmOrder(transcation.id, res)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        binding.processingBar.setVisibility(View.GONE);
                        if(response.isSuccessful()) {
                            Intent intent = new Intent(getActivity(), PassengerSocketService.class);
                            intent.setAction(STOP_TIMER);
                            getActivity().startService(intent);
                            if(response.body().success == 1) {
                                PassengerTransactionActivity.changeFragment(PassengerDriverConnectedFragment.newInstance(), true);
                            } else {
                                Toast.makeText(mContext, response.body().message,
                                        Toast.LENGTH_SHORT).show();
                                PassengerTransactionActivity.changeFragment(PassengerCancelFragment.newInstance(), true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        binding.processingBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Cannot connect to the Internet",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text = "Please answer with " + String.format("%02d", event.getMinute()) +
                ":" + String.format("%02d", event.getSecond());
        binding.timerText.setText(text);
        if(event.getSecond() == 0 && event.getMinute() == 0) {
            PassengerTransactionActivity.changeFragment(PassengerCancelFragment.newInstance(), true);
        }
    }
}
