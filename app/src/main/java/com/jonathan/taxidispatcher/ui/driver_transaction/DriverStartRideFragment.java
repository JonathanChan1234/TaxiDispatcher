package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
import com.jonathan.taxidispatcher.api.GoogleAPIInterface;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.databinding.FragmentDriverStartRideBinding;
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

import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity.STOP_TIMER;

public class DriverStartRideFragment extends Fragment
        implements Injectable,
        OnMapReadyCallback {
    public static final String START_REACH_TIMER = "startReachTimer";
    FragmentDriverStartRideBinding binding;
    GoogleMap mMap;

    @Inject
    DriverTransactionViewModelFactory factory;
    @Inject
    APIInterface apiService;

    @Inject
    GoogleAPIInterface googleAPIService;

    DriverTransactionViewModel viewModel;
    Transcation transcation;
    boolean isExpanded = false;

    int updateCount = 0;

    public DriverStartRideFragment() {
        // Required empty public constructor
    }

    public static DriverStartRideFragment newInstance() {
        return new DriverStartRideFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverStartRideBinding.inflate(inflater, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.driverMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (transcation != null) {
            setMarker(new LatLng(Double.parseDouble(transcation.startLat),
                    Double.parseDouble(transcation.startLong)));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        if (viewModel.getTranscation() != null) {
            transcation = viewModel.getTranscation();
            initUI();
        } else {
            Toast.makeText(getContext(), "You do not have transaction", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        if (transcation != null) {
            setMarker(new LatLng(Double.parseDouble(transcation.startLat),
                    Double.parseDouble(transcation.startLong)));
            String routeText = "From " + transcation.startAddr + " To " + transcation.desAddr;
            binding.routeText.setText(routeText);
            final ExpandableRelativeLayout expandBlock = binding.getRoot().findViewById(R.id.expandableLayout);
            expandBlock.expand();
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
            binding.passengerUsernameText.setText(transcation.user.username);
            binding.callPassengerButton.setOnClickListener(view -> {
                String phone = transcation.user.phonenumber;
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            });

            binding.exitButton.setOnClickListener(view -> {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Cancel Order")
                        .setMessage("cancel the order will lower your rating?\n" +
                                "Are you sure that you want to cancel the order?")
                        .setPositiveButton("Yes", ((dialogInterface, i) -> exitTransaction()))
                        .setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()))
                        .show();
            });

            binding.reachPickupPointButton.setOnClickListener(reachPickUpPoint);
            if (transcation.status == 202) {
                binding.reachPickupPointButton.setText("Cancel Ride");
            }

            binding.finishButton.setOnClickListener(view -> {
                finishRide();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdate(Location location) {
        if(transcation != null) {
            updateCount += (++updateCount) % 4; // Limit the route update rate
            if(updateCount == 3) {
                String origin;
                String destination;
                if(transcation.status == 200) { //Driver not yet reach the pick-up points
                    origin = location.getLatitude() + "," + location.getLongitude();
                    destination = transcation.startLat + "," + transcation.startLong;
                } else {
                    origin = location.getLatitude() + "," + location.getLongitude();
                    destination = transcation.desLat + "," + transcation.desLong;
                }
                googleAPIService.getRoute(origin, destination, Constants.API_key)
                        .enqueue(new Callback<DirectionModel>() {
                            @Override
                            public void onResponse(Call<DirectionModel> call, Response<DirectionModel> response) {
                                if(response.body().status.equals("OK")) {
                                    updateDriverMap(RouteDrawingUtils.getNavigationLine(response.body()), location);
                                }
                            }

                            @Override
                            public void onFailure(Call<DirectionModel> call, Throwable t) {
                                Toast.makeText(getContext(), "Cannot connect to the Internet", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void updateDriverMap(PolylineOptions lines, Location location) {
        if(mMap != null) {
            mMap.addPolyline(lines);
            setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    View.OnClickListener reachPickUpPoint = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (transcation.status == 202) {
                cancelOrder();
            } else {
                reachPickup(transcation.id);
            }
        }
    };

    private void exitTransaction() {
        apiService.driverExitOrder(transcation.id)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().success == 1) {
                                stopTimer();
                                resetStatus();
                            } else {
                                Toast.makeText(getContext(), response.body().message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Fail to connection to the Internet", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Driver reach the pick up point
     *
     * @param id
     */
    private void reachPickup(int id) {
        viewModel.driverReachPickup(id)
                .observe(DriverStartRideFragment.this, response -> {
                    if (response.isSuccessful()) {
                        if (response.body.success == 1) {
                            binding.reachPickupPointButton.setEnabled(false);
                            //Start timer in service
                            Intent intent = new Intent(getActivity(), DriverSocketService.class);
                            intent.setAction(START_REACH_TIMER);
                            intent.putExtra("timeout", response.body.message);
                            getActivity().startService(intent);
                        } else {
                            Toast.makeText(getContext(), response.body.message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Fail to connection to the Internet", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Cancel the transaction after reach for 5 mins
     */
    private void cancelOrder() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Are you sure to cancel this ride")
                .setMessage("You are allowed to cancel the ride (Your grade will not be deduced)")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    apiService.driverCancelOrder(transcation.id)
                            .enqueue(new Callback<StandardResponse>() {
                                @Override
                                public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(getContext(), "Cancel successfully", Toast.LENGTH_SHORT).show();
                                            stopTimer();
                                            resetStatus();
                                        } else {
                                            Toast.makeText(getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<StandardResponse> call, Throwable t) {
                                    Toast.makeText(getContext(), "Fail to connection to the Internet", Toast.LENGTH_LONG).show();
                                }
                            });
                }))
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }))
                .show();
    }

    private void finishRide() {
        apiService.driverFinishRide(transcation.id)
                .enqueue(new Callback<StandardResponse>() {
                    @Override
                    public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().success == 1) {
                                Toast.makeText(getContext(), "You have completed the ride", Toast.LENGTH_SHORT).show();
                                stopTimer();
                                resetStatus();
                            } else {
                                Toast.makeText(getContext(), "Your Passenger haven't confirm the ride", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Fail to connect to the Internet", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void stopTimer() {
        Intent intent = new Intent(getActivity(), DriverSocketService.class);
        intent.setAction(STOP_TIMER);
        getActivity().startService(intent);
    }

    private void resetStatus() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Start A new Ride")
                .setMessage("Do you want to start a new ride")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    apiService.setOccupied(Session.getUserId(getContext()), 1, null)
                            .enqueue(new Callback<StandardResponse>() {
                                @Override
                                public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(getContext(), "You are ready to make another call", Toast.LENGTH_SHORT).show();
                                        }
                                        DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(),
                                                true);
                                    }
                                }

                                @Override
                                public void onFailure(Call<StandardResponse> call, Throwable t) {
                                    DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(),
                                            true);
                                }
                            });
                }))
                .setNegativeButton("No", ((dialogInterface, i) ->
                        DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(),
                                true)))
                .setOnCancelListener(dialogInterface1 -> {
                    DriverTransactionActivity.changeFragment(DriverWaitingFragment.newInstance(),
                            true);
                })
                .show();
    }

    private void setMarker(LatLng latlng) {
        if (transcation != null && mMap != null) {
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

            Log.i("Passenger Found", latlng.latitude + ", " + latlng.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    latlng, 15f));

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimerEvent(TimerEvent event) {
        String text = "Timeout at: " +
                String.format(new Locale("ENG"), "%02d", event.getMinute()) +
                ":" + String.format(new Locale("ENG"), "%02d", event.getSecond());
        binding.timeCounterText.setText(text);
        binding.timeCounterText.setTextColor(Color.RED);
        if (event.getMinute() == 0 && event.getSecond() == 0) {
            transcation.status = 202;
            binding.reachPickupPointButton.setText("Cancel Ride");
        }
    }
}

