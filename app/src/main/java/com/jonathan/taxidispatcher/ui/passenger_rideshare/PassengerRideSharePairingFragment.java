package com.jonathan.taxidispatcher.ui.passenger_rideshare;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerRideSharePairingBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.PassengerShareRideViewModelFactory;

import javax.inject.Inject;


public class PassengerRideSharePairingFragment extends Fragment implements Injectable, OnMapReadyCallback {
    FragmentPassengerRideSharePairingBinding binding;
    @Inject
    PassengerShareRideViewModelFactory factory;
    PassengerRideShareViewModel viewModel;
    SupportMapFragment mapFragment;
    GoogleMap mMap;

    RideShare first_transaction;
    RideShare second_transaction;
    int shareRideId;

    public PassengerRideSharePairingFragment() {
        // Required empty public constructor
    }

    public static PassengerRideSharePairingFragment newInstance() {
        return new PassengerRideSharePairingFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerRideSharePairingBinding.inflate(inflater, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.routeMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerRideShareViewModel.class);
        viewModel.getPairing().observe(this, new Observer<ApiResponse<RideSharePairingResponse>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<RideSharePairingResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body.success == 1) {
                        shareRideId = response.body.rideShare.id;
                        first_transaction = response.body.rideShare.first_transaction;
                        second_transaction = response.body.rideShare.second_transaction;
                        binding.routeText.setText("From " + first_transaction.startAddr + " To " +
                                first_transaction.desAddr);
                        binding.myRouteText.setText("From " + second_transaction.startAddr + " To " +
                                second_transaction.desAddr);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                        Double.parseDouble(first_transaction.startLat),
                                        Double.parseDouble(first_transaction.startLong)),
                                17));
                        if (mMap != null) {
                            setMarker(Double.parseDouble(first_transaction.startLat),
                                    Double.parseDouble(first_transaction.startLong),
                                    "p");
                            setMarker(Double.parseDouble(second_transaction.startLat),
                                    Double.parseDouble(second_transaction.startLong),
                                    "p");
                            setMarker(Double.parseDouble(first_transaction.desLat),
                                    Double.parseDouble(first_transaction.desLong),
                                    "d");
                            setMarker(Double.parseDouble(first_transaction.desLat),
                                    Double.parseDouble(first_transaction.desLong),
                                    "d");
                        }
                    } else {
                        Toast.makeText(getContext(), "Info Not found", Toast.LENGTH_SHORT).show();
                        PassengerRideShareActivity.changeFragment(PassengerRideShareWaitingFragment.newInstance(), true);
                    }
                }
            }
        });
        binding.acceptButton.setOnClickListener(view -> {

        });
        binding.rejectButton.setOnClickListener(view -> {

        });
    }

    private void setMarker(Double lat, Double lng, String type) {
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        if (type.equals("p")) {
            markerOptions.title("pick-up");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        } else {
            markerOptions.title("destination");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        }
        markerOptions.position(latLng);
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (first_transaction != null &&
                second_transaction != null) {
            setMarker(Double.parseDouble(first_transaction.startLat),
                    Double.parseDouble(first_transaction.startLong),
                    "p");
            setMarker(Double.parseDouble(second_transaction.startLat),
                    Double.parseDouble(second_transaction.startLong),
                    "p");
            setMarker(Double.parseDouble(first_transaction.desLat),
                    Double.parseDouble(first_transaction.desLong),
                    "d");
            setMarker(Double.parseDouble(first_transaction.desLat),
                    Double.parseDouble(first_transaction.desLong),
                    "d");
        }
    }
}
