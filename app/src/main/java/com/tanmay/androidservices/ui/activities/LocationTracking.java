package com.tanmay.androidservices.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tanmay.androidservices.R;
import com.tanmay.androidservices.googleapis.DirectionsAPI;
import com.tanmay.androidservices.googleapis.DirectionsJsonParser;
import com.tanmay.androidservices.interfaces.OnLocationObtainedListener;
import com.tanmay.androidservices.services.LocationService;
import com.tanmay.androidservices.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationTracking extends AppCompatActivity implements OnLocationObtainedListener,
        OnMapReadyCallback {

    public static String TAG = "LocationTracking ==>";

    Context context;

    Toolbar toolbar;
    Button start, stop;
    RelativeLayout entireLayout;

    LocationService locationService;
    GoogleMap mMap;
    Location currentLocation;

    DirectionsAPI directionsAPI;

    boolean isBound = false;
    boolean isViewingRoute = false;
    Snackbar gettingRouteSnack, showingRouteSnack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "On create!");

        setContentView(R.layout.activity_location_tracking);

        initView();
        context = this;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        directionsAPI = new DirectionsAPI();
        Log.d(TAG, "Location Tracking Data: " + LocalStorage.getInstance(context).getTrackingInfo());

        LocationService.listener = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On resume!");

        setButtons();
        startLocationService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "On pause!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "On stop!");

//        unbindService();
    }

    public void initView() {
        Log.d(TAG, "Initialize view!");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        start = (Button) findViewById(R.id.start_tracking);
        stop = (Button) findViewById(R.id.stop_tracking);
        entireLayout = (RelativeLayout) findViewById(R.id.entire_layout);
    }

    public void onStartService(View view) {
        Log.d(TAG, "On start service!");

        Intent mServiceIntent = new Intent(context, LocationService.class);
        startService(mServiceIntent);
    }

    public void onStopService(View view) {
        Log.d(TAG, "On stop service!");

        Intent mServiceIntent = new Intent(context, LocationService.class);
        stopService(mServiceIntent);
    }

    public void onBindService(View view) {
        Log.d(TAG, "On bind service!");

        startLocationService();
    }

    public void onUnbindService(View view) {
        Log.d(TAG, "On unbind service!");

        unbindService();
    }

    public void startTracking(View view) {
        Log.d(TAG, "Start tracking!");

        try {
            JSONObject trackingObj = new JSONObject();
            JSONArray waypoints = new JSONArray();
            JSONObject waypoint = new JSONObject();

            waypoint.put("LAT", currentLocation.getLatitude());
            waypoint.put("LONG", currentLocation.getLongitude());

            waypoints.put(waypoint);

            trackingObj.put("STATUS", true);
            trackingObj.put("WAYPOINTS", waypoints);

            LocalStorage.getInstance(context).setTrackingInfo(trackingObj.toString());

            start.setEnabled(false);
            stop.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopTracking(View view) {
        Log.d(TAG, "Stop tracking!");

        gettingRouteSnack = Snackbar.make(entireLayout, "Getting your route...", Snackbar.LENGTH_INDEFINITE);
        gettingRouteSnack.show();

        try {
            JSONObject lastTrackingObj = new JSONObject(LocalStorage.getInstance(context).getTrackingInfo());
            JSONArray lastWaypoints = lastTrackingObj.optJSONArray("WAYPOINTS");
            Log.d(TAG, lastWaypoints.toString());
            showRouteTaken(lastWaypoints);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject trackingObj = new JSONObject();
            trackingObj.put("STATUS", false);
            LocalStorage.getInstance(context).setTrackingInfo(trackingObj.toString());

            stop.setEnabled(false);
            start.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showRouteTaken(JSONArray waypoints) {
        getRoute(waypoints);
    }

    @Override
    public void onLocationObtained(Location location) {
        Log.d(TAG, "On location obtained!");

        currentLocation = location;
        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "On location changed!");

        currentLocation = location;
        if (!isViewingRoute) setCurrentMarker();
        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
    }

    public void startLocationService() {
        Log.d(TAG, "Start location service!");

        Intent mServiceIntent = new Intent(context, LocationService.class);
        context.bindService(mServiceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "On service connected!");
                isBound = true;
                LocationService.LocalBinder localBinder = (LocationService.LocalBinder) iBinder;
                locationService = localBinder.getService();

                setUpMap();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "On service disconnected!");
                isBound = false;
            }
        }, BIND_AUTO_CREATE);
    }

    public void unbindService() {
        Log.d(TAG, "Unbind service!");

        if (isBound)
            getApplicationContext().unbindService(new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Log.d(TAG, "On service connected!");
                    isBound = true;
                    LocationService.LocalBinder localBinder = (LocationService.LocalBinder) iBinder;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    Log.d(TAG, "On service disconnected!");
                    isBound = false;
                }
            });
    }

    public void setUpMap() {
        Log.d(TAG, "Set up map!");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "On map ready!");
        mMap = googleMap;
        setCurrentMarker();
    }

    public void setCurrentMarker() {
        Log.d(TAG, "Set current marker!");

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Your Location!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        setButtons();
    }

    public void setButtons() {
        Log.d(TAG, "Set buttons");

        String trackingStr = LocalStorage.getInstance(context).getTrackingInfo();
        if (trackingStr == null) {
            startFresh();
        } else {
            try {
                JSONObject trackingObj = new JSONObject(trackingStr);
                boolean trackingStatus = trackingObj.optBoolean("STATUS");

                if (!trackingStatus) {
                    startFresh();
                } else {
                    keepTracking();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void startFresh() {
        Log.d(TAG, "Start fresh!");

        start.setEnabled(true);
        stop.setEnabled(false);
    }

    public void keepTracking() {
        Log.d(TAG, "Keep tracking!");

        start.setEnabled(false);
        stop.setEnabled(true);
    }

    public void getRoute(JSONArray waypoints) {
        Log.d(TAG, "Get Route!");
        int destinationIndex = waypoints.length() - 1;
        Log.d(TAG, "Origin: "
                + new LatLng(waypoints.optJSONObject(0).optDouble("LAT"), waypoints.optJSONObject(0).optDouble("LAT"))
                + ", Destination: "
                + new LatLng(waypoints.optJSONObject(destinationIndex).optDouble("LAT"), waypoints.optJSONObject(destinationIndex).optDouble("LAT")));

        String url = directionsAPI.getDirectionsUrl(waypoints);
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d(TAG, "Exception - Download URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d(TAG, "Background Task: " + e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJsonParser parser = new DirectionsJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(R.color.colorPrimaryDark);
            }
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

            gettingRouteSnack.dismiss();
            showingRouteSnack = Snackbar.make(entireLayout, "This the path you took!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Okay!", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showingRouteSnack.dismiss();
                        }
                    });
            showingRouteSnack.show();
        }
    }
}
