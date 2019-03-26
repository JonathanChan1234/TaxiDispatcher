package com.jonathan.taxidispatcher.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class Utils {
    public static String stringListToString(List<String> list) {
        if (list.size() > 0) {
            StringBuilder string = new StringBuilder();
            for (String item : list) {
                string.append(item);
                string.append(", ");
            }
            return string.toString();
        }
        return "";
    }

    /**
     * Check whether the service class is running
     * @param serviceClass service
     * @param context activity
     * @return
     */
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
