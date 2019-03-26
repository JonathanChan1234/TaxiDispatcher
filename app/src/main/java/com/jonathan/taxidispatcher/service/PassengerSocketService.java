package com.jonathan.taxidispatcher.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.DriverFoundResponse;
import com.jonathan.taxidispatcher.data.model.DriverLocation;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.event.DriverFoundEvent;
import com.jonathan.taxidispatcher.event.DriverResponseEvent;
import com.jonathan.taxidispatcher.event.LocationUpdateEvent;
import com.jonathan.taxidispatcher.event.PassengerDriverReachEvent;
import com.jonathan.taxidispatcher.event.PassengerShareRideFound;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.room.TaxiDb;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_transaction.PassengerTransactionActivity;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.PassengerNotificationChannel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import timber.log.Timber;

public class PassengerSocketService extends Service {

    private static final String TAG = "Passenger Service";

    // Socket Event
    public static final String SHARE_RIDE_PAIRING_SUCCESS = "shareRidePairingSuccess";
    public static final String LOCATION_UPDATE = "locationUpdate";
    public static final String DRIVER_FOUND = "passengerDriverFound";
    public static final String DRIVER_REACH = "passengerDriverReach";
    public static final String EVENT_ACK = "eventAck";

    private Socket mSocket;
    private boolean isConnected;

    PendingIntent goToActivityIntent;

    NotificationManager nm;
    NotificationCompat.Builder notificationBuilder_android8;
    Notification.Builder notificationBuilder;
    int transcationid = 0;

    @Inject
    TransactionDao transactionDao;

    boolean isTimerThreadStarted = false;
    private boolean driverReachTimerThreadStarted = false;

    public PassengerSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            PassengerNotificationChannel.createNotificationChannel(this);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        foregroundServiceStart();
        transcationid = Session.getCurrentTransactionID(this);
        Log.d("PassengerSocketService", "started");
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.START_FOREGROUND_SERVICE)) {
                initHttpSocket();
            } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_SERVICE)) {
                stopForeground(true);
                stopSelf();
            }
        }
        return START_REDELIVER_INTENT; //START_STICKY: resume the service right (without the intent parameter) after it is killed
    }

    private void initHttpSocket() {
        try {
            IO.Options opts = new IO.Options();
            opts.timeout = -1;
            opts.reconnection = true;
            mSocket = IO.socket("http://192.168.86.183:3000", opts);
            connectSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectSocket() {
        try {
            mSocket.connect();
            JSONObject object = new JSONObject();
            object.put("identity", "passenger");
            object.put("id", Session.getUserId(this));
            object.put("objective", "transcation");
            if (transcationid != 0) {
                object.put("transcationid", transcationid);
            }
            mSocket.emit("join", object);
            isConnected = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.on(DRIVER_FOUND, driverFound);
        mSocket.on(LOCATION_UPDATE, locationUpdateFromDriver);
        mSocket.on(DRIVER_REACH, driverReachHandler);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.off(DRIVER_FOUND, driverFound);
        mSocket.off(LOCATION_UPDATE, locationUpdateFromDriver);
        mSocket.on(DRIVER_REACH, driverReachHandler);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "connected successfully");
            try {
                JSONObject object = new JSONObject();
                object.put("identity", "passenger");
                object.put("id", Session.getUserId(PassengerSocketService.this));
                object.put("objective", "transcation");
                if (transcationid != 0) {
                    object.put("transcationid", transcationid);
                }
                mSocket.emit("join", object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!isConnected) {
                initHttpSocket();
            }
            isConnected = true;
            notifyUser("Connected to the server");
        }
    };

    public Emitter.Listener driverFound = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i("Driver Found", data.toString());

            notifyUser("You have received a request from a driver");
            eventAck("transcation:" + Session.getCurrentTransactionID(PassengerSocketService.this),
                    DRIVER_FOUND);
            //Convert the JSON object to POJO
            Gson gson = new Gson();
            DriverFoundResponse response = gson.fromJson(data.toString(), DriverFoundResponse.class);
//            transactionDao.updateTransactionStatus(101, response.driver.id, response.driver.phonenumber, response.driver.username,
//                    response.driver.email, Session.getCurrentTransactionID(getApplicationContext()));
            //Fire the event
            EventBus.getDefault().postSticky(new DriverFoundEvent(response.transcation, response.driver));

            //Start the timer
            startTimer(180);
        }
    };

    private void startTimer(int count) {
        if (!isTimerThreadStarted) {
            isTimerThreadStarted = true;
            Thread timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int counter = count;
                        while (counter > 0 && isTimerThreadStarted) {
                            Thread.sleep(1000);
                            counter--;
                            int minute = (counter % 3600) / 60;
                            int second = (counter % 60);
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

    public Emitter.Listener driverReachHandler = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                String time = data.getString("time");
                JSONObject transcationData = data.getJSONObject("transcation");
                Gson gson = new Gson();
                Transcation transcation = gson.fromJson(transcationData.toString(), Transcation.class);
                eventAck("transcation:" + transcation.id, DRIVER_REACH);
                // Update the UI
                EventBus.getDefault().post(new PassengerDriverReachEvent(time));
                notifyUser("Your driver have already reached the pick-up point");
                startNotificationTimer(time);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    };

    private void startNotificationTimer(String time) {
        Calendar timeoutTime = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ENG"));
        try {
            timeoutTime.setTime(format.parse(time));
            timeoutTime.add(Calendar.MINUTE, 15);
            timeoutTime.getTime().getTime();
            Calendar calendar = Calendar.getInstance();
            int timeDifference = (int) (timeoutTime.getTime().getTime() - calendar.getTime().getTime());
            driverReachTimerThreadStarted = true;
            Thread timerThread = new Thread(() -> {
                try {
                    int count = timeDifference / 1000;
                    while (count > 0 && driverReachTimerThreadStarted) {
                        Thread.sleep(1000);
                        count--;
                        int minute = (count % 3600) / 60;
                        int second = (count % 60);
                        //Update the notification bar
                        notifyUserTimer("Your driver have already reached the pick-up point \n" +
                                "Timeout Time: " + String.format("%02d", minute) +
                                ":" + String.format("%02d", second));
                        // Update the UI
                        EventBus.getDefault().post(new TimerEvent(minute, second));
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

    private Emitter.Listener locationUpdateFromDriver = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject locationData = (JSONObject) args[0];
            Log.i("Location data", locationData.toString());
            Gson gson = new Gson();
            DriverLocation location = gson.fromJson(locationData.toString(), DriverLocation.class);
            EventBus.getDefault().post(location);
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDriverResponseEvent(DriverResponseEvent event) {
        //stop the timer
        if (isTimerThreadStarted) {
            isTimerThreadStarted = false;
        }
        //Cancel the timer after user input
        Log.i("onDriverResponseEvent", event.getResponse() + "");
        JSONObject response = new JSONObject();
        try {
            // Accept order
            if (event.getResponse() == 1) {
                response.put("response", 1);
            } else { // Reject order
                response.put("response", 0);
            }
            response.put("transcation", event.getTranscation().id);
            response.put("driver", event.getDriver().id);
            mSocket.emit("driverFoundResponse", response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "socket disconnected");
            notifyUser("server disconnected");
            isConnected = false;
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            isConnected = false;
            notifyUser("connection error");
            Log.i(TAG, "socket connection error ");
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "socket connection timeout ");
        }
    };

    /*
    Initialize the foreground service (cannot be killed unless manually killed by users)
     */
    private void foregroundServiceStart() {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, PassengerTransactionActivity.class);
        notificationIntent.setAction("Action");
        goToActivityIntent = PendingIntent.
                getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8 = new NotificationCompat.Builder(this, PassengerNotificationChannel.CHANNEL_ID);
            notificationBuilder_android8.setContentTitle("Taxi GoGo is Running at Background")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(goToActivityIntent)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            startForeground(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setContentTitle("Taxi GoGo is Running at Background")
                    .setSmallIcon(R.drawable.ic_taxi_icon)
                    .setContentIntent(goToActivityIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);
            startForeground(1, notificationBuilder.build());
        }
    }

    private void notifyUser(String message) {
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8.setContentText(message);
            notificationBuilder_android8.setOnlyAlertOnce(false);
            nm.notify(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder.setContentText(message);
            notificationBuilder.setOnlyAlertOnce(false);
            nm.notify(1, notificationBuilder.build());
        }
    }

    private void notifyUserTimer(String message) {
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8.setContentText(message);
            notificationBuilder_android8.setOnlyAlertOnce(true);
            notificationBuilder_android8.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(message));
            nm.notify(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder.setContentText(message);
            notificationBuilder.setOnlyAlertOnce(true);
            notificationBuilder.setStyle((new Notification.BigTextStyle()
                    .bigText(message)));
            nm.notify(1, notificationBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        //Stop the timer
        if (isTimerThreadStarted) {
            isTimerThreadStarted = false;
        }

        if (driverReachTimerThreadStarted) {
            driverReachTimerThreadStarted = false;
        }

        EventBus.getDefault().unregister(this);
        if (isConnected) disconnectSocket();
        super.onDestroy();
    }
}
