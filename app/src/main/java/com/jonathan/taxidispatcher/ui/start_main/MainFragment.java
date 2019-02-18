package com.jonathan.taxidispatcher.ui.start_main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jonathan.taxidispatcher.api.APIClient;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.UserRepository;
import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;
import com.jonathan.taxidispatcher.databinding.FragmentMainBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.LogInViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.utils.Utils;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class MainFragment extends Fragment implements Injectable {
    FragmentMainBinding binding;
    LogInViewModel viewModel;

    @Inject
    LogInViewModelFactory factory;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        binding.directToRegisterText.setOnClickListener(view -> {
            StartActivity.switchToCreateAccountFragment();
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, factory).get(LogInViewModel.class);
        binding.phoneTextSignIn.setText(Session.getPhoneNumber(getContext()));

        binding.signInButton.setOnClickListener(view -> {
            String phoneNumber = binding.phoneTextSignIn.getText().toString();
            String password = binding.passwordTextSignIn.getText().toString();
            if(!TextUtils.isEmpty(phoneNumber) &&
                    !TextUtils.isEmpty(password)) {
                if(binding.passengerButtonInSignIn.isChecked()) {
                    viewModel.passengerLogIn(phoneNumber, password).observe(this, new Observer<ApiResponse<AccountUserResponse>>() {
                        @Override
                        public void onChanged(@Nullable ApiResponse<AccountUserResponse> response) {
                            if(response.isSuccessful()) {
                                if(response.body.success == 1) {
                                    Session.logIn(getContext(),
                                            response.body.user.id,
                                            response.body.user.phonenumber,
                                            response.body.user.username,
                                            response.body.user.email,
                                            "user",
                                            response.body.access_token
                                    );
                                    Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(), Utils.stringListToString(response.body.message), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Login check", response.errorMessage);
                                Toast.makeText(getContext(), "Network Connection error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    viewModel.driverLogIn(phoneNumber, password).observe(this, new Observer<ApiResponse<AccountDriverResponse>>() {
                        @Override
                        public void onChanged(@Nullable ApiResponse<AccountDriverResponse> response) {
                            if(response.isSuccessful()) {
                                if(response.body.success == 1) {
                                    Session.logIn(getContext(),
                                            response.body.user.id,
                                            response.body.user.phonenumber,
                                            response.body.user.username,
                                            response.body.user.email,
                                            "driver",
                                            response.body.access_token
                                    );
                                    Intent intent = new Intent(getActivity(), DriverMainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(), Utils.stringListToString(response.body.message), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Network Connection error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}