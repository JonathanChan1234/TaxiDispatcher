package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentTransactionCancelBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.session.Session;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionCancelFragment extends Fragment implements Injectable {

    FragmentTransactionCancelBinding binding;

    @Inject
    APIInterface apiService;

    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;

    public TransactionCancelFragment() {
        // Required empty public constructor
    }

    public static TransactionCancelFragment newInstance() {
        return new TransactionCancelFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionCancelBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(DriverTransactionViewModel.class);
        if(viewModel.getTranscation() != null) {
            Transcation transcation = viewModel.getTranscation();
            binding.routeText.setText("From " + transcation.startAddr + " To " + transcation.desAddr );
            binding.orderIdText.setText("Personal Ride Order ID: " + transcation.id);
        }
        else if(viewModel.getRideShareResource() != null) {
            RideShareTransaction transcation = viewModel.getRideShareResource();
            binding.routeText.setText("From " + transcation.first_transaction.startAddr + " To " + transcation.first_transaction.desAddr + "\n"
            + "From " + transcation.second_transaction.startAddr + " To " + transcation.second_transaction.desAddr + "\n");
            binding.orderIdText.setText("Share Ride Order ID: " + transcation.id);
        }

        binding.backButton.setOnClickListener(view -> {
            askContinue();
        });
    }

    private void askContinue() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Are you ready for the next order?")
                .setMessage("Do you want to take the next order")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    apiService.setOccupied(Session.getUserId(getContext()), 1, null)
                    .enqueue(new Callback<StandardResponse>() {
                        @Override
                        public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                            if(response.body().success == 1) {
                                DriverTransactionActivity.changeFragment(
                                        DriverWaitingFragment.newInstance(),
                                        true
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Cannot connect to the internet",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


                }))
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    DriverTransactionActivity.changeFragment(
                            DriverWaitingFragment.newInstance(),
                            true
                    );
                }))
                .show();
    }
}
