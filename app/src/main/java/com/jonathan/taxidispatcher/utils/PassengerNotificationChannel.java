package com.jonathan.taxidispatcher.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

public class PassengerNotificationChannel {
    public static final String CHANNEL_ID = "message";

    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "message notification";
            String description = "notify user for incoming message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.GRAY);
            channel.setVibrationPattern(new long[]{1000, 1000});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(nm != null) nm.createNotificationChannel(channel);
        }
    }
}
