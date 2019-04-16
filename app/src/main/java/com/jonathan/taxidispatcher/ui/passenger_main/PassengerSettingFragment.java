package com.jonathan.taxidispatcher.ui.passenger_main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerSettingBinding;
import com.jonathan.taxidispatcher.session.Session;

import java.util.ArrayList;
import java.util.List;

public class PassengerSettingFragment extends Fragment {
    FragmentPassengerSettingBinding binding;
    private List<PassengerSettingListAdapter.SettingItem> items;
    private PassengerSettingListAdapter adapter;

    public PassengerSettingFragment() {
        // Required empty public constructor
    }

    public static PassengerSettingFragment newInstance() {
        return new PassengerSettingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerSettingBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    private void initList() {
        binding.settingListView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.settingListView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        items = new ArrayList<>();
        items.add(new PassengerSettingListAdapter.SettingItem("Account", R.drawable.ic_myaccount_24px));
        items.add(new PassengerSettingListAdapter.SettingItem("Notification", R.drawable.ic_notification_24px));
        items.add(new PassengerSettingListAdapter.SettingItem("Help", R.drawable.ic_help_24px));
        adapter = new PassengerSettingListAdapter(getContext(), items, ((position, text) -> {

        }));
        binding.settingListView.setAdapter(adapter);
        binding.setIPButton.setOnClickListener(view -> {
            Session.setIP(getContext(), binding.ipText.getText().toString());
        });
    }
}
