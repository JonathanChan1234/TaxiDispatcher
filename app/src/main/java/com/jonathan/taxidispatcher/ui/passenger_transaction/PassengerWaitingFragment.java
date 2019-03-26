package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerWaitingBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.PassengerTransactionViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;

import javax.inject.Inject;


public class PassengerWaitingFragment extends Fragment implements Injectable {
    PassengerTransactionViewModel viewModel;
    @Inject
    PassengerTransactionViewModelFactory factory;

    FragmentPassengerWaitingBinding binding;
    Handler failHandler;

    public PassengerWaitingFragment() {
        // Required empty public constructor
    }

    public static PassengerWaitingFragment newInstance() {
        return new PassengerWaitingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerWaitingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerTransactionViewModel.class);
        failHandler = new Handler();
        viewModel.getTransactionStatus().observe(this, new Observer<ApiResponse<TranscationResource>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<TranscationResource> response) {
                if(response.isSuccessful()) {
                    binding.warningText.setVisibility(View.GONE);
                    binding.idText.setText(String.valueOf(response.body.data.id));
                    binding.routeText.setText("From " + response.body.data.startAddr + " To " + response.body.data.desAddr);
                    binding.statusText.setText(String.valueOf(response.body.data.status));
                } else {
                    Toast.makeText(getContext(), "Please Check your Internet connection or refresh by pressing the update button",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.cancelOrderButton.setOnClickListener(cancelOrder);
    }

    View.OnClickListener cancelOrder = view -> {
        binding.processingBar.setVisibility(View.VISIBLE);
        binding.cancelOrderButton.setEnabled(false);
        binding.updateStatusButton.setEnabled(false);
        viewModel.cancelTranscation(Session.getCurrentTransactionID(getContext()))
                .observe(this, response -> {
                    binding.processingBar.setVisibility(View.GONE);
                    binding.cancelOrderButton.setEnabled(true);
                    binding.updateStatusButton.setEnabled(true);
                    if(response.isSuccessful()) {
                        if(response.body.success == 1) {
                            Intent intent  = new Intent(getActivity(), PassengerMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(),
                                    response.body.message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Network Connection Issue", Toast.LENGTH_LONG).show();
                    }
                });
    };
}
