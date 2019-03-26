package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.databinding.DialogDriverRequirementBinding;
import com.jonathan.taxidispatcher.databinding.FragmentDriverWaitingBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.driver.SetServingEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.utils.GPSPromptEnabled;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public class DriverWaitingFragment extends Fragment implements Injectable {
    DriverTransactionViewModel viewModel;

    @Inject
    DriverTransactionViewModelFactory factory;
    FragmentDriverWaitingBinding binding;
    int requirement = 1;
    int location = 1;

    public DriverWaitingFragment() {
        // Required empty public constructor
    }

    public static DriverWaitingFragment newInstance() {
        return new DriverWaitingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverWaitingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        initUI();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        GPSPromptEnabled.promptUserEnabledGPS(getActivity());
        // initUI depending on whether the taxi is on serve
        if(viewModel.getOnServe() != null) {
            Log.d("on Resume", viewModel.getOnServe() + "");
            if(viewModel.getOnServe()) {
                setOnServeState();
            } else {
                setNotOnServeState();
            }
        } else {
            Log.d("on Resume", "No data");
        }
    }

    private void initUI() {
        binding.plateNumberText.setText(Session.getTaxiPlateNumber(getContext()));
        binding.startSearchingPassengerButton.setOnClickListener(startSearchingForPassenger);
        binding.cancelSearchButton.setOnClickListener(cancelSearch);
        binding.signOutTaxiButton.setOnClickListener(signoutTaxi);
        viewModel.getStartOnServeResponse().observe(this, response -> {
            if(response.isSuccessful()) {
                if(response.body.success == 1) {
                    if(response.body.message.equals("occupied")) {
//                        DriverTransactionActivity.changeFragment(DriverWaitingTransactionFragment.newInstance(), true);
                        setNotOnServeState();
                        EventBus.getDefault().post(new SetServingEvent(0));
                    } else {
                        setOnServeState();
                        EventBus.getDefault().post(new SetServingEvent(1));
                    }
                } else {
                    Toast.makeText(getContext(), response.body.message, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Fail to connect to the Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOnServeState() {
        binding.waitingProgressBar.setVisibility(View.VISIBLE);
        binding.startSearchingPassengerButton.setText("On Serve");
        binding.signOutTaxiButton.setEnabled(false);
        binding.cancelSearchButton.setEnabled(true);
        binding.startSearchingPassengerButton.setEnabled(false);
    }

    private void setNotOnServeState() {
        binding.waitingProgressBar.setVisibility(View.GONE);
        binding.startSearchingPassengerButton.setText("Not On Serve");
        binding.cancelSearchButton.setEnabled(false);
        binding.signOutTaxiButton.setEnabled(true);
        binding.startSearchingPassengerButton.setEnabled(true);
    }

    View.OnClickListener startSearchingForPassenger = view -> {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_driver_requirement, null);
        DialogDriverRequirementBinding dialogBinding =
                DialogDriverRequirementBinding.bind(dialogView);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setTitle("Requirement")
                .setPositiveButton("Agree", ((dialogInterface, i) -> {
                    if (dialogBinding.typeRadioGroup.getCheckedRadioButtonId() != -1) {
                        switch (dialogBinding.typeRadioGroup.getCheckedRadioButtonId()) {
                            case R.id.singleGroupButton:
                                requirement = 1;
                                break;
                            case R.id.rideSharingButton:
                                requirement = 2;
                                break;
                            case R.id.bothSelectButton:
                                requirement = 3;
                                break;
                            default:
                                requirement = 1;
                                break;
                        }
                    }
                    if(dialogBinding.locationRadioGroup.getCheckedRadioButtonId() != -1) {
                        switch (dialogBinding.locationRadioGroup.getCheckedRadioButtonId()) {
                            case R.id.toIslandButton:
                                location = 1;
                                break;
                            case R.id.notToIslandButton:
                                location = 2;
                                break;
                            case R.id.doesNotMatterButton:
                                location = 3;
                                break;
                            default:
                                location = 3;
                                break;
                        }
                    }
                    startOnServe();
                }))
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .show();
    };

    /**
     *
     * Set the driver to be unoccupied
     * Start service
     */
    private void startOnServe() {
        viewModel.setOccupied(Session.getUserId(getContext()), 1, requirement, location);
    }

    View.OnClickListener cancelSearch = view -> {
        viewModel.setOccupied(Session.getUserId(getContext()), 0, 0, 0);
    };

    View.OnClickListener signoutTaxi = view -> {
        Log.d("Sign out Taxi", Session.getTaxiId(getContext()) + "");
        if(Session.getTaxiId(getContext()) != 0) {
            viewModel.signoutTaxi(Session.getTaxiId(getContext()), Session.getUserId(getContext()));
            viewModel.getSignOutTaxiResponse().observe(this, response -> {
                if(response.isSuccessful()) {
                    if(response.body.success == 1) {
                        Toast.makeText(getContext(), "Sign out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), DriverMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Something is wrong. Please try again later", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Cannot not connect to the Internet", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
