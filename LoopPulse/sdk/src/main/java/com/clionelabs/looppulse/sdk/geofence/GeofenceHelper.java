package com.clionelabs.looppulse.sdk.geofence;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

/**
 * Created by hiukim on 2014-10-15.
 */
public class GeofenceHelper {
    private static String TAG = GeofenceHelper.class.getCanonicalName();

    private Context context;

    String requestId = "1";
    double latitude = 22.286763;
    double longitude = 114.190319;
    float radius = 50.0f;

    public GeofenceHelper(Context context) {
        this.context = context;
    }

    public void startMonitoring() {
        if (!servicesConnected()) {
            return;
        }

        GeofenceRequester geofenceRequester = new GeofenceRequester(context);
        ArrayList<GeofenceLocation> locations = new ArrayList<GeofenceLocation>();
        locations.add(new GeofenceLocation(requestId, latitude, longitude, radius));
        geofenceRequester.addGeofences(locations);
    }

    public void stopMonitoring() {
        if (!servicesConnected()) {
            return;
        }

        GeofenceRemover geofenceRemover = new GeofenceRemover(context);
        ArrayList<String> requestIds = new ArrayList<String>();
        requestIds.add(requestId);
        geofenceRemover.removeGeofencesById(requestIds);
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
            // Google Play services was not available for some reason
        } else {
            Log.d(TAG, "google play services not available: " + resultCode);
            return false;
        }
    }
}
