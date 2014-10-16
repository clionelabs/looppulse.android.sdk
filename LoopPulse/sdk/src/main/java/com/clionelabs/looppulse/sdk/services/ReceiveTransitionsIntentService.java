package com.clionelabs.looppulse.sdk.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {
    private static String TAG = ReceiveTransitionsIntentService.class.getCanonicalName();

    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            if (LocationClient.hasError(intent)) {
                int errorCode = LocationClient.getErrorCode(intent);
                Log.e(TAG, "LocationClient Error: " + errorCode);
            } else {
                // Get the type of transition (entry or exit)
                int transition = LocationClient.getGeofenceTransition(intent);

                if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.d(TAG, "GeoFence Enter");
                } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.d(TAG, "GeoFence Exit");
                }
            }
        }
    }
}
