package com.jonathan.taxidispatcher.ui.start_main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.R;

public class LaunchFragment extends Fragment {


    public LaunchFragment() {
    }

    public static LaunchFragment newInstance() {
        return new LaunchFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launch, container, false);
    }
}
