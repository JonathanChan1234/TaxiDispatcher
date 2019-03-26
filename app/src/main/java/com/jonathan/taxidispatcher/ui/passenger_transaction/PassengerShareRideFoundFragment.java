package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.databinding.FragmentPassengerShareRideFoundBinding;


public class PassengerShareRideFoundFragment extends Fragment {
    FragmentPassengerShareRideFoundBinding binding;

    public PassengerShareRideFoundFragment() {
        // Required empty public constructor
    }

    public static PassengerShareRideFoundFragment newInstance() {
        return new PassengerShareRideFoundFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerShareRideFoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
