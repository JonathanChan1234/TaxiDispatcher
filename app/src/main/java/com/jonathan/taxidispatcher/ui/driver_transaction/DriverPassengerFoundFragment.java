package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentDriverPassengerFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.PassengerFoundResponse;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class DriverPassengerFoundFragment extends Fragment 
        implements Injectable, OnMapReadyCallback {
    FragmentDriverPassengerFoundBinding binding;
    GoogleMap mMap;
    boolean isClicked = false;
    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;
    Transcation transcation;
    Driver driver;

    public DriverPassengerFoundFragment() {
        // Required empty public constructor
    }

    public static DriverPassengerFoundFragment newInstance() {
        return new DriverPassengerFoundFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverPassengerFoundBinding.inflate(inflater, container, false);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.routeMap);
        if(fragment != null) {
            Log.d("OnMapReady", "fragment exist");
            fragment.getMapAsync(this);
        } 
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        if(viewModel.getTranscation() != null) {
            transcation = viewModel.getTranscation();
            if(transcation != null) {
                binding.idText.setText(String.valueOf(transcation.id));
                binding.routeText.setText("From " + transcation.startAddr + " To"
                        + transcation.desAddr);
                binding.requirementText.setText(transcation.requirement);
                binding.statusText.setText(String.valueOf(transcation.status));
                driver = transcation.driver;
                binding.toDestinationButton.setOnClickListener(view -> {
                    toDestination();
                });
                binding.acceptDealButton.setOnClickListener(view -> {
                    acceptDeal();
                });
                binding.rejectDealButton.setOnClickListener(view -> {
                    rejectDeal();
                });
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        placeMarker();
    }

    // Set up the marker
    private void placeMarker() {
        MarkerOptions originOptions = new MarkerOptions();
        originOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        originOptions.title("Pick-up");

        MarkerOptions destinationOptions = new MarkerOptions();
        destinationOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        destinationOptions.title("Destination");

        originOptions.position(new LatLng(Double.parseDouble(transcation.startLat), Double.parseDouble(transcation.startLong)));
        mMap.addMarker(originOptions);
        destinationOptions.position(new LatLng(Double.parseDouble(transcation.desLat), Double.parseDouble(transcation.desLong)));
        mMap.addMarker(destinationOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(Double.parseDouble(transcation.startLat), Double.parseDouble(transcation.startLong)), 17));
    }

    public void toDestination() {
        if(mMap != null) {
            if (isClicked) {
                LatLng latlng = new LatLng(Double.parseDouble(transcation.startLat), Double.parseDouble(transcation.startLong));
                Log.i("Passenger Found", latlng.latitude + ", " + latlng.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                binding.toDestinationButton.setText("To Destination");
                isClicked = false;
            } else {
                isClicked  = true;
                Log.i("Destination", "{" + transcation.startLat + ", " + transcation.startLong + "}");
                LatLng latlng = new LatLng(Double.parseDouble(transcation.desLat), Double.parseDouble(transcation.desLong));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                binding.toDestinationButton.setText("To Pick up point");
            }
        }
    }

    public void acceptDeal() {
        EventBus.getDefault().post(new PassengerFoundResponse(1, transcation.id, driver.id));
        DriverTransactionActivity.changeFragment(DriverWaitingReplyFragment.newInstance(), true);
    }

    public void rejectDeal() {
        EventBus.getDefault().post(new PassengerFoundResponse(0, transcation.id, driver.id));
        DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text = "Please answer with " + String.format("%02d", event.getMinute()) +
                ":" + String.format("%02d", event.getSecond());
        binding.timeCounterText.setText(text);
    }
}
