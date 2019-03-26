package com.jonathan.taxidispatcher.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.data.model.DriverFoundResponse;
import com.jonathan.taxidispatcher.data.model.DriverTransactionType;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;
import com.jonathan.taxidispatcher.event.ShareRideDriverResponse;
import com.jonathan.taxidispatcher.event.driver.DriverShareRideFoundEvent;
import com.jonathan.taxidispatcher.event.driver.PassengerFoundEvent;
import com.jonathan.taxidispatcher.event.PassengerFoundResponse;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.event.TranscationCompletedEvent;
import com.jonathan.taxidispatcher.event.driver.SetServingEvent;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverStartRideFragment;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.DriverNotificationChannel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity.STOP_TIMER;

public class DriverSocketService extends Service
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Socket Service";
    public static final long INTERVAL = 5000;

    //Socket Event
    public static final String PASSENGER_TIMEOUT = "PassengerTimeout";
    public static final String PASSENGER_FOUND = "passengerFound";
    public static final String TRANSCATION_INVITATION = "transcationInvitation";
    public static final String CANCEL_RIDE = "cancelRide";
    private static final String EVENT_ACK = "eventAck";
    public static final String SHARE_RIDE_DRIVER_FOUND = "shareRideDriverFound";
    public static final String LOCATION_UPDATE_TO_PASSENGER = "locationUpdateToPassenger";

    private Socket mSocket;
    private boolean isConnected;

    PendingIntent goToActivityIntent;

    @Inject
    DriverSyncAdapter syncAdapter;

    //Notfication
    NotificationManager nm;
    NotificationCompat.Builder notificationBuilder_android8;
    Notification.Builder notificationBuilder;

    // Location update
    GoogleApiClient mClient;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient mLocationClient;
    LocationCallback mLocationCallback;

    SimpleDateFormat sqlTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean isTimerThreadStarted = false;

    Boolean onServe;
    String rideType;
    boolean reportPositionToPassenger = false;
    boolean driverReachThreadStart = false;

    @Inject
    APIInterface apiService;

    private final IBinder mBinder = new DriverServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AndroidInjection.inject(this);
        DriverNotificationChannel.createNotificationChannel(this);
        Log.d(TAG, "service on start command");
        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case Constants.ACTION.START_FOREGROUND_SERVICE:
                        Log.d(TAG, "foreground service started");
                        foregroundServiceStart();
                        if (isGooglePlayServiceAvailable()) {
                            createLocationRequest();
                        }
                        mClient = new GoogleApiClient.Builder(this)
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .build();
                        mClient.connect();
                        initHttpSocket();
                        startLocationUpdate();
                        EventBus.getDefault().register(this);
                        break;

                    case Constants.ACTION.STOP_FOREGROUND_SERVICE:
                        Log.d(TAG, "foreground service stopped");
                        stopForeground(true);
                        stopSelf();
                        break;

                    case DriverStartRideFragment.START_REACH_TIMER:
                        Log.d(TAG, "reach timer started");
                        driverReachThreadStart = true;
                        startDriverReachTimer(intent.getStringExtra("timeout"));
                        break;
                    case Constants.ACTION.UPDATE_DRIVER_STATUS:
                        Log.d("From messaging service", "update status");
                        updateTransactionStatus();
                        break;
                    case STOP_TIMER: //Stop the timer
                        driverReachThreadStart = false;
                        isTimerThreadStarted = false;
                        break;
                    case DriverTransactionActivity.REPORT_POSITION_TO_PASSENGER:
                        reportPositionToPassenger = true;
                        break;
                    default:
                        break;
                }
            }
        }
        return START_STICKY; //START_STICKY: resume the service right (without the intent parameter) after it is killed
    }

    // Receive from messaging service
    private void updateTransactionStatus() {
        apiService.checkDriverTransactionStatus(Session.getUserId(this))
                .enqueue(new Callback<DriverTransactionType>() {
                    @Override
                    public void onResponse(Call<DriverTransactionType> call, Response<DriverTransactionType> response) {
                        // Driver already have the current transaction
                        if(response.body() != null) {
                            if(response.body().success == 1) {
                                if(response.body().type.equals("p")) {
                                    apiService.searchForRecentTranscation(response.body().transactionId)
                                            .enqueue(new Callback<TranscationResource>() {
                                                @Override
                                                public void onResponse(Call<TranscationResource> call, Response<TranscationResource> response) {

                                                }

                                                @Override
                                                public void onFailure(Call<TranscationResource> call, Throwable t) {

                                                }
                                            });
                                } else {
                                    apiService.checkRideShareStatus(response.body().transactionId)
                                            .enqueue(new Callback<RideShareResource>() {
                                                @Override
                                                public void onResponse(Call<RideShareResource> call, Response<RideShareResource> response) {

                                                }

                                                @Override
                                                public void onFailure(Call<RideShareResource> call, Throwable t) {

                                                }
                                            });
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverTransactionType> call, Throwable t) {

                    }
                });
    }

    private void initHttpSocket() {
        try {
            mSocket = IO.socket("http://192.168.86.183:3000");
            connectSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectSocket() {
        try {
            mSocket.connect();
            JSONObject object = new JSONObject();
            object.put("identity", "driver");
            object.put("id", Session.getUserId(this));
            object.put("objective", "locationUpdate");
            mSocket.emit("join", object);
            isConnected = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.on(PASSENGER_FOUND, passengerFoundEvent);
        mSocket.on(TRANSCATION_INVITATION, transcationInvitation);
        mSocket.on(PASSENGER_TIMEOUT, passengerTimeout);
        mSocket.on(SHARE_RIDE_DRIVER_FOUND, shareRideHandler);
        mSocket.on(CANCEL_RIDE, cancelRide);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.off(PASSENGER_FOUND, passengerFoundEvent);
        mSocket.off(TRANSCATION_INVITATION, transcationInvitation);
        mSocket.off(PASSENGER_TIMEOUT, passengerTimeout);
        mSocket.off(SHARE_RIDE_DRIVER_FOUND, shareRideHandler);
        mSocket.off(CANCEL_RIDE, cancelRide);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "connected successfully");
            if (!isConnected) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("identity", "driver");
                    object.put("id", Session.getUserId(DriverSocketService.this));
                    object.put("objective", "locationUpdate");
                    mSocket.emit("join", object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            notifyUser("Connected successfully");
            isConnected = true;
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "socket disconnected");
            notifyUser("server disconnected");
            isConnected = false;
        }
    };

    /**
     * Connection error listener
     * Try to reconnect the server
     */
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
            EngineIOException e = (EngineIOException) args[0];
            notifyUser("connection error");
            Log.i(TAG, e.getMessage());
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "socket connection timeout ");
        }
    };

    //Set the on serve mode
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetOnServeEvent(SetServingEvent event) {
        Log.i(TAG, "On Serve: " + event.onServe + "");
        onServe = (event.onServe == 1);
    }

    // Passenger Found Event listener
    // Server send the request to the driver
    private Emitter.Listener passengerFoundEvent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i("Passenger Found", data.toString());
            eventAck("driver:" + Session.getUserId(DriverSocketService.this), PASSENGER_FOUND);
            //Save the transaction data
            PassengerFoundEvent event = new PassengerFoundEvent(data.toString());
            Gson gson = new Gson();
            DriverFoundResponse response = gson.fromJson(data.toString(), DriverFoundResponse.class);
            Session.saveCurrentTransactionID(DriverSocketService.this, response.transcation.id);
            syncAdapter.insertNewTransaction(response.transcation, ((success, message) -> {
                if (success == 1) {
                    Transcation transaction = response.transcation;
                    rideType = "p";
                    notifyUser("Order Found\n" +
                            "From " + transaction.startAddr + " To " + transaction.desAddr);
                    // Fire the event back to the Driver Main Activity
                    EventBus.getDefault().post(event);
                    //Start timer (3 mins)
                    startTimer(180, "p");
                }
            }));
        }
    };

    private void startTimer(int second, String type) {
        if (!isTimerThreadStarted) {
            isTimerThreadStarted = true;
            Thread timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = second;
                        while (count > 0 && isTimerThreadStarted) {
                            Thread.sleep(1000);
                            count--;
                            int minute = (count % 3600) / 60;
                            int second = (count % 60);
                            Log.i("Timer", minute + ":" + second);
                            EventBus.getDefault().post(new TimerEvent(minute, second));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            timerThread.start();
        }
    }

    // Driver response to the request
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDriverResponseEvent(PassengerFoundResponse event) {
        Log.d("Passenger Found", event.getResponse() + "");

        if (isTimerThreadStarted) {
            isTimerThreadStarted = false;
        }

        int response = event.getResponse();
        try {
            JSONObject data = new JSONObject();
            data.put("response", response);
            data.put("driver", event.getDriverID());
            data.put("transcation", event.getTransactionID());
            Log.d("Passenger Found Data", data.toString());
            mSocket.emit("passengerFoundResponse", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Emitter.Listener transcationInvitation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("Transcation completed", "completed");
            JSONObject response = (JSONObject) args[0];
            reportPositionToPassenger = true;
            eventAck("driver:" + Session.getUserId(DriverSocketService.this), TRANSCATION_INVITATION);
            mSocket.emit("joinTranscation", response);
            try {
                int res = response.getInt("response");
                EventBus.getDefault().post(new TranscationCompletedEvent(res));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void startDriverReachTimer(String timeout) {
        Calendar timeoutTime = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ENG"));
        try {
            timeoutTime.setTime(format.parse(timeout));
            timeoutTime.add(Calendar.MINUTE, 5);
            timeoutTime.getTime().getTime();
            Calendar calendar = Calendar.getInstance();
            int timeDifference = (int) (timeoutTime.getTime().getTime() - calendar.getTime().getTime());
            Thread timerThread = new Thread(() -> {
                try {
                    int count = timeDifference / 1000;
                    while (count > 0 && driverReachThreadStart) {
                        Thread.sleep(1000);
                        count--;
                        int minute = (count % 3600) / 60;
                        int second = (count % 60);
                        EventBus.getDefault().post(new TimerEvent(minute, second));
                        String text = "Time Limit: " +
                                String.format(new Locale("ENG"), "%02d", minute) +
                                ":" + String.format(new Locale("ENG"), "%02d", second);
                        notifyUser(text);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            timerThread.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Emitter.Listener shareRideHandler = args -> {
        Log.d("Driver Socket Service", "Share Ride Found");
        try {
            JSONObject response = (JSONObject) args[0];
            Gson gson = new Gson();
            Log.d(TAG, response.toString());
            RideShareTransaction rideShare =
                    gson.fromJson(response.getJSONObject("transcation").toString(), RideShareTransaction.class);

            eventAck("driver:" + Session.getUserId(DriverSocketService.this),
                    SHARE_RIDE_DRIVER_FOUND);
            notifyUser("Share Ride Order : \n" +
                    "1. From " + rideShare.first_transaction.startAddr + " To " + rideShare.first_transaction.desAddr + "\n" +
                    "2. From " + rideShare.second_transaction.startAddr + " To " + rideShare.second_transaction.desAddr);
            EventBus.getDefault().post(new DriverShareRideFoundEvent(rideShare));
            startTimer(180, "s");
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
    };

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onShareRideTranscationEvent(ShareRideDriverResponse response) {
        isTimerThreadStarted = false;
        JSONObject res = new JSONObject();
        try {
            res.put("response", response.response);
            res.put("transcation", response.rideShareId);
            res.put("driver", response.driverId);
            mSocket.emit("shareRideDriverResponse", res);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Emitter.Listener passengerTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = (JSONObject) args[0];
                JSONObject data = jsonObject.getJSONObject("data");
                Gson gson = new Gson();
                Transcation transcation = gson.fromJson(data.toString(), Transcation.class);
                eventAck("driver:" + Session.getUserId(DriverSocketService.this), PASSENGER_TIMEOUT);
                if (transcation.id != null) {

                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    };

    public Emitter.Listener cancelRide = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = (JSONObject) args[0];
                Gson gson = new Gson();
                Transcation transcation = gson.fromJson(jsonObject.getJSONObject("transcation").toString(), Transcation.class);
                if(transcation.id == Session.getCurrentTransactionID(DriverSocketService.this)) {
                    Session.saveCurrentTransactionID(DriverSocketService.this, 0);
                }
            } catch(JSONException e) {

            }
        }
    };

    private void eventAck(String key, String event) {
        if (mSocket != null) {
            try {
                JSONObject object = new JSONObject();
                object.put("event", event);
                object.put("key", key);
                mSocket.emit(EVENT_ACK, object);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    /**
     * Initialize the foreground service (cannot be killed unless manually killed by users)
     */
    private void foregroundServiceStart() {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, DriverTransactionActivity.class);
        notificationIntent.setAction("Action");
        goToActivityIntent = PendingIntent.
                getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8 = new NotificationCompat.Builder(this, DriverNotificationChannel.CHANNEL_ID);
            notificationBuilder_android8.setContentTitle("Messenger")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(goToActivityIntent)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            startForeground(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setContentTitle("Messenger")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(goToActivityIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH);
            startForeground(1, notificationBuilder.build());
        }
    }

    private void createLocationRequest() {
        //Initialize location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL * 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Initialize location service client and callback
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(TAG, "onLocationResult");
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
                    sendPositionToServer(location);
                    sendPositionToPassenger(location);
                }
            }
        };
    }

    private void startLocationUpdate() {
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        } catch (SecurityException e) {
            Log.i(TAG, "Please allow for location update");
            Toast.makeText(getApplicationContext(), "Please allow for location update", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPositionToServer(Location location) {
        Calendar calendar = Calendar.getInstance();
        try {
            JSONObject data = new JSONObject();
            JSONObject object = new JSONObject();
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data.put("timestamp", sqlTimeFormat.format(calendar.getTime()));
            object.put("id", Session.getUserId(this));
            object.put("location", data);
            mSocket.emit("locationUpdate", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendPositionToPassenger(Location location) {
        Calendar calendar = Calendar.getInstance();
        try {
            JSONObject data = new JSONObject();
            JSONObject object = new JSONObject();
            JSONObject pack = new JSONObject();
            // {id: transactionID, pack: {id: driverId, location: lat, long, timestamp}
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data.put("timestamp", sqlTimeFormat.format(calendar.getTime()));
            pack.put("id", Session.getUserId(this));
            pack.put("location", data);
            if(Session.getCurrentTransactionID(this) != 0) {
                object.put("key", "transcation:" + Session.getCurrentTransactionID(this));
            }
            if(Session.getShareRideId(this) != 0) {
                object.put("key", "rideshare:" + Session.getShareRideId(this));
            }
            object.put("pack", pack);
            mSocket.emit(LOCATION_UPDATE_TO_PASSENGER, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifyUser(String message) {
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8.setContentText(message);
            notificationBuilder_android8.setStyle(
                    new NotificationCompat.BigTextStyle().bigText(message));
            nm.notify(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder.setContentText(message);
            notificationBuilder.setVibrate(new long[]{1000, 1000});
            notificationBuilder.setStyle(
                    new Notification.BigTextStyle().bigText(message));
            nm.notify(1, notificationBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        if (isTimerThreadStarted) isTimerThreadStarted = false;   //Stop the timer thread
        if(driverReachThreadStart) driverReachThreadStart = false;
        if (mLocationClient != null)
            mLocationClient.removeLocationUpdates(mLocationCallback);   //Remove Location Update
        if (isConnected) disconnectSocket();     //disconnect the socket connection
        EventBus.getDefault().unregister(this); //unregister Event bus to avoid memory leak
        super.onDestroy();
    }

    public class DriverServiceBinder extends Binder {
        public DriverSocketService getService() {
            return DriverSocketService.this;
        }
    }

    private boolean isGooglePlayServiceAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            Log.i(TAG, "Google Play Service unavailable");
            return false;
        }
    }

    //Callback when Google API client is connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Google API Connected");
    }

    //Callback when Google API client is disconnected
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Google API disconnected");
    }

    //Callback in error
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Google API Connection error");
    }
}
