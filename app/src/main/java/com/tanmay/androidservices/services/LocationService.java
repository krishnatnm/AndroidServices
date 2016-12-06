package com.tanmay.androidservices.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tanmay.androidservices.interfaces.OnLocationObtainedListener;
import com.tanmay.androidservices.utils.LocalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final long UPDATE_INTERVAL = 10000;
    public static final long FASTEST_UPDATE_INTERVAL = 5000;

    public static String TAG = "LocationService =>=> ";

    public static OnLocationObtainedListener listener;

    public IBinder binder = new LocalBinder();

    Context context;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    Location mCurrentLocation;

    public LocationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println(TAG + "On bind!");

        if (mGoogleApiClient.isConnected())
            startLocationUpdates();
        else
            mGoogleApiClient.connect();

        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println(TAG + "On create!");

        context = this;
        buildGoogleApiClient();
    }

    public synchronized void buildGoogleApiClient() {
        System.out.println(TAG + "Building Google API client!");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        System.out.println(TAG + "Create location request!");

//        mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        System.out.println(TAG + "On start command!");

        mGoogleApiClient.connect();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println(TAG + "On destroy!");
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println(TAG + "On connected!");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println(TAG + "On connection suspended!");

        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println(TAG + "On location changed!");

        if (isTracking()) {
            System.out.println(TAG + "TRACKING!!!");
            updateTracker(location.getLatitude(), location.getLongitude());
        }

        mCurrentLocation = location;
        listener.onLocationChanged(mCurrentLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println(TAG + "On connection failed!");
    }

    public boolean isTracking() {
        System.out.println(TAG + "Is tracking");
        String trackingStr = LocalStorage.getInstance(context).getTrackingInfo();
        if (trackingStr == null) {
            return false;
        } else {
            try {
                JSONObject trackingObj = new JSONObject(trackingStr);
                boolean trackingStatus = trackingObj.optBoolean("STATUS");

                if (!trackingStatus) {
                    return false;
                } else {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void updateTracker(Double latitude, Double longitude) {
        System.out.println(TAG + "Update tracker!");

        try {
            JSONObject trackingObj = new JSONObject(LocalStorage.getInstance(context).getTrackingInfo());
            JSONArray waypoints = trackingObj.optJSONArray("WAYPOINTS");

            JSONObject waypoint = new JSONObject();
            waypoint.put("LAT", latitude);
            waypoint.put("LONG", longitude);

            waypoints.put(waypoint);

            trackingObj.put("WAYPOINTS", waypoints);

            LocalStorage.getInstance(context).setTrackingInfo(trackingObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startLocationUpdates() {
        System.out.println(TAG + "Start location updates!");

        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        System.out.println(TAG + "Location: " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
        listener.onLocationObtained(mCurrentLocation);
    }

    public Location getLocation() {
        System.out.println(TAG + "Get location!");

        if (mCurrentLocation == null) {
            System.out.println(TAG + "Current location is null!");
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else
            return mCurrentLocation;
    }

    public class LocalBinder extends Binder {

        public String BINDER_TAG = "Local Binder =>=> ";

        public LocationService getService() {
            System.out.println(BINDER_TAG + "Get service!");
            return LocationService.this;
        }

        public Location getCurrentLocation() {
            System.out.println(BINDER_TAG + "Get current location!");
            return getLocation();
        }
    }
}
