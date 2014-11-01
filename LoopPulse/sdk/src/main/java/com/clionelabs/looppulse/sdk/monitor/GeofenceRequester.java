package com.clionelabs.looppulse.sdk.monitor;

/**
 * Created by hiukim on 2014-10-15.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.Arrays;

public class GeofenceRequester implements OnAddGeofencesResultListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static String TAG = GeofenceRequester.class.getCanonicalName();

    private Context context;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mCurrentGeofences;
    private LocationClient mLocationClient;

    public GeofenceRequester(Context context) {
        this.context = context;
        mGeofencePendingIntent = null;
        mLocationClient = null;
    }

    public void addGeofences(ArrayList<GeofenceLocation> locations) {
        mCurrentGeofences = new ArrayList<Geofence>();
        for (GeofenceLocation location: locations) {
            mCurrentGeofences.add(location.makeGeofence());
        }

        // connect location service first, and then add Geofence in the connected callback
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(context, this, this);
        }
        mLocationClient.connect();
    }

    /**
     * Get a location client and disconnect from Location Services
     */
    private void requestDisconnection() {
        mLocationClient.disconnect();
    }

    /*
     * Called by Location Services once the location client is connected.
     *
     * Continue by adding the requested geofences.
     */
    @Override
    public void onConnected(Bundle arg0) {
        Log.d(TAG, "LocationService onConnected()");

        // Continue adding the geofences
        LoopPulseServiceExecutor.setGeofenceEventTrigger(context, mGeofencePendingIntent, mLocationClient, mCurrentGeofences, this);
    }

    /*
     * Called by Location Services once the location client is disconnected.
     */
    @Override
    public void onDisconnected() {
        // Destroy the current location client
        mLocationClient = null;
    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed()");
    }

    /*
     * Handle the result of adding the geofences
     */
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Log.d(TAG, "geoFenceAdded successful: " + Arrays.toString(geofenceRequestIds));
        } else {
            Log.d(TAG, "geoFenceAdded failed: " + Arrays.toString(geofenceRequestIds));
        }
        // Disconnect the location client
        requestDisconnection();
    }
}
