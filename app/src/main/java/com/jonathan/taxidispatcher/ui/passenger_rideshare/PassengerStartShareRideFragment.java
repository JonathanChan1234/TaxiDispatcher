package com.jonathan.taxidispatcher.ui.passenger_rideshare;


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
import com.jonathan.taxidispatcher.databinding.FragmentPassegnerStartShareRideBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.event.Location;
import com.jonathan.taxidispatcher.event.LocationUpdateEvent;
import com.jonathan.taxidispatcher.factory.PassengerShareRideViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

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

    public PassengerStartShareRideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        if(viewModel.getRideShareTranscation() != null) {
            transcation = viewModel.getRideShareTranscation();
            if(transcation.first_transaction.user.id == Session.getUserId(getContext())) {
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
        if(transcation.taxi != null)    {
            binding.taxiPlatenumberText.setText("Plate Number:" + transcation.taxi.platenumber);
        }
        String ownRoute = "From " + ownRide.startAddr + " To " + ownRide.desAddr;
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
                if(!isExpanded) {
                    Drawable collapseIcon = getResources().getDrawable(R.drawable.ic_collapse_24px);
                    int h = collapseIcon.getIntrinsicHeight();
                    int w = collapseIcon.getIntrinsicWidth();
                    collapseIcon.setBounds( 0, 0, w, h );
                    binding.toggleButton.setCompoundDrawables(null, collapseIcon, null, null);
                } else {
                    Drawable expandIcon = getResources().getDrawable(R.drawable.ic_expand_24px);
                    int h = expandIcon.getIntrinsicHeight();
                    int w = expandIcon.getIntrinsicWidth();
                    expandIcon.setBounds( 0, 0, w, h );
                    binding.toggleButton.setCompoundDrawables(null, expandIcon, null, null);
                }
                isExpanded = !isExpanded;
                expandBlock.toggle();
            }
        });

        viewModel.getRoute().observe(this, response -> {
            if(response.isSuccessful() && mMap != null) {
                // Draw predicted route
                lines = RouteDrawingUtils.getGoogleMapPolyline(response.body);
                setMarker();
                // Add predicted arrival time
                binding.arrivalTimeText.setText( String.valueOf(response.body.routes.get(0).legs.get(0).duration.value / 60) + " minutes");
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

        // Message the driver
        binding.messageDriverButton.setOnClickListener(view -> {

        });

        // Report No show
        binding.reportNoShowButton.setOnClickListener(view -> {

        });

        // Driver already reach the pick-up point
        if(transcation.status == 201) {
            binding.reportNoShowButton.setEnabled(false);
            binding.cancelButton.setEnabled(false);
            isDriverReached = true;
        }
        Log.d("Start Ride", "set marker");
        setMarker();
    }

    private void setMarker() {
        Log.d("Start Ride", "set marker");
        if (transcation != null && mMap != null) {
            Log.d("Start Ride", "set marker");
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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));

            if(driverPosition != null && lines != null) {
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
        if(mMap != null) {
            if(transcation != null) {
                if(driverMarker != null) {
                    driverMarker.setPosition(new LatLng(Double.parseDouble(event.latitude),
                            Double.parseDouble(event.longitude)));
                }
                if(!isDriverReached) {
                    viewModel.searchRoute(driverPosition);
                } else {
                    viewModel.trackLocation(driverPosition);
                }
                Log.i("location update event", "map update");
            }
        }
    }

}
