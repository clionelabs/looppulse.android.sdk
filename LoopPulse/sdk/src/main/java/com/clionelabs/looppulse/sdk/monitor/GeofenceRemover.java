package com.clionelabs.looppulse.sdk.monitor;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.List;

/**
 * Class for connecting to Location Services and removing geofences.
 * <p>
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing geofences.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p>
 * To use a GeofenceRemover, instantiate it, then call either RemoveGeofencesById() or
 * RemoveGeofencesByIntent(). Everything else is done automatically.
 *
 */
public class GeofenceRemover implements ConnectionCallbacks, OnConnectionFailedListener, OnRemoveGeofencesResultListener {
    private static String TAG = GeofenceRemover.class.getCanonicalName();

    private Context mContext;
    private List<String> mCurrentGeofenceIds;
    private LocationClient mLocationClient;
    private PendingIntent mCurrentIntent;

    public GeofenceRemover(Context context) {
        mContext = context;
        mCurrentGeofenceIds = null;
        mLocationClient = null;
    }

    public void removeGeofencesById(List<String> geofenceIds) {
        if ((null == geofenceIds) || (geofenceIds.size() == 0)) return;
        mCurrentGeofenceIds = geofenceIds;

        // connect location service first, and then remove Geofence in the connected callback
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(mContext, this, this);
        }
        mLocationClient.connect();
    }

    /*
     * Called by Location Services once the location client is connected. Continue by removing the requested geofences.
     */
    @Override
    public void onConnected(Bundle arg0) {
        Log.d(TAG, "LocationService onConnected()");

        // Continue the request to remove the geofences
        mLocationClient.removeGeofences(mCurrentGeofenceIds, this);
    }

    /*
     * Called by Location Services if the connection is lost.
     */
    @Override
    public void onDisconnected() {
        mLocationClient = null;
    }

    /**
     * Get a location client and disconnect from Location Services
     */
    private void requestDisconnection() {
        mLocationClient.disconnect();
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

    /**
     * When the request to remove geofences by IDs returns, handle the result.
     *
     * @param statusCode The code returned by Location Services
     * @param geofenceRequestIds The IDs removed
     */
    @Override
    public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Log.d(TAG, "geoFenceRemove successful");
        } else {
            Log.d(TAG, "geoFenceRemove failed");
        }
        requestDisconnection();
    }

    /**
     * When the request to remove geofences by PendingIntent returns, handle the result.
     *
     * @param statusCode the code returned by Location Services
     * @param requestIntent The Intent used to request the removal.
     */
    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
        // We haven't done removeGeofencesByPendingIntent, so it's not supposed to be called.
        Log.e(TAG, "unimplemented method");
    }
}
