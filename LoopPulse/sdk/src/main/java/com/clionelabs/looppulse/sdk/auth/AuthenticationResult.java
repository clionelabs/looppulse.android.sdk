package com.clionelabs.looppulse.sdk.auth;

import android.util.Log;

import com.clionelabs.looppulse.sdk.monitor.GeofenceLocation;
import com.estimote.sdk.Region;

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

    public ArrayList<Region> beaconRegions;

    public ArrayList<GeofenceLocation> geofenceLocations;

    public AuthenticationResult(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            isAuthenticated = jsonObject.getBoolean("authenticated");

            if (isAuthenticated) {
                JSONObject systemObject = jsonObject.getJSONObject("system");
                JSONObject parseObject = systemObject.getJSONObject("parse");
                JSONObject firebaseObject = systemObject.getJSONObject("firebase");
                JSONObject locationsObject = systemObject.getJSONObject("locations");
                Iterator<String> locationKeys = locationsObject.keys();

                geofenceLocations = new ArrayList<GeofenceLocation>();
                beaconRegions = new ArrayList<Region>();
                while (locationKeys.hasNext()) {
                    String locationKey = locationKeys.next();
                    JSONObject locationObject = locationsObject.getJSONObject(locationKey);
                    JSONObject coordinateObject = locationObject.getJSONObject("coordinate");
                    double latitude = coordinateObject.getDouble("latitude");
                    double longitude = coordinateObject.getDouble("longitude");
                    float radius = (float) coordinateObject.getDouble("radius");
                    geofenceLocations.add(new GeofenceLocation(locationKey, latitude, longitude, radius));
                    Log.d(TAG, "coordinate: " + latitude + ", " + longitude + ", " + radius);

                    JSONObject installationsObject = locationObject.getJSONObject("installations");
                    Iterator<String> installationKeys = installationsObject.keys();
                    while (installationKeys.hasNext()) {
                        String installationKey = installationKeys.next();
                        JSONObject installationObject = installationsObject.getJSONObject(installationKey);
                        JSONObject beaconObject = installationObject.getJSONObject("beacon");
                        String proximateUUID = beaconObject.getString("proximityUUID").toLowerCase();
                        int major = beaconObject.getInt("major");
                        int minor = beaconObject.getInt("minor");
                        beaconRegions.add(new Region(installationKey, proximateUUID, major, minor));
                        Log.d(TAG, "adding beacon: " + proximateUUID + ", " + major + ", " + minor);
                    }
                }

                parseApplicationId = parseObject.getString("applicationId");
                parseClientKey = parseObject.getString("clientKey");
                parseRestKey = parseObject.getString("restKey");

                firebaseToken = firebaseObject.getString("token");
                firebaseRoot = firebaseObject.getString("root");
                firebaseBeaconEventsURL = firebaseObject.getString("beacon_events");
                firebaseEngagementEventsURL = firebaseObject.getString("engagement_events");
                firebaseVisitorEventsURL = firebaseObject.getString("visitor_events");

                Log.d(TAG, "isAuthenticated: " + isAuthenticated);
                Log.d(TAG, "parseApplicationId: " + parseApplicationId);
                Log.d(TAG, "parseClientKey: " + parseClientKey);
                Log.d(TAG, "parseRestKey: " + parseRestKey);
                Log.d(TAG, "firebaseToken: " + firebaseToken);
                Log.d(TAG, "firebaseRoot: " + firebaseRoot);
                Log.d(TAG, "firebaseBeaconEventsURL: " + firebaseBeaconEventsURL);
                Log.d(TAG, "firebaseEngagementEventsURL: " + firebaseEngagementEventsURL);
                Log.d(TAG, "firebaseVisitorEventsURL: " + firebaseVisitorEventsURL);
            }
        } catch (JSONException e) {
            isAuthenticated = false;
            e.printStackTrace();
        }
    }
}
