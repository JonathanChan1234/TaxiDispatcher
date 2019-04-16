package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.databinding.FragmentPassengerCancelBinding;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;


public class PassengerCancelFragment extends Fragment {
    FragmentPassengerCancelBinding binding;

    public PassengerCancelFragment() {
        // Required empty public constructor
    }

    public static PassengerCancelFragment newInstance() {
       return new PassengerCancelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerCancelBinding.inflate(inflater,
                container, false);
        binding.backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        return binding.getRoot();
    }
}
