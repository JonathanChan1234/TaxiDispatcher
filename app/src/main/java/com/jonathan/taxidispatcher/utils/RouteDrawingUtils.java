package com.jonathan.taxidispatcher.utils;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jonathan.taxidispatcher.data.model.DirectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteDrawingUtils {
    public static PolylineOptions getGoogleMapPolyline(DirectionModel model) {
        List<List<HashMap<String, String>>> route = parse(model);
        ArrayList points;
        PolylineOptions lineOptions = null;

        for (int i = 0; i < route.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = route.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);
            lineOptions.color(Color.RED);
            lineOptions.geodesic(true);
        }

        return lineOptions;
    }

    private static List<List<HashMap<String, String>>> parse(DirectionModel routeData) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        List<DirectionModel.Route> jRoutes = null;
        List<DirectionModel.Leg> jLegs = null;
        List<DirectionModel.Step> jSteps = null;

        jRoutes = routeData.routes;

        /** Traversing all routes */
        for (int i = 0; i < jRoutes.size(); i++) {
            jLegs = (jRoutes.get(i)).legs;
            List path = new ArrayList<HashMap<String, String>>();

            /** Traversing all legs */
            for (int j = 0; j < jLegs.size(); j++) {
                jSteps = jLegs.get(j).steps;

                /** Traversing all steps */
                for (int k = 0; k < jSteps.size(); k++) {
                    String polyline = "";
                    polyline = jSteps.get(k).polyline.points;
                    List list = decodePoly(polyline);

                    /** Traversing all points */
                    for (int l = 0; l < list.size(); l++) {
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                        hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                        path.add(hm);
                    }
                }
                routes.add(path);
            }
        }
        return routes;
    }

    private static List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
