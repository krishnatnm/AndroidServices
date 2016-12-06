package com.tanmay.androidservices.interfaces;

import android.location.Location;

/**
 * Created by TaNMay on 19/07/16.
 */
public interface OnLocationObtainedListener {

    void onLocationObtained(Location location);

    void onLocationChanged(Location location);
}
