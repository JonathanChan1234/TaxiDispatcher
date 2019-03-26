package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.R;


public class DriverWaitingTransactionFragment extends Fragment {


    public DriverWaitingTransactionFragment() {
        // Required empty public constructor
    }

    public static DriverWaitingTransactionFragment newInstance() {
        return new DriverWaitingTransactionFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_waiting_transaction, container, false);
    }

}
