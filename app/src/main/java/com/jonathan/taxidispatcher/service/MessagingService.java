package com.jonathan.taxidispatcher.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIClient;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.ui.passenger_rideshare.PassengerShareRideSocketService;
import com.jonathan.taxidispatcher.utils.Constants;
import com.jonathan.taxidispatcher.utils.PassengerNotificationChannel;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    public static final String TAG = "messaging service";
    NotificationManager nm;
    Notification.Builder notificationBuilder;
    NotificationCompat.Builder notificationBuilder8;

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        APIInterface apiService = APIClient.getAPIService();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo. gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            /*            if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }*/

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            initNotification();
            notifyUser(remoteMessage.getNotification().getBody(), "Taxi GoGo");
            scheduleJob();
            EventBus.getDefault().post(Integer.valueOf(1));
            if (remoteMessage.getData().size() > 0) {
                // Identify the receiver identity and perform action
                String type = "";
                String identity = "";
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    Log.d("data key", key);
                    Log.d("data value", value);
                    switch (key) {
                        case "driver":
                            if(value.equals("transcation")) {
                                updateDriverTransactionStatus();
                            } else {
                                updateDriverTransactionStatus();
                            }
                            break;
                        case "passenger":
                           if(value.equals("transcation")) {
                               updatePassengerTransactionStatus();
                           } else {
                               updatePassengerRideShareTransactionStatus();
                           }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void updateDriverTransactionStatus() {
        Intent intent = new Intent(this, DriverSocketService.class);
        intent.setAction(Constants.ACTION.UPDATE_DRIVER_STATUS);
        startService(intent);
    }

    private void updatePassengerTransactionStatus() {
        Intent intent = new Intent(this, PassengerSocketService.class);
        intent.setAction(Constants.ACTION.UPDATE_PASSENGER_TRANSACTION);
        startService(intent);
    }

    private void updatePassengerRideShareTransactionStatus() {
        Intent intent = new Intent(this, PassengerShareRideSocketService.class);
        intent.setAction(Constants.ACTION.UPDATE_PASSENGER_TRANSACTION);
        startService(intent);
    }

    private void initNotification() {
        PassengerNotificationChannel.createNotificationChannel(this);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT <= 25) { //API <= 25 (Android 7.1 or older)
            notificationBuilder = new Notification.Builder(this)
                    .setContentTitle("Taxi GoGo: You have got a new notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(false);
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        } else {    //API > 25 (Android 8 or higher)
            notificationBuilder8 = new NotificationCompat.Builder(this, PassengerNotificationChannel.CHANNEL_ID)
                    .setContentTitle("Taxi GoGo: You have got a new notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(false);
            notificationBuilder8.setPriority(NotificationManagerCompat.IMPORTANCE_HIGH);
        }
    }

    private void notifyUser(String message, String sender) {
        if(Build.VERSION.SDK_INT <= 25) {
            notificationBuilder.setContentText(sender + ": " + message);
            nm.notify(1, notificationBuilder.build());
        } else {
            notificationBuilder8.setContentText(sender + ": " + message);
            nm.notify(1, notificationBuilder8.build());
        }
    }

    private void timerNotifyUser(String message) {
        notificationBuilder.setContentText(message);
        notificationBuilder.setOnlyAlertOnce(true);
        nm.notify(1, notificationBuilder.build());
    }

    private void scheduleJob() {

    }
}
