package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceResource {
    @SerializedName("html_attributions")
    @Expose
    public List<String> htmlAttributions = null;
    @SerializedName("next_page_token")
    @Expose
    public String nextPageToken;
    @SerializedName("results")
    @Expose
    public List<Result> results = null;
    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("location")
    @Expose
    public Location location;
    @SerializedName("viewport")
    @Expose
    public Viewport viewport;

    public class Geometry {
        @SerializedName("location")
        @Expose
        public Location location;
        @SerializedName("viewport")
        @Expose
        public Viewport viewport;
    }

    public class Location {
        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;
    }

    public class Viewport {
        @SerializedName("northeast")
        @Expose
        public Northeast northeast;
        @SerializedName("southwest")
        @Expose
        public Southwest southwest;
    }

    public class Result {
        @SerializedName("geometry")
        @Expose
        public Geometry geometry;
        @SerializedName("icon")
        @Expose
        public String icon;
        @SerializedName("id")
        @Expose
        public String id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("place_id")
        @Expose
        public String placeId;
        @SerializedName("reference")
        @Expose
        public String reference;
        @SerializedName("scope")
        @Expose
        public String scope;
        @SerializedName("types")
        @Expose
        public List<String> types = null;
    }

    public class Northeast {
        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;
    }

    public class Southwest {
        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;
    }
}
