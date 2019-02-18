package com.jonathan.taxidispatcher.api;

import android.support.annotation.Nullable;

import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;
import com.jonathan.taxidispatcher.data.model.StandardResponse;
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
    Call<TranscationResource> searchForRecentTranscation(@Field("userid")Integer userid);


    @FormUrlEncoded
    @POST("user/login")
    Call<AccountUserResponse> passengerSignIn(
            @Field("phonenumber") String phonenumber,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("user/register")
    Call<AccountUserResponse> passengerCreateAccount(
            @Field("username") String username,
            @Field("password") String password,
            @Field("phonenumber") String phonenumber,
            @Field("email") String email,
            @Field("img") String profileImg
    );

    @FormUrlEncoded
    @POST("driver/login")
    Call<AccountDriverResponse> driverSignIn(
            @Field("phonenumber") String phonenumber,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("driver/register")
    Call<AccountDriverResponse> driverCreateAccount(
            @Field("username") String username,
            @Field("password") String password,
            @Field("phonenumber") String phonenumber,
            @Field("email") String email,
            @Field("img") String profileImg
    );

    @FormUrlEncoded
    @POST("driver/setOccupied")
    Call<StandardResponse> setOccupied(
            @Field("id") Integer id,
            @Field("occupied") Integer occupied
    );

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
    Call<StandardResponse> signInTaxi(
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
}
