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
import com.jonathan.taxidispatcher.data.model.DriverFoundResponse;
import com.jonathan.taxidispatcher.data.model.DriverLocation;
import com.jonathan.taxidispatcher.event.DriverFoundEvent;
import com.jonathan.taxidispatcher.event.DriverResponseEvent;
import com.jonathan.taxidispatcher.event.LocationUpdateEvent;
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

import javax.inject.Inject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PassengerSocketService extends Service  {

    private static final String TAG = "Passenger Service";
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

    public PassengerSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            PassengerNotificationChannel.createNotificationChannel(this);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        foregroundServiceStart();
        transcationid = Session.getCurrentTransactionID(this);
        initHttpSocket();
        return START_REDELIVER_INTENT; //START_STICKY: resume the service right (without the intent parameter) after it is killed
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
            object.put("identity", "passenger");
            object.put("id", 9);
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
        mSocket.on(Constants.DRIVER_FOUND, driverFound);
        mSocket.on(Constants.LOCATION_UPDATE, locationUpdateFromDriver);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.off(Constants.LOCATION_UPDATE, locationUpdateFromDriver);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "connected successfully");
            try {
                JSONObject object = new JSONObject();
                object.put("identity", "passenger");
                object.put("id", 9);
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
//            stopForeground(true);
//            stopSelf();
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "socket connection timeout ");
        }
    };

    public Emitter.Listener driverFound = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i("Driver Found", data.toString());

            notifyUser("You have received a request from a driver");

            //Convert the JSON object to POJO
            Gson gson = new Gson();
            DriverFoundResponse response = gson.fromJson(data.toString(), DriverFoundResponse.class);
//            transactionDao.updateTranscationStatus(101, response.driver.id, response.driver.phonenumber, response.driver.username,
//                    response.driver.email, Session.getCurrentTransactionID(getApplicationContext()));
            //Fire the event
            EventBus.getDefault().post(new DriverFoundEvent(response.transcation, response.driver));

            //Start the timer
            startTimer();
        }
    };

    private void startTimer() {
        if (!isTimerThreadStarted) {
            isTimerThreadStarted = true;
            Thread timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = 180;
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

    private Emitter.Listener locationUpdateFromDriver = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject locationData = (JSONObject) args[0];
            Log.i("Location data", locationData.toString());
            Gson gson = new Gson();
            DriverLocation location = gson.fromJson(locationData.toString(), DriverLocation.class);
            EventBus.getDefault().post(new LocationUpdateEvent(location));
        }
    };

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onDriverResponseEvent(DriverResponseEvent event) {
        //stop the timer
        if(isTimerThreadStarted) {
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
            notificationBuilder_android8.setContentTitle("Messenger")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(goToActivityIntent)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            startForeground(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setContentTitle("Messenger")
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
            nm.notify(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder.setContentText(message);
            nm.notify(1, notificationBuilder.build());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

/*    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onStopServiceEvent(StopServiceEvent event) {
        stopForeground(true);
        stopSelf();
    }*/

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        //Stop the timer
        if(isTimerThreadStarted) {
            isTimerThreadStarted = false;
        }

        EventBus.getDefault().unregister(this);
        disconnectSocket();
        super.onDestroy();
    }
}
