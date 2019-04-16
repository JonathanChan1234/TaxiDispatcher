package com.jonathan.taxidispatcher.ui.driver_transaction;


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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.GoogleAPIInterface;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentDriverPassengerFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.service.DriverSocketService;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity.STOP_TIMER;


public class DriverPassengerFoundFragment extends Fragment
        implements Injectable, OnMapReadyCallback {
    FragmentDriverPassengerFoundBinding binding;
    GoogleMap mMap;
    boolean isClicked = false;
    @Inject
    DriverTransactionViewModelFactory factory;
    DriverTransactionViewModel viewModel;

    @Inject
    GoogleAPIInterface googleAPIService;

    @Inject
    APIInterface apiService;

    Transcation transcation;
    Driver driver;
    private FusedLocationProviderClient fusedLocationClient;
    Context mContext;

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
        if (fragment != null) {
            Log.d("OnMapReady", "fragment exist");
            fragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        mContext = getActivity();
        if (viewModel.getTranscation() != null) {
            transcation = viewModel.getTranscation();
            if (transcation != null) {
                binding.idText.setText(String.valueOf(transcation.id));
                binding.routeText.setText("From " + transcation.startAddr + " To"
                        + transcation.desAddr);
                driver = transcation.driver;
                binding.toDestinationButton.setOnClickListener(view -> {
                    toDestination();
                });
                binding.acceptDealButton.setOnClickListener(view -> {
                    responseDeal(1);
                });
                binding.rejectDealButton.setOnClickListener(view -> {
                    responseDeal(0);
                });
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    public void getDistanceAndRoute() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        String origin = String.valueOf(location.getLatitude()) + "," +
                                String.valueOf(location.getLongitude());
                        String destination = transcation.startLat + ',' + transcation.startLong;
                        googleAPIService.getRoute(origin, destination, Constants.API_key)
                                .enqueue(new Callback<DirectionModel>() {
                                    @Override
                                    public void onResponse(Call<DirectionModel> call, Response<DirectionModel> response) {
                                        if (response.body() != null) {
                                            if(response.body().status.equals("OK")) {
                                                String travelDistance = String.valueOf(response.body().routes.get(0).legs.get(0).distance.value / 1000) + " km";
                                                String travelTime = String.valueOf(response.body().routes.get(0).legs.get(0).duration.value / 60) + " minutes";
                                                binding.driverTimeText.setText(
                                                        travelTime);
                                                binding.distanceText.setText(travelDistance);
                                                if (mMap != null) {
                                                    mMap.addPolyline(RouteDrawingUtils.getGoogleMapPolyline(response.body()));
                                                }
                                            }

                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<DirectionModel> call, Throwable t) {

                                    }
                                });
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        placeMarker();
        updateLocationUI();
        getDistanceAndRoute();
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
        if (mMap != null) {
            if (isClicked) {
                LatLng latlng = new LatLng(Double.parseDouble(transcation.startLat), Double.parseDouble(transcation.startLong));
                Log.i("Passenger Found", latlng.latitude + ", " + latlng.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                binding.toDestinationButton.setText("To Destination");
                isClicked = false;
            } else {
                isClicked = true;
                Log.i("Destination", "{" + transcation.startLat + ", " + transcation.startLong + "}");
                LatLng latlng = new LatLng(Double.parseDouble(transcation.desLat), Double.parseDouble(transcation.desLong));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                binding.toDestinationButton.setText("To Pick up point");
            }
        }
    }

    private void responseDeal(int res) {
        binding.processingBar.setVisibility(View.VISIBLE);
        apiService.driverResponseOrder(transcation.id, driver.id, res)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        binding.processingBar.setVisibility(GONE);
                        //Stop the timer
                        if(response.body().success == 1) {
                            Intent intent = new Intent(getActivity(), DriverSocketService.class);
                            intent.setAction(STOP_TIMER);
                            getActivity().startService(intent);
                            if (response.isSuccessful()) {
                                if (res == 1) {
                                    DriverTransactionActivity.changeFragment(DriverWaitingReplyFragment.newInstance(), true);
                                } else {
                                    DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                            DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        binding.processingBar.setVisibility(GONE);
                        Toast.makeText(mContext, "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
                    }
                });
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
        if (event.getMinute() == 0 &&
                event.getSecond() == 0) {
            DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
