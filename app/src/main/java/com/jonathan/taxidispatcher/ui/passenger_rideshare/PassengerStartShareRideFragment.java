package com.jonathan.taxidispatcher.ui.passenger_rideshare;


import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.databinding.FragmentPassegnerStartShareRideBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.Location;
import com.jonathan.taxidispatcher.event.LocationUpdateEvent;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.factory.PassengerShareRideViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_transaction.RatingActivity;
import com.jonathan.taxidispatcher.utils.MapUtils;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerStartShareRideFragment extends Fragment implements OnMapReadyCallback, Injectable {
    boolean isExpanded = false;
    GoogleMap mMap;
    LatLng driverPosition;
    FragmentPassegnerStartShareRideBinding binding;

    @Inject
    PassengerShareRideViewModelFactory factory;
    PassengerRideShareViewModel viewModel;

    @Inject
    APIInterface apiService;

    RideShareTransaction transcation;
    RideShare ownRide, passengerRide;
    Boolean isDriverReached = false;
    MarkerOptions locationOptions = new MarkerOptions();
    Marker driverMarker;
    PolylineOptions lines = null;
    String ownRoute;

    public PassengerStartShareRideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapUtils.updateLocationUI(googleMap);
        setMarker();
    }

    public static PassengerStartShareRideFragment newInstance() {
        return new PassengerStartShareRideFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassegnerStartShareRideBinding.inflate(inflater, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.driverMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerRideShareViewModel.class);
        if (viewModel.getRideShareTranscation() != null) {
            transcation = viewModel.getRideShareTranscation();
            if (transcation.first_transaction.user.id == Session.getUserId(getContext())) {
                ownRide = transcation.first_transaction;
                passengerRide = transcation.second_transaction;
            } else {
                ownRide = transcation.second_transaction;
                passengerRide = transcation.first_transaction;
            }
            initUI();
        }
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

    private void initUI() {
        //Initialize the ride and driver info
        binding.driverUsernameText.setText("Username: " + transcation.driver.username);
        if (transcation.taxi != null) {
            binding.taxiPlatenumberText.setText("Plate Number:" + transcation.taxi.platenumber);
        }
        ownRoute = "From " + ownRide.startAddr + " To " + ownRide.desAddr;
        binding.routeText.setText(ownRoute);
        String passengerRoute = "From " + passengerRide.startAddr + " To " + passengerRide.desAddr;
        binding.passengerRouteText.setText(passengerRoute);
        binding.passengerUsernameText.setText(passengerRide.user.username);

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
                setMarker();
                // Add predicted arrival time
                binding.arrivalTimeText.setText(String.valueOf(response.body.routes.get(0).legs.get(0).duration.value / 60) + " minutes");
            } else {
                Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
            }
        });

        // Call the driver
        binding.callDriverButton.setOnClickListener(view -> {
            String phone = transcation.driver.phonenumber;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        });

        // Confirm the ride
        binding.confirmRideButton.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Transaction Confirmation")
                    .setMessage("Are you sure to confirm this ride?")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        apiService.passengerConfirmShareRide(transcation.id, ownRide.id)
                                .enqueue(new Callback<StandardResponse>() {
                                    @Override
                                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                        if (response.body() != null) {
                                            // Go the Rating Activity
                                            if (response.body().success == 1) {
                                                Intent intent = new Intent(getActivity(), RatingActivity.class);
                                                intent.putExtra("route", ownRoute);
                                                if (transcation != null) {
                                                    intent.putExtra("driverId", transcation.driver.id);
                                                    intent.putExtra("driver", transcation.driver.username);
                                                }
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                                        Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }))
                    .show();
        });

        // Report No show
        binding.reportNoShowButton.setOnClickListener(view -> {

        });

        if (transcation.first_confirmed == 201) {
            isDriverReached = true;
        }

        binding.cancelButton.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Cancel the Transaction")
                    .setMessage("Are you sure to cancel this ride?")
                    .setPositiveButton("OK", ((dialogInterface, i) -> {
                        apiService.cancelShareRideOrder(ownRide.id)
                                .enqueue(new Callback<StandardResponse>() {
                                    @Override
                                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(getContext(), "Cancelled successfully", Toast.LENGTH_SHORT).show();
                                            toMainActivity();
                                        } else {
                                            Toast.makeText(getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                                        Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()))
                    .show();
        });
        Log.d("Start Ride", "set marker");
        setMarker();
    }

    private void toMainActivity() {
        Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setMarker() {
        if (transcation != null && mMap != null) {
            Log.d("Set Marker", "set marker");
            mMap.clear();
            MarkerOptions ownPickup = new MarkerOptions();
            ownPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            ownPickup.title("Pick-up");
            ownPickup.position(new LatLng(Double.parseDouble(ownRide.startLat), Double.parseDouble(ownRide.startLong)));
            mMap.addMarker(ownPickup);

            MarkerOptions ownDes = new MarkerOptions();
            ownDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            ownDes.title("Destination");
            ownDes.position(new LatLng(Double.parseDouble(ownRide.desLat), Double.parseDouble(ownRide.desLong)));
            mMap.addMarker(ownDes);

            MarkerOptions passengerPickup = new MarkerOptions();
            passengerPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            passengerPickup.title("Share Ride Pick-up");
            passengerPickup.position(new LatLng(Double.parseDouble(passengerRide.startLat), Double.parseDouble(passengerRide.startLong)));
            mMap.addMarker(passengerPickup);

            MarkerOptions passengerDes = new MarkerOptions();
            passengerDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            passengerDes.title("Destination");
            passengerDes.position(new LatLng(Double.parseDouble(passengerRide.desLat), Double.parseDouble(passengerRide.desLong)));
            mMap.addMarker(passengerDes);

            LatLng latlng = new LatLng(Double.parseDouble(ownRide.startLat), Double.parseDouble(ownRide.startLong));
            Log.i("Passenger Found", latlng.latitude + ", " + latlng.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16f));

            if (driverPosition != null && lines != null) {
                locationOptions.position(driverPosition);
                locationOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
                locationOptions.title("Driver");
                driverMarker = mMap.addMarker(locationOptions);
                mMap.addPolyline(lines);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdateEvent(Location event) {
        driverPosition = new LatLng(Double.parseDouble(event.latitude),
                Double.parseDouble(event.longitude));
        if (mMap != null) {
            if (transcation != null) {
                if (driverMarker != null) {
                    driverMarker.setPosition(new LatLng(Double.parseDouble(event.latitude),
                            Double.parseDouble(event.longitude)));
                }
                if (!isDriverReached) {
                    viewModel.searchRoute(driverPosition);
                } else {
                    viewModel.trackLocation(driverPosition);
                }
                Log.i("location update event", "map update");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text =  "Please reach within " + String.format(new Locale("ENG"), "%02d", event.getMinute()) +
                ":" + String.format(new Locale("ENG"), "%02d", event.getSecond());
        binding.timeCounterText.setText(text);
    }

}
