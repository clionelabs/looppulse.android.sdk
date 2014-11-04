package com.clionelabs.looppulse.sdk.monitor;

import com.google.android.gms.location.Geofence;

/**
 * Created by hiukim on 2014-10-14.
 */
public class GeofenceLocation {
    private String requestId;
    private double latitude;
    private double longitude;
    private float radius;

    public GeofenceLocation(String requestId, double latitude, double longitude, float radius) {
        this.requestId = requestId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getRequestId() {
        return requestId;
    }

    public Geofence makeGeofence() {
        return new Geofence.Builder()
                .setRequestId(requestId)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}
