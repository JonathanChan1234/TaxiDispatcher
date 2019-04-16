package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PassengerSettingFragment extends Fragment {


    public PassengerSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_passenger_setting2, container, false);
    }

}
