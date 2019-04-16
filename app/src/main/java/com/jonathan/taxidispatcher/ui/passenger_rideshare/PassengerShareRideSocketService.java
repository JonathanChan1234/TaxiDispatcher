package com.jonathan.taxidispatcher.ui.passenger_rideshare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.DriverLocation;
import com.jonathan.taxidispatcher.data.model.RideShare;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;
import com.jonathan.taxidispatcher.event.DriverReachTimerEvent;
import com.jonathan.taxidispatcher.event.Location;
import com.jonathan.taxidispatcher.event.PassengerShareRideFound;
import com.jonathan.taxidispatcher.event.TimerEvent;
import com.jonathan.taxidispatcher.session.Session;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;

import dagger.android.AndroidInjection;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import timber.log.Timber;

public class PassengerShareRideSocketService extends Service {

    public static final String TAG = "Share Ride Service";
    public static final String LOCATION_UPDATE_TO_PASSENGER = "locationUpdateToPassenger";
    public static final String JOIN_SHARE_RIDE_ROOM = "joinShareRideRoom";
    public static final String GET_DRIVER_LOCATION = "getDriverLocation";
    public static final String STOP_GET_DRIVER_LOCATION = "stopGetDriverLocation";
    public static final String SHARE_RIDE_DRIVER_REACH = "shareRideDriverReach";
    // Notification Manager
    NotificationManager nm;
    NotificationCompat.Builder notificationBuilder_android8;
    Notification.Builder notificationBuilder;

    // Socket Event
    public static final String SHARE_RIDE_PAIRING_SUCCESS = "shareRidePairingSuccess";
    public static final String EVENT_ACK = "eventAck";

    private Socket mSocket;
    private boolean isConnected;
    Handler handler;
    Runnable updateLocation;
    private boolean driverReachTimerThreadStarted = false;

    public PassengerShareRideSocketService() {
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
        Log.d("PassengerSocketService", "started");
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Constants.ACTION.START_FOREGROUND_SERVICE:
                    initHttpSocket();
                    break;
                case Constants.ACTION.STOP_FOREGROUND_SERVICE:
                    stopForeground(true);
                    stopSelf();
                    break;
                case GET_DRIVER_LOCATION:
                    getDriverPosition(intent.getIntExtra("driverId", 0));
                    break;
                case STOP_GET_DRIVER_LOCATION:
                    if (handler != null && updateLocation != null)
                        handler.removeCallbacks(updateLocation);
                    break;
                default:
                    break;
            }
        }
        return START_STICKY; //START_STICKY: resume the service right (without the intent parameter) after it is killed
    }

    private void getDriverPosition(int id) {
        if (handler == null) handler = new Handler(Looper.getMainLooper());
        if (updateLocation != null) handler.removeCallbacks(updateLocation);
        updateLocation = new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    try {
                        Log.d(TAG, "get location");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("driver", id);
                        Object[] object = new Object[1];
                        object[0] = jsonObject;
                        mSocket.emit("getDriverLocation", object, new Ack() {
                            @Override
                            public void call(Object... location) {
                                try {
                                    JSONObject data = (JSONObject) location[0];
                                    Log.d(TAG, data.toString());
                                    Location position = new Gson().fromJson(data.toString(), Location.class);
                                    EventBus.getDefault().post(position);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        handler.postDelayed(this, 30000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        handler.post(updateLocation);
    }

    private void initHttpSocket() {
        try {
            IO.Options opts = new IO.Options();
            opts.timeout = -1;
            opts.reconnection = true;
            mSocket = IO.socket("http://" + Session.getIP(this) + ":3000", opts);
            connectSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void connectSocket() {
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.on(SHARE_RIDE_PAIRING_SUCCESS, shareRideFound);
        mSocket.on(SHARE_RIDE_DRIVER_REACH, driverReach);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
        mSocket.off(SHARE_RIDE_PAIRING_SUCCESS, shareRideFound);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTimerEvent(TimerEvent timerEvent) {

    }

    /**
     * Passenger Connect to the server
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "connected successfully");
            try {
                JSONObject object = new JSONObject();
                object.put("identity", "passenger");
                object.put("id", Session.getUserId(PassengerShareRideSocketService.this));
                object.put("objective", "transcation");
                if (Session.getCurrentTransactionID(PassengerShareRideSocketService.this) != 0) {
                    object.put("transcationid",
                            Session.getCurrentTransactionID(PassengerShareRideSocketService.this));
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
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8 = new NotificationCompat.Builder(this, PassengerNotificationChannel.CHANNEL_ID);
            notificationBuilder_android8.setContentTitle("Taxi GoGo is Running at Background")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            startForeground(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder = new Notification.Builder(this);
            notificationBuilder.setContentTitle("Taxi GoGo is Running at Background")
                    .setSmallIcon(R.drawable.ic_taxi_icon)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);
            startForeground(1, notificationBuilder.build());
        }
    }

    /**
     * Notification to the user
     *
     * @param message message text
     */
    private void notifyUser(String message) {
        if (Build.VERSION.SDK_INT > 26) {
            notificationBuilder_android8.setContentText(message);
            notificationBuilder_android8.setOnlyAlertOnce(false);
            notificationBuilder_android8.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(message));
            nm.notify(1, notificationBuilder_android8.build());
        } else {
            notificationBuilder.setContentText(message);
            notificationBuilder.setOnlyAlertOnce(false);
            notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(message));
            nm.notify(1, notificationBuilder.build());
        }
    }

    private Emitter.Listener shareRideFound = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject transactionData = (JSONObject) args[0];
                Log.d("Share Ride Found", transactionData.toString());
                Gson gson = new Gson();
                RideShareTransaction transaction = gson.fromJson(transactionData.getJSONObject("transcation").toString(),
                        RideShareTransaction.class);
                eventAck("passenger:" + Session.getUserId(PassengerShareRideSocketService.this),
                        SHARE_RIDE_PAIRING_SUCCESS);
                notifyUser("Share Ride Pairing Success");
                // Update UI
                EventBus.getDefault().post(new PassengerShareRideFound(transaction));

                JSONObject responseData = new JSONObject();
                responseData.put("transcation", transaction.id);
                mSocket.emit(JOIN_SHARE_RIDE_ROOM, responseData);
                getDriverPosition(transaction.driver.id);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    };

    private Emitter.Listener driverReach = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                Gson gson = new Gson();
                RideShareTransaction transaction = gson.fromJson(data.getJSONObject("transcation").toString(),
                        RideShareTransaction.class);
                eventAck("passenger:" + Session.getUserId(PassengerShareRideSocketService.this),
                        SHARE_RIDE_DRIVER_REACH);
                String time = data.getString("time");
                startTimer(time);
            } catch (JSONException e) {

            }
        }
    };

    private void startTimer(String time) {
        Calendar timeoutTime = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ENG"));
        try {
            timeoutTime.setTime(format.parse(time));
            timeoutTime.add(Calendar.MINUTE, 5);
            timeoutTime.getTime().getTime();
            Calendar calendar = Calendar.getInstance();
            int timeDifference = (int) (timeoutTime.getTime().getTime() - calendar.getTime().getTime());
            if (timeDifference > 0) notifyUser("Your driver has reached the pick-up point");
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
                        notifyUser("Your driver have already reached the pick-up point \n" +
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectSocket();
        EventBus.getDefault().unregister(this);
        if (updateLocation != null && handler != null) handler.removeCallbacks(updateLocation);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
