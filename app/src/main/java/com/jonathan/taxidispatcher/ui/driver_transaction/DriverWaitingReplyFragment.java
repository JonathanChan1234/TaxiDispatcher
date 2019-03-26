package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentDriverWaitingReplyBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;

import javax.inject.Inject;


public class DriverWaitingReplyFragment extends Fragment implements Injectable {
    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;
    FragmentDriverWaitingReplyBinding binding;

    public DriverWaitingReplyFragment() {
        // Required empty public constructor
    }

    public static DriverWaitingReplyFragment newInstance() {
        return new DriverWaitingReplyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverWaitingReplyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        initUI();
    }

    private void initUI() {
        Transcation transcation = viewModel.getTranscation();
        if (transcation != null) {
            String infoText = "Transaction ID: " + transcation.id + "\n"
                    + "From " + transcation.startAddr + " To " + transcation.desAddr;
            binding.infoText.setText(infoText);
        }
    }
}
