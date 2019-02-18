package com.jonathan.taxidispatcher.ui.driver_main;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Taxi;
import com.jonathan.taxidispatcher.databinding.FragmentDriverManageTaxiBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverMainViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;

import java.util.List;

import javax.inject.Inject;

public class DriverManageTaxiFragment extends Fragment implements Injectable {
    FragmentDriverManageTaxiBinding binding;
    private List<Taxi> items;
    @Inject
    DriverMainViewModelFactory factory;
    DriverMainViewModel viewModel;
    DriverManageListAdapter adapter;

    public DriverManageTaxiFragment() {
        // Required empty public constructor
    }

    public static DriverManageTaxiFragment newInstance() {
        return new DriverManageTaxiFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Your Taxi");
        binding = FragmentDriverManageTaxiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverMainViewModel.class);
        binding.driverTaxiList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.driverTaxiList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onResume() {
        super.onResume();
        checkOwnedTaxi();
    }

    private void checkOwnedTaxi() {
        if (Session.getUserId(getContext()) != 0) {
            binding.progressBar.setVisibility(View.VISIBLE);
            viewModel.getTaxiList(Session.getUserId(getContext()))
                    .observe(this, taxisApiResponse -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (taxisApiResponse.isSuccessful()) {
                            if (taxisApiResponse.body.taxis.size() > 0) {
                                items = taxisApiResponse.body.taxis;
                                adapter = new DriverManageListAdapter(getContext(), items, ((position, text) -> {
                                    Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                                    deleteTaxiAccount(text);
                                }));
                                binding.driverTaxiList.setAdapter(adapter);
                            } else {
                                binding.messageText.setVisibility(View.VISIBLE);
                                binding.messageText.setText("You do not own any taxi");
                                binding.driverTaxiList.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Taxi list", "not found");
                            Toast.makeText(getContext(), "Network Connection Issue", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteTaxiAccount(String plateNumber) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.text_input_layout, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Please enter the password")
                .setView(dialogView)
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    EditText editText = dialogView.findViewById(R.id.editText);
                    viewModel.deleteTaxiAccount(plateNumber, editText.getText().toString())
                    .observe(DriverManageTaxiFragment.this, response -> {
                        if(response.isSuccessful()) {
                            if(response.body.success == 1) {
                                Toast.makeText(getContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                                checkOwnedTaxi();
                            } else {
                                Toast.makeText(getContext(), response.body.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Network connection error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }
}
