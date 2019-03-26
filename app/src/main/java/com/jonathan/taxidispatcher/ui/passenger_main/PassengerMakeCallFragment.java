package com.jonathan.taxidispatcher.ui.passenger_main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.PlaceResource;
import com.jonathan.taxidispatcher.databinding.FragmentPassengerMakeCallBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.PassengerMainViewModelFactory;
import com.jonathan.taxidispatcher.utils.GPSPromptEnabled;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class PassengerMakeCallFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Injectable {

    public static final int PICK_UP_REQUEST_CODE = 1;
    public static final int DESTINATION_REQUEST_CODE = 2;
    public static final String START_LAT = "startLat";
    public static final String START_LONG = "startLong";
    public static final String DES_LAT = "desLat";
    public static final String DES_LONG = "desLong";
    public static final String START_ADDR = "startAddr";
    public static final String DES_ADDR = "desAddr";
    public static final String TIME = "time";
    private static final long INTERVAL = 1000;

    FragmentPassengerMakeCallBinding binding;

    GoogleMap mMap;
    PlacesClient placesClient;
    GoogleApiClient apiClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    boolean isPositioning = false;
    ProgressDialog positioningDialog;

    PassengerMainViewModel viewModel;

    @Inject
    PassengerMainViewModelFactory factory;

    Context applicationContext;

    LatLng[] marker = new LatLng[2];
    private LatLng tempClickMarker;
    private LatLng currentPosition;

    public PassengerMakeCallFragment() {
        // Required empty public constructor
    }

    public static PassengerMakeCallFragment newInstance() {
        return new PassengerMakeCallFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPassengerMakeCallBinding.inflate(inflater, container, false);

        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMap);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        applicationContext = getContext();
        positioningDialog = new ProgressDialog(applicationContext);
        positioningDialog.setTitle("Wait for positioning");

        apiClient = new GoogleApiClient.Builder(applicationContext)
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
                    moveCamera(currentPosition);
                    if(positioningDialog != null)   positioningDialog.hide();
                    isPositioning = true;
                }
            }
        };

        PassengerMakeCallFragmentPermissionsDispatcher.askForRuntimePermissionWithPermissionCheck(this);
        initPlaceClient();
        initSearchFunction();
        updateUI();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void askForRuntimePermission() {
    }

    @Override
    public void onResume() {
        super.onResume();
        GPSPromptEnabled.promptUserEnabledGPS(getActivity());
        if (!apiClient.isConnected()) apiClient.connect();
        positioningDialog.show();
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
        if(positioningDialog != null) positioningDialog.hide();
        if(apiClient.isConnected()) apiClient.disconnect();
        if(fusedLocationProviderClient != null) fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void initPlaceClient() {
        Places.initialize(applicationContext, "AIzaSyBwJyQDS_1ZZfic_OLFdB0q7UZC11B9vw4");
        placesClient = Places.createClient(applicationContext);
    }

    private void initSearchFunction() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME);
        Intent searchPlaceIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("HK")
                .build(applicationContext);
        binding.pickUpPointSelector.setOnClickListener(view -> {
            startActivityForResult(searchPlaceIntent, PICK_UP_REQUEST_CODE);
        });
        binding.destinationPointSelector.setOnClickListener(view -> {
            startActivityForResult(searchPlaceIntent, DESTINATION_REQUEST_CODE);
        });
        binding.taxiRequestButton.setOnClickListener(toTransactionDetails);
        binding.timePicker.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            int minute = c.get(Calendar.MINUTE);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            new TimePickerDialog(applicationContext, (pickerView, targetHour, targetMinute) -> {
                binding.timePicker.setText(targetHour + ":" + targetMinute);
                binding.timePicker.setTextSize(18);
            }, hour, minute, false).show();
        });
    }

    private void updateUI() {
        viewModel = ViewModelProviders.of(getActivity(), factory).get(PassengerMainViewModel.class);
        viewModel.getDestinationPointResponse().observe(this, new Observer<ApiResponse<PlaceResource>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<PlaceResource> response) {
                if (response.isSuccessful()) {
                    if (response.body.results.size() > 0) {
                        binding.progressBar.setVisibility(View.GONE);
                        setLocationMarker("end", tempClickMarker);
                        setLocationText("end", response.body.results.get(0).name);
                    } else
                        Toast.makeText(applicationContext, "Location Not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(applicationContext, "Network connection fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
        viewModel.getPickupPointResponse().observe(this, new Observer<ApiResponse<PlaceResource>>() {
            @Override
            public void onChanged(@Nullable ApiResponse<PlaceResource> response) {
                if (response.isSuccessful()) {
                    if (response.body.results.size() > 0) {
                        binding.progressBar.setVisibility(View.GONE);
                        setLocationText("start", response.body.results.get(0).name);
                        setLocationMarker("start", tempClickMarker);
                    } else
                        Toast.makeText(applicationContext, "Location Not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(applicationContext, "Network connection fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.selectCurrentPositionButton.setOnClickListener(view -> {
            if(currentPosition != null) {
                tempClickMarker = currentPosition;
                binding.progressBar.setVisibility(View.VISIBLE);
                viewModel.searchPickupPoint(currentPosition);
            } else {
                Toast.makeText(applicationContext, "Please wait for positioning", Toast.LENGTH_SHORT).show();
            }
        });
        binding.setViewmodel(viewModel);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(setMarkerOnMap);
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_UP_REQUEST_CODE:
                    Place start = Autocomplete.getPlaceFromIntent(data);
                    setLocationText("start", start.getName());
                    setLocationMarker("start", start.getLatLng());
                    break;
                case DESTINATION_REQUEST_CODE:
                    Place end = Autocomplete.getPlaceFromIntent(data);
                    setLocationText("end", end.getName());
                    setLocationMarker("end", end.getLatLng());
                    break;
                default:
                    break;
            }
        }
    }

    private void setLocationText(String type, String address) {
        if (type.equals("start")) {
            binding.pickUpPointSelector.setText(address);
        }
        if (type.equals("end")) {
            binding.destinationPointSelector.setText(address);
        }
    }

    private void setLocationMarker(String type, LatLng latlng) {
        mMap.clear();
        if (type.equals("start")) {
            marker[0] = latlng;
        } else if (type.equals("end")) {
            marker[1] = latlng;
        }
        MarkerOptions originOptions = new MarkerOptions();
        originOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        originOptions.title("Origin");

        MarkerOptions destinationOptions = new MarkerOptions();
        destinationOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        destinationOptions.title("Destination");

        if (marker[0] != null) {
            originOptions.position(marker[0]);
            mMap.addMarker(originOptions);
        }
        if (marker[1] != null) {
            destinationOptions.position(marker[1]);
            mMap.addMarker(destinationOptions);
        }
    }

    private GoogleMap.OnMapClickListener setMarkerOnMap = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            tempClickMarker = latLng;
            AlertDialog dialog = new AlertDialog.Builder(applicationContext)
                    .setPositiveButton("Origin", setOrigin)
                    .setNeutralButton("Destination", setDestination)
                    .setTitle("Set as ...")
                    .setCancelable(true)
                    .create();
            dialog.show();
        }
    };

    DialogInterface.OnClickListener setOrigin = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            viewModel.searchPickupPoint(tempClickMarker);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    };

    DialogInterface.OnClickListener setDestination = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            viewModel.searchDestination(tempClickMarker);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PassengerMakeCallFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        switch (requestCode) {
            case GPSPromptEnabled.REQUEST_CHECK_SETTINGS:
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        Log.d("location servie", "connected");
        try {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch(SecurityException e) {
            PassengerMakeCallFragmentPermissionsDispatcher.askForRuntimePermissionWithPermissionCheck(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        fusedLocationProviderClient = null;
    }

    private void moveCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }

    private View.OnClickListener toTransactionDetails = view -> {
        if (marker[0] != null && marker[1] != null) {
            Bundle dataBundle = new Bundle();
            dataBundle.putDouble(START_LAT, marker[0].latitude);
            dataBundle.putDouble(START_LONG, marker[0].longitude);
            dataBundle.putDouble(DES_LAT, marker[1].latitude);
            dataBundle.putDouble(DES_LONG, marker[1].longitude);
            dataBundle.putString(START_ADDR, binding.pickUpPointSelector.getText().toString());
            dataBundle.putString(DES_ADDR, binding.destinationPointSelector.getText().toString());
            dataBundle.putString(TIME, binding.timePicker.getText().toString());
            viewModel.transcationData.setValue(dataBundle);
            PassengerMainActivity.toConfirmFragment();
        } else {
            Toast.makeText(applicationContext, "Please select both the pick-up and destination point",
                    Toast.LENGTH_SHORT).show();
        }
    };
}
