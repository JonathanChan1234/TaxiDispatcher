package com.jonathan.taxidispatcher.api;

import android.support.annotation.Nullable;

import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;
import com.jonathan.taxidispatcher.data.model.DriverTransactionType;
import com.jonathan.taxidispatcher.data.model.RideSharePairingResponse;
import com.jonathan.taxidispatcher.data.model.RideShareResource;
import com.jonathan.taxidispatcher.data.model.RideShareTransactionResource;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
import com.jonathan.taxidispatcher.data.model.TaxiSignInResponse;
import com.jonathan.taxidispatcher.data.model.Taxis;
import com.jonathan.taxidispatcher.data.model.Transcation;
import com.jonathan.taxidispatcher.data.model.TranscationResource;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {
    // Personal Ride Transaction
    @FormUrlEncoded
    @POST("transcation/startTranscation")
    Call<TranscationResource> startTranscation(@Field("userid")Integer userid,
                                               @Field("start_lat") Double start_lat,
                                               @Field("start_long") Double start_long,
                                               @Field("start_addr")  String start_addr,
                                               @Field("des_lat") Double des_lat,
                                               @Field("des_long") Double des_long,
                                               @Field("des_addr") String des_arr,
                                               @Field("meet_up_time")@Nullable String meet_up_time,
                                               @Field("requirement") @Nullable String requirement);

    @FormUrlEncoded
    @POST("transcation/startTranscationDemo")
    Call<TranscationResource> startDemoTranscation(@Field("userid")Integer userid,
                                           @Field("start_lat") Double start_lat,
                                           @Field("start_long") Double start_long,
                                           @Field("start_addr")  String start_addr,
                                           @Field("des_lat") Double des_lat,
                                           @Field("des_long") Double des_long,
                                           @Field("des_addr") String des_arr,
                                           @Field("meet_up_time")@Nullable String meet_up_time,
                                           @Field("requirement") @Nullable String requirement);

    @FormUrlEncoded
    @POST("transcation/searchForRecentTranscation")
    Call<TranscationResource> searchForRecentTranscation(@Field("id")Integer userid);

    @FormUrlEncoded
    @POST("transcation/cancelOrder")
    Call<StandardResponse> cancelOrder(@Field("id") Integer transcationId);

    @FormUrlEncoded
    @POST("transcation/driverReachPickupPoint")
    Call<StandardResponse> driverReachPickupPoint(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("transcation/driverExitOrder")
    Call<StandardResponse> driverExitOrder(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("transcation/driverCancelOrder")
    Call<StandardResponse> driverCancelOrder(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("transcation/passengerConfirmRide")
    Call<StandardResponse> passengerConfirmRide(@Field("id") Integer transactionId);

    @FormUrlEncoded
    @POST("transcation/finishRide")
    Call<StandardResponse> driverFinishRide(@Field("id") Integer transactionId);

    // Share Ride
    @FormUrlEncoded
    @POST("rideShare/makeShareRide")
    Call<RideShareResource> startRideShare(@Field("userid")Integer userid,
                                           @Field("start_lat") Double start_lat,
                                           @Field("start_long") Double start_long,
                                           @Field("start_addr")  String start_addr,
                                           @Field("des_lat") Double des_lat,
                                           @Field("des_long") Double des_long,
                                           @Field("des_addr") String des_arr);
    @FormUrlEncoded
    @POST("rideShare/checkStatus")
    Call<RideShareResource> checkRideShareStatus(@Field("id") Integer transactionID);

    @FormUrlEncoded
    @POST("rideShare/checkTransactionStatus")
    Call<RideShareTransactionResource> checkRideShareTransactionStatus(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("rideShare/getShareRidePairing")
    Call<RideSharePairingResponse> getShareRidePairing(@Field("id") Integer id);

    @FormUrlEncoded
    @POST("rideShare/cancelOrder")
    Call<StandardResponse> cancelShareRideOrder(@Field("id") Integer transactionID);

    // Passenger Account
    @FormUrlEncoded
    @POST("user/login")
    Call<AccountUserResponse> passengerSignIn(
            @Field("phonenumber") String phonenumber,
            @Field("password") String password,
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("user/register")
    Call<AccountUserResponse> passengerCreateAccount(
            @Field("username") String username,
            @Field("password") String password,
            @Field("phonenumber") String phonenumber,
            @Field("email") String email,
            @Field("img") String profileImg,
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("user/logout")
    Call<StandardResponse> passengerLogout(
            @Field("id") Integer id
    );

    // Driver Account
    @FormUrlEncoded
    @POST("driver/login")
    Call<AccountDriverResponse> driverSignIn(
            @Field("phonenumber") String phonenumber,
            @Field("password") String password,
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("driver/register")
    Call<AccountDriverResponse> driverCreateAccount(
            @Field("username") String username,
            @Field("password") String password,
            @Field("phonenumber") String phonenumber,
            @Field("email") String email,
            @Field("img") String profileImg,
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("user/logout")
    Call<StandardResponse> driverLogout(
            @Field("id") Integer id
    );

    @FormUrlEncoded
    @POST("driver/setOccupied")
    Call<StandardResponse> setOccupied(
            @Field("id") Integer id,
            @Field("occupied") Integer occupied
    );

    @FormUrlEncoded
    @POST("driver/findCurrentTransaction")
    Call<DriverTransactionType> checkDriverTransactionStatus(@Field("id") Integer id);

    //Taxi Account
    @FormUrlEncoded
    @POST("taxi/checkDuplicate")
    Call<StandardResponse> checkDuplicate(
            @Field("platenumber") String platenumber
    );

    @FormUrlEncoded
    @POST("taxi/register")
    Call<StandardResponse> registerNewTaxi(
            @Field("platenumber") String platenumber,
            @Field("password") String password,
            @Field("id") Integer id
    );

    @FormUrlEncoded
    @POST("taxi/signIn")
    Call<TaxiSignInResponse> signInTaxi(
            @Field("platenumber") String platenumber,
            @Field("accessToken") String accessToken,
            @Field("id") Integer id
    );

    @FormUrlEncoded
    @POST("taxi/checkOwnerTaxi")
    Call<Taxis> getTaxiList(
            @Field("id") Integer id
    );

    @FormUrlEncoded
    @POST("taxi/deleteAccount")
    Call<StandardResponse> deleteTaxiAccount(
            @Field("platenumber") String platenumber,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("taxi/logout")
    Call<StandardResponse> signoutTaxi(
            @Field("taxiID") Integer taxiID,
            @Field("driverID") Integer driverID
    );
}
