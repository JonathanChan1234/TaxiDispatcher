package com.jonathan.taxidispatcher.ui.passenger_transaction;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.DriverLocation;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerDriverConnectedBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.LocationUpdateEvent;
import com.jonathan.taxidispatcher.event.PassengerDriverReachEvent;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.PassengerTransactionViewModelFactory;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerDriverConnectedFragment extends Fragment implements OnMapReadyCallback, Injectable {
    boolean isExpanded = false;
    GoogleMap mMap;
    LatLng driverPosition;
    FragmentPassengerDriverConnectedBinding binding;
    PassengerTransactionViewModel viewModel;

    @Inject
    PassengerTransactionViewModelFactory factory;

    @Inject
    APIInterface apiService;
    Transcation transcation;
    Driver driver;
    Boolean isDriverReached = false;
    MarkerOptions locationOptions = new MarkerOptions();
    PolylineOptions lines = null;

    public PassengerDriverConnectedFragment() {
        // Required empty public constructor
    }

    public static PassengerDriverConnectedFragment newInstance() {
        return new PassengerDriverConnectedFragment();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMarker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerDriverConnectedBinding.inflate(inflater, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.driverMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerTransactionViewModel.class);
        transcation = viewModel.getCurrentTranscation();
        driver = viewModel.getDriver();
        if (transcation.status == 201) isDriverReached = true;
        initUI();
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
    public void onLocationUpdateEvent(DriverLocation event) {
        if (mMap != null) {
            if (transcation != null && driver != null) {
                driverPosition = new LatLng(Double.parseDouble(event.location.latitude),
                        Double.parseDouble(event.location.longitude));
                if (!isDriverReached) {
                    viewModel.searchRoute(driverPosition);
                } else {
                    viewModel.trackLocation(driverPosition);
                }
                Log.i("location update event", "map update");
            }
        }
    }

    private void initUI() {
        //Initialize the ride and driver info
        binding.driverUsernameText.setText(driver.username);
        if (transcation.taxi != null)
            binding.taxiPlatenumberText.setText(transcation.taxi.platenumber);
        String route = "From " + transcation.startAddr + " To " + transcation.desAddr;
        binding.routeText.setText(route);

        //Initialize the expandable item
        final ExpandableRelativeLayout expandBlock = binding.getRoot().findViewById(R.id.expandableLayout);
        expandBlock.collapse();
        expandBlock.setInterpolator(new AccelerateDecelerateInterpolator());
        binding.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExpanded) {
                    Drawable collapseIcon = getResources().getDrawable(R.drawable.ic_collapse_24px);
                    int h = collapseIcon.getIntrinsicHeight();
                    int w = collapseIcon.getIntrinsicWidth();
                    collapseIcon.setBounds(0, 0, w, h);
                    binding.toggleButton.setCompoundDrawables(null, collapseIcon, null, null);
                } else {
                    Drawable expandIcon = getResources().getDrawable(R.drawable.ic_expand_24px);
                    int h = expandIcon.getIntrinsicHeight();
                    int w = expandIcon.getIntrinsicWidth();
                    expandIcon.setBounds(0, 0, w, h);
                    binding.toggleButton.setCompoundDrawables(null, expandIcon, null, null);
                }
                isExpanded = !isExpanded;
                expandBlock.toggle();
            }
        });

        viewModel.getRoute().observe(this, response -> {
            if (response.isSuccessful() && mMap != null) {
                // Draw predicted route
                lines = RouteDrawingUtils.getGoogleMapPolyline(response.body);
                mMap.addPolyline(lines);
                locationOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
                locationOptions.position(driverPosition);
                // Add predicted arrival time
                binding.arrivalTimeText.setText(String.valueOf(response.body.routes.get(0).legs.get(0).duration.value / 60) + " minutes");
            } else {
                Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
            }
        });

        // Call the driver
        binding.callDriverButton.setOnClickListener(view -> {
            String phone = driver.phonenumber;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });

        // Confirm
        binding.confirmButton.setOnClickListener(view -> {
            // Ask the passenger whether they want to confirm the ride
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Ride")
                    .setMessage("The driver will be allowed to charge After confirmed" +
                            "\n Are you sure?")
                    .setPositiveButton("OK", ((dialogInterface, i) -> {
                        apiService.passengerConfirmRide(transcation.id)
                                .enqueue(new Callback<StandardResponse>() {
                                    @Override
                                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                        if (response.isSuccessful()) {
                                            if (response.body().success == 1) {
                                                Log.d("Confirm", "success");
                                                Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            } else {
                                                Log.d("Confirm", "invalid");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                                        Toast.makeText(getContext(), "Cannot connected to the internet\n" +
                                                "Please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()))
                    .show();
        });

        // Report No show
        binding.reportNoShowButton.setOnClickListener(view -> {
            apiService.cancelOrder(transcation.id).enqueue(new Callback<StandardResponse>() {
                @Override
                public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body().success == 1) {
                            Toast.makeText(getContext(), "Cancel successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "You cannot cancel at this stage", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Cannot connected to the internet\n" +
                            "Please try again later", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //Cancel Button
        binding.cancelButton.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure to cancel the order?")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        apiService.cancelOrder(transcation.id).enqueue(new Callback<StandardResponse>() {
                            @Override
                            public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                if(response.isSuccessful()) {
                                    if(response.body().success == 1) {
                                        Toast.makeText(getContext(), "Cancel successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getContext(), "You cannot cancel at this stage", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<StandardResponse> call, Throwable t) {
                                Toast.makeText(getContext(), "Cannot connected to the internet\n" +
                                        "Please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        });
    }

    /**
     * Driver has reached the pick-up point. The time counter has started.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        isDriverReached = true;
        String text = "Your Driver has reached the pick-up point\n Please arrive within"
                + String.format("%02d", event.getMinute())
                + ":" + String.format("%02d", event.getSecond());
        binding.arrivalTimeText.setText(text);
    }

    private void setMarker() {
        if (transcation != null) {
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

            LatLng latlng = new LatLng(Double.parseDouble(transcation.startLat), Double.parseDouble(transcation.startLong));
            Log.i("Passenger Found", latlng.latitude + ", " + latlng.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            if (lines != null && locationOptions != null) {
                mMap.addPolyline(lines);
                mMap.addMarker(locationOptions);
            }
        }
    }
}