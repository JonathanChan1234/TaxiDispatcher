package com.jonathan.taxidispatcher.ui.passenger_main;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerConfirmBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.PassengerMainViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerRideShareActivity;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerTransactionActivity;
import com.jonathan.taxidispatcher.utils.RouteDrawingUtils;

import javax.inject.Inject;

public class PassengerConfirmFragment extends Fragment
        implements OnMapReadyCallback, Injectable {
    FragmentPassengerConfirmBinding binding;
    PassengerMainViewModel viewModel;
    @Inject
    PassengerMainViewModelFactory factory;

    GoogleMap mMap;

    LatLng origin, destination;
    Bundle dataBundle;

    public PassengerConfirmFragment() {
        // Required empty public constructor
    }

    public static PassengerConfirmFragment newInstance() {
        return new PassengerConfirmFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerConfirmBinding.inflate(inflater, container, false);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.orderMap);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerMainViewModel.class);
        initUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (origin != null && destination != null) {
            Log.d("moveCamera", String.valueOf(origin));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 14));
            setMarker();
        }
    }

    private void drawRoute(DirectionModel model) {
        PolylineOptions lineOptions = RouteDrawingUtils.getGoogleMapPolyline(model);
        mMap.addPolyline(lineOptions);
    }

    private void setMarker() {
        MarkerOptions originOptions = new MarkerOptions();
        originOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        originOptions.title("Origin");
        originOptions.position(origin);
        mMap.addMarker(originOptions);

        MarkerOptions destinationOptions = new MarkerOptions();
        destinationOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        destinationOptions.title("Destination");
        destinationOptions.position(destination);
        mMap.addMarker(destinationOptions);
    }

    private void initUI() {
        dataBundle = viewModel.getTranscationData().getValue();
        // Initialize location data
        if (dataBundle != null) {
            binding.pickUpPointText.setText(dataBundle.getString(PassengerMakeCallFragment.START_ADDR));
            binding.destinationText.setText(dataBundle.getString(PassengerMakeCallFragment.DES_ADDR));

            origin = new LatLng(dataBundle.getDouble(PassengerMakeCallFragment.START_LAT),
                    dataBundle.getDouble(PassengerMakeCallFragment.START_LONG));
            destination = new LatLng(dataBundle.getDouble(PassengerMakeCallFragment.DES_LAT),
                    dataBundle.getDouble(PassengerMakeCallFragment.DES_LONG));

            viewModel.isLoading.set(true);
            binding.progressBar.setVisibility(View.VISIBLE);
            initUIUpdate();

            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
            }
            binding.makeTransactionButton.setOnClickListener(makeTransaction);
        } else {
            PassengerMainActivity.toPassengerMakeCallFragment();
        }

    }

    private View.OnClickListener makeTransaction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Confirmation")
                    .setMessage("Are you sure to make this call?")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        if (binding.shareRideButton.isChecked()) {
                            makeShareRideTransaction();
                        } else {
                            makePersonalRideTransaction();
                        }
                    }))
                    .setNegativeButton("No", ((dialogInterface, i) -> {
                        dialogInterface.cancel();
                    }))
                    .show();
        }
    };

    private void makePersonalRideTransaction() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.makeTransaction(Session.getUserId(getContext()),
                dataBundle.getDouble(PassengerMakeCallFragment.START_LAT),
                dataBundle.getDouble(PassengerMakeCallFragment.START_LONG),
                dataBundle.getString(PassengerMakeCallFragment.START_ADDR),
                dataBundle.getDouble(PassengerMakeCallFragment.DES_LAT),
                dataBundle.getDouble(PassengerMakeCallFragment.DES_LONG),
                dataBundle.getString(PassengerMakeCallFragment.DES_ADDR),
                dataBundle.getString(PassengerMakeCallFragment.TIME),
                "")
                .observe(PassengerConfirmFragment.this, response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        if (response.body != null) {
                            Session.saveCurrentTransaction(getContext(), response.body.data.id, response.body.data);
                            Intent intent = new Intent(getActivity(), PassengerTransactionActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(getContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeShareRideTransaction() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.makeRideShare(Session.getUserId(getContext()),
                dataBundle.getDouble(PassengerMakeCallFragment.START_LAT),
                dataBundle.getDouble(PassengerMakeCallFragment.START_LONG),
                dataBundle.getString(PassengerMakeCallFragment.START_ADDR),
                dataBundle.getDouble(PassengerMakeCallFragment.DES_LAT),
                dataBundle.getDouble(PassengerMakeCallFragment.DES_LONG),
                dataBundle.getString(PassengerMakeCallFragment.DES_ADDR))
                .observe(this, rideShareResourceApiResponse -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if(rideShareResourceApiResponse != null) {
                        if(rideShareResourceApiResponse.isSuccessful()) {
                            Session.saveCurrentShareRideId(getContext(), rideShareResourceApiResponse.body.data.id);
                            Intent intent = new Intent(getActivity(), PassengerRideShareActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.d("Share Ride", rideShareResourceApiResponse.errorMessage);
                            Toast.makeText(getContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initUIUpdate() {
        viewModel.getRouteData(origin, destination)
                .observe(this, new Observer<ApiResponse<DirectionModel>>() {
                    @Override
                    public void onChanged(@Nullable ApiResponse<DirectionModel> response) {
                        viewModel.isLoading.set(false);
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            if (response.body.status.equals("OK")) {
                                String travelDistance = String.valueOf(response.body.routes.get(0).legs.get(0).distance.value / 1000) + " km";
                                String travelTime = String.valueOf(response.body.routes.get(0).legs.get(0).duration.value / 60) + " minutes";
                                binding.travelDistanceText.setText(travelDistance);
                                binding.travelTimeText.setText(travelTime);
                            } else {
                                Toast.makeText(getContext(), "Route Not found", Toast.LENGTH_LONG).show();
                                PassengerMainActivity.toPassengerMakeCallFragment();
                            }
                            drawRoute(response.body);
                        } else {
                            Toast.makeText(getContext(), "Network connection issue", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
