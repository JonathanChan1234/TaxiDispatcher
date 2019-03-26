package com.jonathan.taxidispatcher.ui.passenger_rideshare;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerRideShareWaitingBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.PassengerShareRideViewModelFactory;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;

import javax.inject.Inject;


public class PassengerRideShareWaitingFragment extends Fragment implements Injectable {
    FragmentPassengerRideShareWaitingBinding binding;
    @Inject
    PassengerShareRideViewModelFactory factory;
    PassengerRideShareViewModel viewModel;
    RideShare rideShare;

    public PassengerRideShareWaitingFragment() {
        // Required empty public constructor
    }

    public static PassengerRideShareWaitingFragment newInstance() {
        return new PassengerRideShareWaitingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerRideShareWaitingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerRideShareViewModel.class);
        initUI();
    }

    private void initUI() {
        if(viewModel.getRideShare() != null) {
            rideShare = viewModel.getRideShare();
            binding.routeText.setText("From " + rideShare.startAddr + " To " + rideShare.desAddr);
            binding.cancelOrderButton.setOnClickListener(view -> {
                binding.cancelOrderButton.setEnabled(true);
                binding.actionText.setText("Cancelling");
                viewModel.cancelShareRideOrder(rideShare.id).observe(this, new Observer<ApiResponse<StandardResponse>>() {
                    @Override
                    public void onChanged(@Nullable ApiResponse<StandardResponse> response) {
                        if(response.isSuccessful()) {
                            if(response.body.success == 1) {
                                Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), response.body.message, Toast.LENGTH_SHORT).show();
                                binding.actionText.setText("Pairing");
                            }
                        } else {
                            binding.actionText.setText("Pairing");
                            Toast.makeText(getContext(), response.errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }
    }
}
