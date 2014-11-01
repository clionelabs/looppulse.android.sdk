package com.clionelabs.looppulse.sdk.auth;

import android.util.Log;

import com.clionelabs.looppulse.sdk.monitor.GeofenceLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hiukim on 2014-10-04.
 */
public class AuthenticationResult {
    private static String TAG = AuthenticationResult.class.getCanonicalName();

    public boolean isAuthenticated;

    public String parseApplicationId;
    public String parseClientKey;
    public String parseRestKey;

    public String firebaseToken;
    public String firebaseRoot;
    public String firebaseBeaconEventsURL;
    public String firebaseEngagementEventsURL;
    public String firebaseVisitorEventsURL;

    public ArrayList<GeofenceLocation> geofenceLocations;

    public AuthenticationResult(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            isAuthenticated = jsonObject.getBoolean("authenticated");

            if (isAuthenticated) {
                JSONObject systemObject = jsonObject.getJSONObject("system");
                JSONObject parseObject = systemObject.getJSONObject("parse");
                JSONObject firebaseObject = systemObject.getJSONObject("firebase");
                JSONObject geofencesObject = systemObject.getJSONObject("geofences");

                geofenceLocations = new ArrayList<GeofenceLocation>();
                Iterator<String> keys = geofencesObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject locationObject = geofencesObject.getJSONObject(key);
                    double latitude = locationObject.getDouble("latitude");
                    double longitude = locationObject.getDouble("longitude");
                    float radius = (float) locationObject.getDouble("radius");
                    geofenceLocations.add(new GeofenceLocation(key, latitude, longitude, radius));
                    Log.d(TAG, "coordinate: " + latitude + ", " + longitude + ", " + radius);
                }

                parseApplicationId = parseObject.getString("applicationId");
                parseClientKey = parseObject.getString("clientKey");
                parseRestKey = parseObject.getString("restKey");

                firebaseToken = firebaseObject.getString("token");
                firebaseRoot = firebaseObject.getString("root");
                firebaseBeaconEventsURL = firebaseObject.getString("beacon_events");
                firebaseEngagementEventsURL = firebaseObject.getString("engagement_events");
                firebaseVisitorEventsURL = firebaseObject.getString("visitor_events");
            }
        } catch (JSONException e) {
            isAuthenticated = false;
            e.printStackTrace();
        }
    }
}
