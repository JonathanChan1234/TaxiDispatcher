package com.jonathan.taxidispatcher.ui.driver_main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.RideShareCollection;
import com.jonathan.taxidispatcher.data.model.TranscationCollection;
import com.jonathan.taxidispatcher.databinding.FragmentDriverOrderBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.session.Session;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverOrderFragment extends Fragment implements Injectable {

    FragmentDriverOrderBinding binding;
    DriverOrderListAdapter adapter;
    DriverShareRideOrderAdapter share_adapter;

    @Inject
    APIInterface apiService;

    public DriverOrderFragment() {
        // Required empty public constructor
    }

    public static DriverOrderFragment newInstance() {
        return new DriverOrderFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverOrderBinding.inflate(inflater, container, false);
        initUI();
        return binding.getRoot();
    }

    private void initUI() {
        ArrayAdapter<CharSequence> typeList = ArrayAdapter.createFromResource(getContext(),
                R.array.type,
                android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(typeList);

        binding.orderList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.orderList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("spinner", i + "");
                switch (i) {
                    case 0:
                        getPersonalRide();
                        break;
                    case 1:
                        getShareRide();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getPersonalRide();

    }

    private void getShareRide() {
        binding.processingBar.setVisibility(View.VISIBLE);
        apiService.driverShareRideHistory(Session.getUserId(getContext()))
                .enqueue(new Callback<RideShareCollection>() {
                    @Override
                    public void onResponse(Call<RideShareCollection> call, Response<RideShareCollection> response) {
                        binding.processingBar.setVisibility(View.GONE);
                        if (response.body() != null) {
                            if (response.body().transactionList.size() == 0) {
                                Toast.makeText(getContext(), "No transaction history", Toast.LENGTH_SHORT).show();
                            }
                            share_adapter = new DriverShareRideOrderAdapter(getContext(), response.body().transactionList);
                            binding.orderList.setAdapter(share_adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<RideShareCollection> call, Throwable t) {
                        binding.processingBar.setVisibility(View.GONE);
                    }
                });
    }

    private void getPersonalRide() {
        binding.processingBar.setVisibility(View.VISIBLE);
        apiService.checkDriverOrder(Session.getUserId(getContext()))
                .enqueue(new Callback<TranscationCollection>() {
                    @Override
                    public void onResponse(Call<TranscationCollection> call, Response<TranscationCollection> response) {
                        binding.processingBar.setVisibility(View.GONE);
                        if (response.body() != null) {
                            if (response.body().transcationList.size() > 0) {
                                adapter = new DriverOrderListAdapter(getContext(), response.body().transcationList);
                                binding.orderList.setAdapter(adapter);
                            } else {
                                Toast.makeText(getContext(), "No transaction history", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TranscationCollection> call, Throwable t) {
                        binding.processingBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
