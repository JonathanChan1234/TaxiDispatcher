package com.jonathan.taxidispatcher.ui.driver_transaction;


import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
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
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.RideShareTransactionResource;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.databinding.FragmentDriverStartShareRideBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverTransactionViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.MapUtils;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverStartShareRideFragment extends Fragment
        implements Injectable, OnMapReadyCallback {

    public FragmentDriverStartShareRideBinding binding;
    @Inject
    DriverTransactionViewModelFactory factory;
    @Inject
    APIInterface apiService;
    @Inject
    GoogleAPIInterface googleAPIService;

    DriverTransactionViewModel viewModel;
    RideShareTransaction transaction;
    RideShare firstTranscation;
    RideShare secondTranscation;
    GoogleMap mMap;
    ExpandableLinearLayout expandBlock;

    //Location
    private int updateCount = 0;
    private Location previousLocation;
    Handler handler;
    Runnable firstTimerRunnable;
    Runnable secondTimerRunnable;
    private boolean isExpanded = false;
    private boolean firstStarted = false;
    private boolean secondStarted = false;

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
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverTransactionViewModel.class);
        handler = new MyHandler(this);
        initUI();
    }

    private void initUI() {
        expandBlock = binding.getRoot().findViewById(R.id.expandableLayout);
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
        if (viewModel.getRideShareResource() != null) {
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
                driverReachPickup(transaction.id, firstTranscation.id);
            });
            binding.secondReachPickupPointButton.setOnClickListener(view -> {
                driverReachPickup(transaction.id, secondTranscation.id);
            });
            binding.firstCallPassengerButton.setOnClickListener(view -> {
                callPassenger(firstTranscation.user.phonenumber);
            });
            binding.secondCallPassengerButton.setOnClickListener(view -> {
                callPassenger(secondTranscation.user.phonenumber);
            });
            binding.exitButton.setOnClickListener(view -> exitTransaction());
            binding.finishButton.setOnClickListener(view -> finishTransaction());
            uiUpdate();
            expandBlock.initLayout();
        }
    }

    private void finishTransaction() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Finish Ride")
                .setMessage("You can only finish the ride only when both passenger are in timeout state/ confirmed\n" +
                "If you insist to leave the ride, you can click the exit button.")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    apiService.driverFinishShareRide(transaction.id)
                            .enqueue(new Callback<StandardResponse>() {
                                @Override
                                public void onResponse(Call<StandardResponse> call, Response<StandardResponse> response) {
                                    if(response.body().success == 1) {
                                        resetStatus();
                                    } else {
                                        Toast.makeText(getContext(), "You are not allowed to finish the ride at this stage",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<StandardResponse> call, Throwable t) {
                                    Toast.makeText(getContext(), "Cannot connect to the internet", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.cancel();
                }))
                .show();
    }

    private void exitTransaction() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Cancel Ride")
                .setMessage("Cancellation of Ride will degrade your rating?")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    apiService.driverExitShareRide(transaction.id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    resetStatus();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getContext(), "Cannot connect to the internet", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.cancel();
                }))
                .show();
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

    private void uiUpdate() {
        if(transaction.first_confirmed == 101 ||
                transaction.first_confirmed == 102 ||
                transaction.first_confirmed == 200) {
            binding.firstReachPickupPointButton.setEnabled(false);
        }
        if(transaction.second_confirmed == 101 ||
                transaction.second_confirmed == 102 ||
                transaction.second_confirmed == 200) {
            binding.secondReachPickupPointButton.setEnabled(false);
        }

        if(transaction.first_confirmed == 102) {
            binding.firstTimeCounterText.setVisibility(View.VISIBLE);
            binding.firstTimeCounterText.setText("Timeout");
        }

        if(transaction.second_confirmed == 102) {
            binding.secondTimeCounterText.setVisibility(View.VISIBLE);
            binding.secondTimeCounterText.setText("Timeout");
        }

        if(transaction.first_confirmed == 400) {
            binding.firstTimeCounterText.setBackgroundResource(R.color.red);
            binding.firstTimeCounterText.setVisibility(View.VISIBLE);
            binding.firstTimeCounterText.setText("Cancelled");
            binding.firstTimeCounterText.setTextColor(Color.BLACK);
            firstStarted = false;
        }
        if(transaction.second_confirmed == 400) {
            binding.secondTimeCounterText.setBackgroundResource(R.color.red);
            binding.secondTimeCounterText.setVisibility(View.VISIBLE);
            binding.secondTimeCounterText.setText("Cancelled");
            binding.secondTimeCounterText.setTextColor(Color.BLACK);
            secondStarted = false;
        }
    }

    private void initTimer() {
        if(transaction.first_confirmed == 101) {
            binding.firstReachPickupPointButton.setEnabled(false);
            firstStarted = true;
            firstTimerRunnable = ()-> {
                Calendar timeoutTime = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ENG"));
                try {
                    timeoutTime.setTime(format.parse(transaction.first_reach_time));
                    timeoutTime.add(Calendar.MINUTE, 5);
                    timeoutTime.getTime().getTime();
                    Calendar calendar = Calendar.getInstance();
                    int timeDifference = (int) (timeoutTime.getTime().getTime() - calendar.getTime().getTime());
                    int count = timeDifference / 1000;
                    while (count > 0 && firstStarted) {
                        try {
                            Thread.sleep(1000);
                            count--;
                            int minute = (count % 3600) / 60;
                            int second = (count % 60);
                            String text = "Time Limit: " +
                                    String.format(new Locale("ENG"), "%02d", minute) +
                                    ":" + String.format(new Locale("ENG"), "%02d", second);
                            Log.d("timetext", text);
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = text;
                            handler.sendMessage(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            };
            Thread thread1 = new Thread(firstTimerRunnable);
            thread1.start();
        }
        if(transaction.second_confirmed == 101) {
            binding.secondReachPickupPointButton.setEnabled(false);
            secondStarted = true;
            secondTimerRunnable = ()-> {
                Calendar timeoutTime = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ENG"));
                try {
                    timeoutTime.setTime(format.parse(transaction.second_reach_time));
                    timeoutTime.add(Calendar.MINUTE, 5);
                    timeoutTime.getTime().getTime();
                    Calendar calendar = Calendar.getInstance();
                    int timeDifference = (int) (timeoutTime.getTime().getTime() - calendar.getTime().getTime());
                    int count = timeDifference / 1000;
                    while (count > 0 && secondStarted) {
                        try {
                            Thread.sleep(1000);
                            count--;
                            int minute = (count % 3600) / 60;
                            int second = (count % 60);
                            String text = "Time Limit: " +
                                    String.format(new Locale("ENG"), "%02d", minute) +
                                    ":" + String.format(new Locale("ENG"), "%02d", second);
                            Log.d("timetext", text);
                            Message msg = Message.obtain();
                            msg.what = 2;
                            msg.obj = text;
                            handler.sendMessage(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            };
            Thread thread2 = new Thread(secondTimerRunnable);
            thread2.start();
        }
    }

    private void driverReachPickup(int orderId, int rideshareId) {
        apiService.driverReachPickupShareRide(orderId, rideshareId)
                .enqueue(new Callback<RideShareTransactionResource>() {
                    @Override
                    public void onResponse(Call<RideShareTransactionResource> call, Response<RideShareTransactionResource> response) {
                        if(response.body() != null) {
                            transaction = response.body().data;
                            initTimer();
                        }
                    }

                    @Override
                    public void onFailure(Call<RideShareTransactionResource> call, Throwable t) {
                        Toast.makeText(getContext(), "No Network connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void callPassenger(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setTrafficEnabled(true);
        MapUtils.updateLocationUI(mMap);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdate(Location location) {
        if(mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    15
            ));
        }
        if(previousLocation != null) {
            if(previousLocation.getLongitude() != location.getLongitude() ||
                    previousLocation.getLatitude() != location.getLatitude()) {
                if (transaction != null) {
                    updateCount += (++updateCount) % 4; // Limit the route update rate
                    if (updateCount == 3) {
                        String origin = location.getLatitude() + "," + location.getLongitude();;
                        String first_destination, second_destination;
                        if (transaction.first_confirmed < 101
                                || transaction.second_confirmed < 101) { //Both reached
                            first_destination = transaction.first_transaction.startLat + "," + transaction.first_transaction.startLong;
                            second_destination = transaction.second_transaction.startLat + "," + transaction.second_transaction.startLong;
                        } else {
                            first_destination = transaction.first_transaction.desLat + "," + transaction.first_transaction.desLong;
                            second_destination = transaction.second_transaction.desLat + "," + transaction.second_transaction.desLong;
                        }

                        googleAPIService.getRouteWithWaypoints(origin, first_destination, second_destination, Constants.API_key)
                                .enqueue(new Callback<DirectionModel>() {
                                    @Override
                                    public void onResponse(Call<DirectionModel> call, Response<DirectionModel> response) {
                                        if(response.body() != null) {
                                            if (response.body().status.equals("OK")) {
                                                updateDriverMap(RouteDrawingUtils.getNavigationLine(response.body()), location);
                                            }
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
        }
        previousLocation = location;
    }

    private void updateDriverMap(PolylineOptions lines, Location location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addPolyline(lines);
            setMarker();
        }
    }

    private void setMarker() {
        if (firstTranscation != null && secondTranscation != null) {
            MarkerOptions ownPickup = new MarkerOptions();
            ownPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            ownPickup.title("1st Pick-up");
            ownPickup.position(new LatLng(Double.parseDouble(firstTranscation.startLat), Double.parseDouble(firstTranscation.startLong)));
            mMap.addMarker(ownPickup);

            MarkerOptions ownDes = new MarkerOptions();
            ownDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            ownDes.title("1st Destination");
            ownDes.position(new LatLng(Double.parseDouble(firstTranscation.desLat), Double.parseDouble(firstTranscation.desLong)));
            mMap.addMarker(ownDes);

            MarkerOptions passengerPickup = new MarkerOptions();
            passengerPickup.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            passengerPickup.title("2nd Share Ride Pick-up");
            passengerPickup.position(new LatLng(Double.parseDouble(secondTranscation.startLat), Double.parseDouble(secondTranscation.startLong)));
            mMap.addMarker(passengerPickup);

            MarkerOptions passengerDes = new MarkerOptions();
            passengerDes.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            passengerDes.title("2nd Destination");
            passengerDes.position(new LatLng(Double.parseDouble(secondTranscation.desLat), Double.parseDouble(secondTranscation.desLong)));
            mMap.addMarker(passengerDes);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        initTimer();
        uiUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        firstStarted = false;
        secondStarted = false;
    }

    static class MyHandler extends Handler {
        WeakReference<DriverStartShareRideFragment> reference;

        MyHandler(DriverStartShareRideFragment fragment) {
            reference = new WeakReference<DriverStartShareRideFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            DriverStartShareRideFragment fragment = reference.get();
            switch (msg.what) {
                case 1:
                    fragment.binding.firstTimeCounterText.setVisibility(View.VISIBLE);
                    fragment.binding.firstTimeCounterText.setText((String) msg.obj);
                    break;
                case 2:
                    fragment.binding.secondTimeCounterText.setVisibility(View.VISIBLE);
                    fragment.binding.secondTimeCounterText.setText((String) msg.obj);
                    break;
            }
        }
    }
}
