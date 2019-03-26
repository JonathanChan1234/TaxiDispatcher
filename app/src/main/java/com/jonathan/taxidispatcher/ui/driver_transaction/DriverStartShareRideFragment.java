package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.databinding.FragmentDriverStartShareRideBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;

import javax.inject.Inject;

import timber.log.Timber;

public class DriverStartShareRideFragment extends Fragment
        implements Injectable, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    FragmentDriverStartShareRideBinding binding;
    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;
    RideShareTransaction transaction;
    RideShare firstTranscation;
    RideShare secondTranscation;
    GoogleMap mMap;

    //Location
    private GoogleApiClient apiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private final static int INTERVAL = 10000;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentPosition;
    Marker driverMarker;

    public DriverStartShareRideFragment() {
        // Required empty public constructor
    }

    public static DriverStartShareRideFragment newInstance() {
        return new DriverStartShareRideFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverStartShareRideBinding.inflate(inflater, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.driverMap);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        initUI();
    }

    private void initUI() {
        if(viewModel.getRideShareResource() != null) {
            transaction = viewModel.getRideShareResource();
            firstTranscation = transaction.first_transaction;
            secondTranscation = transaction.second_transaction;
            String firstRoute = "From " + firstTranscation.startAddr + " To " +
                    firstTranscation.desAddr;
            binding.firstRouteText.setText(firstRoute);
            binding.firstPassengerUsernameText.setText(firstTranscation.user.username);

            String secondRoute = "From " + secondTranscation.startAddr + " To " +
                    secondTranscation.desAddr;
            binding.secondRouteText.setText(secondRoute);
            binding.secondPassengerUsernameText.setText(secondTranscation.user.username);

            binding.firstReachPickupPointButton.setOnClickListener(view -> {

            });
            binding.secondReachPickupPointButton.setOnClickListener(view -> {

            });
            binding.firstCallPassengerButton.setOnClickListener(view -> {
                callPassenger(firstTranscation.user.phonenumber);
            });
            binding.secondCallPassengerButton.setOnClickListener(view -> {
                callPassenger(secondTranscation.user.phonenumber);
            });
        }
    }

    private void callPassenger(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setTrafficEnabled(true);
        if(firstTranscation != null && secondTranscation != null) {
            mMap.clear();
            MarkerOptions ownPickup = new MarkerOptions();
            ownPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            ownPickup.title("Pick-up");
            ownPickup.position(new LatLng(Double.parseDouble(firstTranscation.startLat), Double.parseDouble(firstTranscation.startLong)));
            mMap.addMarker(ownPickup);

            MarkerOptions ownDes = new MarkerOptions();
            ownDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            ownDes.title("Destination");
            ownDes.position(new LatLng(Double.parseDouble(firstTranscation.desLat), Double.parseDouble(firstTranscation.desLong)));
            mMap.addMarker(ownDes);

            MarkerOptions passengerPickup = new MarkerOptions();
            passengerPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            passengerPickup.title("Share Ride Pick-up");
            passengerPickup.position(new LatLng(Double.parseDouble(secondTranscation.startLat), Double.parseDouble(secondTranscation.startLong)));
            mMap.addMarker(passengerPickup);

            MarkerOptions passengerDes = new MarkerOptions();
            passengerDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            passengerDes.title("Destination");
            passengerDes.position(new LatLng(Double.parseDouble(secondTranscation.desLat), Double.parseDouble(secondTranscation.desLong)));
            mMap.addMarker(passengerDes);
        }

        initLocationRequest();
    }

    private void initLocationRequest() {
        apiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL * 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Initialize location service client and callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i("position", "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    updateDriverPosition(currentPosition);
                }
            }
        };
    }

    private void updateDriverPosition(LatLng position) {
        if(mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f));
            if(driverMarker != null) {
                driverMarker.setPosition(position);
            } else {
                MarkerOptions driverMarkerPosition = new MarkerOptions();
                driverMarkerPosition.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
                driverMarkerPosition.title("Your Position");
                driverMarkerPosition.position(position);
                driverMarker = mMap.addMarker(driverMarkerPosition);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!apiClient.isConnected()) apiClient.connect();
        if (fusedLocationProviderClient != null) {
            try {
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            } catch(SecurityException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(apiClient.isConnected()) apiClient.connect();
        if(fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        Log.d("location servie", "connected");
        try {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
