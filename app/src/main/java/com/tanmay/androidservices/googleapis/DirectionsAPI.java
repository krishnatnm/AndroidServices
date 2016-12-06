package com.tanmay.androidservices.googleapis;

import org.json.JSONArray;

/**
 * Created by TaNMay on 3/8/2016.
 */
public class DirectionsAPI {

    public DirectionsAPI() {

    }

    public String getDirectionsUrl(JSONArray waypointArr) {
        // Origin of route
        String str_origin = "origin="
                + waypointArr.optJSONObject(0).optDouble("LAT")
                + ","
                + waypointArr.optJSONObject(0).optDouble("LONG");

        // Destination of route
        int destinationIndex = waypointArr.length() - 1;
        String str_dest = "destination="
                + waypointArr.optJSONObject(destinationIndex).optDouble("LAT")
                + ","
                + waypointArr.optJSONObject(destinationIndex).optDouble("LONG");

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "waypoints=";

        for (int i = 0; i < waypointArr.length(); i++) {
            waypoints += waypointArr.optJSONObject(i).optDouble("LAT")
                    + ","
                    + waypointArr.optJSONObject(i).optDouble("LONG")
                    + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

}
