package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.arch.lifecycle.ViewModelProviders;
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
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.databinding.FragmentDriverShareRideFoundBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.service.DriverSocketService;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverShareRideFoundFragment extends Fragment implements Injectable, OnMapReadyCallback {

    FragmentDriverShareRideFoundBinding binding;
    DriverTransactionViewModel viewModel;
    GoogleMap mMap;

    @Inject
    DriverTransactionViewModelFactory factory;
    RideShareTransaction transaction;

    @Inject
    GoogleAPIInterface googleAPIService;
    @Inject
    APIInterface apiService;
    private FusedLocationProviderClient fusedLocationClient;

    public DriverShareRideFoundFragment() {
        // Required empty public constructor
    }

    public static DriverShareRideFoundFragment newInstance() {
        return new DriverShareRideFoundFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverShareRideFoundBinding.inflate(inflater, container, false);
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        initInfoTable();

        binding.acceptButton.setOnClickListener(view -> {
            acceptOrder();
        });
        binding.rejectButton.setOnClickListener(view -> {
            rejectOrder();
        });

    }

    // Initialize the data in the info table
    private void initInfoTable() {
        if (viewModel.getRideShareResource() != null) {
            transaction = viewModel.getRideShareResource();
            RideShare first_transcation = viewModel.getRideShareResource().first_transaction;
            RideShare second_transcation = viewModel.getRideShareResource().second_transaction;

            binding.firstIdText.setText(String.valueOf(first_transcation.id));
            String firstRoute = "From " + first_transcation.startAddr + " To" +
                    first_transcation.desAddr;
            binding.firstRouteText.setText(firstRoute);
            binding.secondIdText.setText(String.valueOf(second_transcation.id));
            String secondRoute = "From " + second_transcation.startAddr + " To " +
                    second_transcation.desAddr;
            binding.secondRouteText.setText(secondRoute);


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initDistanceAndMap();
    }

    private void initDistanceAndMap() {
        if (transaction != null) {
            try {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    String currentLocation = location.getLatitude() + "," + location.getLongitude();
                    String first_transaction_addr = transaction.first_transaction.startLat + "," +
                            transaction.first_transaction.startLong;
                    String second_transaction_addr = transaction.second_transaction.startLat + "," +
                            transaction.second_transaction.startLong;
                    googleAPIService.getRouteWithWaypoints(currentLocation,
                            first_transaction_addr,
                            second_transaction_addr,
                            Constants.API_key)
                            .enqueue(new Callback<DirectionModel>() {
                                @Override
                                public void onResponse(Call<DirectionModel> call, Response<DirectionModel> response) {
                                    if (response.body() != null && response.body().status.equals("OK")) {
                                        mMap.addPolyline(RouteDrawingUtils.getGoogleMapPolyline(response.body()));
                                        binding.timeDistanceText.setText(String.valueOf(response.body().routes.get(0).legs.get(0).distance.value / 1000) + " km"
                                                + String.valueOf(response.body().routes.get(0).legs.get(0).duration.value / 60) + " minutes");
                                        setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<DirectionModel> call, Throwable t) {
                                    setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                                }
                            });
                });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void setMarker(LatLng location) {
        MarkerOptions currentLocation = new MarkerOptions();
        currentLocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
        currentLocation.position(location);
        mMap.addMarker(currentLocation);

        MarkerOptions firstPickup = new MarkerOptions();
        firstPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        firstPickup.title("1st Pick-up");
        firstPickup.position(new LatLng(Double.parseDouble(transaction.first_transaction.startLat),
                Double.parseDouble(transaction.first_transaction.startLong)));
        mMap.addMarker(firstPickup);

        MarkerOptions secondPickup = new MarkerOptions();
        secondPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        secondPickup.position(new LatLng(Double.parseDouble(transaction.second_transaction.startLat),
                Double.parseDouble(transaction.second_transaction.startLong)));
        secondPickup.title("2nd Pick-up");
        mMap.addMarker(secondPickup);

        MarkerOptions firstDestination = new MarkerOptions();
        firstDestination.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        firstDestination.title("1st Destination");
        firstDestination.position(new LatLng(Double.parseDouble(transaction.first_transaction.desLat),
                Double.parseDouble(transaction.first_transaction.desLong)));
        mMap.addMarker(firstDestination);

        MarkerOptions secondDestination = new MarkerOptions();
        secondDestination.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        secondDestination.title("2nd Destination");
        secondDestination.position(new LatLng(Double.parseDouble(transaction.second_transaction.desLat),
                Double.parseDouble(transaction.second_transaction.desLong)));
        mMap.addMarker(secondDestination);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                location, 16));
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
        if (event.getSecond() == 0 && event.getMinute() == 0) {
            DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
        }
    }

    private void acceptOrder() {
        responseOrder(1);
    }

    private void rejectOrder() {
        responseOrder(0);
    }

    private void stopTimer() {
        Intent intent = new Intent(getActivity(), DriverSocketService.class);
        intent.setAction(DriverTransactionActivity.STOP_TIMER);
        getActivity().startService(intent);
    }

    private void responseOrder(int response) {
        binding.processingBar.setVisibility(View.VISIBLE);
        apiService.driverResponseShareRideOrder(transaction.id, Session.getUserId(getContext()), response)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        binding.processingBar.setVisibility(View.GONE);
                        if (response.body() != null) {
                            if (response.body().success == 1) {
                                stopTimer();
                                DriverTransactionActivity.changeFragment(DriverStartShareRideFragment.newInstance(), true);
                            } else {
                                if(response.body().message.equals("rejected")) {
                                    stopTimer();
                                    DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(), true);
                                } else if(response.body().message.equals("cancelled")) {
                                    stopTimer();
                                    DriverTransactionActivity.changeFragment(TransactionCancelFragment.newInstance(), true);
                                } else {
                                    Toast.makeText(getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        binding.processingBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
