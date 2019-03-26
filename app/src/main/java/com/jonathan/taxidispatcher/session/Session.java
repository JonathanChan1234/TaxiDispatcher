package com.jonathan.taxidispatcher.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.jonathan.taxidispatcher.data.model.Transcation;

public class Session {
    public static final String LOGGED = "Logged";
    public static final String USERNAME = "username";
    public static final String ACCESS_CODE = "access_code";
    public static final String IDENTITY = "Identity";
    public static final String PHONE_NUMBER = "phone number";
    public static final String TRANSACTION = "TRANSACTION";
    public static final String TRANSACTION_ID = "TRANSACTION_ID";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String TAXI = "TAXI";
    public static final String TAXI_NAME = "taxi name";
    public static final String SHARE_RIDE_ID = "shareRideId";


    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("Preference", Context.MODE_PRIVATE);
    }

    public static void logIn(Context context,
                             Integer id,
                             String phonenumber,
                             String username,
                             String email,
                             String identity,
                             String accessCode) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(ID, id);
        editor.putBoolean(LOGGED, true);
        editor.putString(USERNAME, username);
        editor.putString(PHONE_NUMBER, phonenumber);
        editor.putString(ACCESS_CODE, accessCode);
        editor.putString(EMAIL, email);
        editor.putString(IDENTITY, identity);
        editor.apply();
    }

    public static int getUserId(Context context) {
        return getPreferences(context).getInt(ID, 0);
    }

    public static boolean checkLogInState(Context context) {
        return getPreferences(context).getBoolean(LOGGED, false);
    }

    public static String checkIdentity(Context context) {
        return getPreferences(context).getString(IDENTITY, "");
    }

    public static String getUsername(Context context) {
        return getPreferences(context).getString(USERNAME, "");
    }

    public static String getPhoneNumber(Context context) {
        return getPreferences(context).getString(PHONE_NUMBER, "");
    }

    public static String getEmail(Context context) {
        return getPreferences(context).getString(EMAIL, "");
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED, false);
        editor.putString(ACCESS_CODE, "");
        editor.apply();
    }

    public static void saveCurrentShareRideId(Context context, int id) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(SHARE_RIDE_ID, id);
        editor.apply();
    }

    public static int getShareRideId(Context context) {
        return getPreferences(context).getInt(SHARE_RIDE_ID, 0);
    }

    public static void saveCurrentTransaction(Context context, int id, Transcation transcation) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(TRANSACTION_ID, id);
        Gson gson = new Gson();
        String serializedData = gson.toJson(transcation);
        editor.putString(TRANSACTION, serializedData);
        editor.apply();
    }

    public static void saveCurrentTransactionID(Context context, Integer id) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(TRANSACTION_ID, id);
        editor.apply();
    }

    public static int getCurrentTransactionID(Context context) {
        return getPreferences(context).getInt(TRANSACTION_ID, 0);
    }

    public static Transcation getCurrentTranscation(Context context) {
        String serializedData = getPreferences(context).getString(TRANSACTION, "");
        if(!serializedData.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(serializedData, Transcation.class);
        }
        return null;
    }

    public static void saveTaxiPlateNumber(Context context, String plateNumber) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(TAXI_NAME, plateNumber);
        editor.apply();
    }

    public static String getTaxiPlateNumber(Context context) {
        return getPreferences(context).getString(TAXI_NAME, "");
    }

    public static void saveTaxiId(Context context, int id) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(TAXI, id);
        editor.apply();
    }

    public static int getTaxiId(Context context) {
        return getPreferences(context).getInt(TAXI, 0);
    }
}
