package com.jonathan.taxidispatcher.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionModel {
    @SerializedName("geocoded_waypoints")
    @Expose
    public List<GeocodedWaypoint> geocodedWaypoints = null;
    @SerializedName("routes")
    @Expose
    public List<Route> routes = null;
    @SerializedName("status")
    @Expose
    public String status;

    public class Bounds {

        @SerializedName("northeast")
        @Expose
        public Northeast northeast;
        @SerializedName("southwest")
        @Expose
        public Southwest southwest;

    }

    public class Distance {

        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("value")
        @Expose
        public Integer value;

    }

    public class Distance_ {

        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("value")
        @Expose
        public Integer value;

    }


    public class Duration {

        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("value")
        @Expose
        public Integer value;

    }

    public class Duration_ {

        @SerializedName("text")
        @Expose
        public String text;
        @SerializedName("value")
        @Expose
        public Integer value;

    }


    public class EndLocation {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }


    public class EndLocation_ {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }

    public class GeocodedWaypoint {

        @SerializedName("geocoder_status")
        @Expose
        public String geocoderStatus;
        @SerializedName("place_id")
        @Expose
        public String placeId;
        @SerializedName("types")
        @Expose
        public List<String> types = null;

    }

    public class Leg {

        @SerializedName("distance")
        @Expose
        public Distance distance;
        @SerializedName("duration")
        @Expose
        public Duration duration;
        @SerializedName("end_address")
        @Expose
        public String endAddress;
        @SerializedName("end_location")
        @Expose
        public EndLocation endLocation;
        @SerializedName("start_address")
        @Expose
        public String startAddress;
        @SerializedName("start_location")
        @Expose
        public StartLocation startLocation;
        @SerializedName("steps")
        @Expose
        public List<Step> steps = null;
        @SerializedName("traffic_speed_entry")
        @Expose
        public List<Object> trafficSpeedEntry = null;
        @SerializedName("via_waypoint")
        @Expose
        public List<Object> viaWaypoint = null;

    }

    public class Northeast {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }


    public class OverviewPolyline {

        @SerializedName("points")
        @Expose
        public String points;

    }

    public class Polyline {

        @SerializedName("points")
        @Expose
        public String points;

    }

    public class Route {

        @SerializedName("bounds")
        @Expose
        public Bounds bounds;
        @SerializedName("copyrights")
        @Expose
        public String copyrights;
        @SerializedName("legs")
        @Expose
        public List<Leg> legs = null;
        @SerializedName("overview_polyline")
        @Expose
        public OverviewPolyline overviewPolyline;
        @SerializedName("summary")
        @Expose
        public String summary;
        @SerializedName("warnings")
        @Expose
        public List<Object> warnings = null;
        @SerializedName("waypoint_order")
        @Expose
        public List<Object> waypointOrder = null;

    }

    public class Southwest {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }

    public class StartLocation {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }

    public class StartLocation_ {

        @SerializedName("lat")
        @Expose
        public Double lat;
        @SerializedName("lng")
        @Expose
        public Double lng;

    }

    public class Step {

        @SerializedName("distance")
        @Expose
        public Distance_ distance;
        @SerializedName("duration")
        @Expose
        public Duration_ duration;
        @SerializedName("end_location")
        @Expose
        public EndLocation_ endLocation;
        @SerializedName("html_instructions")
        @Expose
        public String htmlInstructions;
        @SerializedName("polyline")
        @Expose
        public Polyline polyline;
        @SerializedName("start_location")
        @Expose
        public StartLocation_ startLocation;
        @SerializedName("travel_mode")
        @Expose
        public String travelMode;
        @SerializedName("maneuver")
        @Expose
        public String maneuver;

    }
}
