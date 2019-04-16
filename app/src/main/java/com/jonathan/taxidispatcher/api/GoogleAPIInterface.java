package com.jonathan.taxidispatcher.api;

import com.jonathan.taxidispatcher.data.model.DirectionModel;
import com.jonathan.taxidispatcher.data.model.PlaceResource;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleAPIInterface {
    @GET("place/nearbysearch/json")
    Call<PlaceResource> getNearbyPlace(@Query("language")String language,
                                       @Query("location") String location,
                                       @Query("radius") String radius,
                                       @Query("key") String key);

    @GET("directions/json")
    Call<DirectionModel> getRoute(
            @Query("origin")String origin,
            @Query("destination") String destination,
            @Query("key") String key
    );

    @GET("directions/json")
    Call<DirectionModel> getRouteWithWaypoints(
            @Query("origin")String origin,
            @Query("destination") String destination,
            @Query("waypoints") String waypoints,
            @Query("key") String key
    );
}
