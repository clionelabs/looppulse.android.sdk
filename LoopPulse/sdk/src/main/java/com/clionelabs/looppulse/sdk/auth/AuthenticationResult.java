package com.clionelabs.looppulse.sdk.auth;

import android.util.Log;

import com.clionelabs.looppulse.sdk.monitor.GeofenceLocation;
import com.estimote.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hiukim on 2014-10-04.
 */
public class AuthenticationResult {
    private static String TAG = AuthenticationResult.class.getCanonicalName();

    public boolean isAuthenticated;

    public String firebaseToken;
    public String firebaseRoot;
    public String firebaseBeaconEventsURL;
    public String firebaseVisitorEventsURL;

    public ArrayList<Region> beaconRegions;

    public ArrayList<GeofenceLocation> geofenceLocations;

    public AuthenticationResult(String responseString) {
        geofenceLocations = new ArrayList<GeofenceLocation>();
        beaconRegions = new ArrayList<Region>();

        try {
            JSONObject jsonObject = new JSONObject(responseString);
            isAuthenticated = jsonObject.getBoolean("authenticated");

            if (isAuthenticated) {
                JSONObject systemObject = jsonObject.getJSONObject("system");
                JSONObject firebaseObject = systemObject.getJSONObject("firebase");
                JSONArray poisArray = systemObject.getJSONArray("pois");
                for (int i = 0; i < poisArray.length(); i++) {
                    JSONObject poiObject = poisArray.getJSONObject(i);
                    String name = poiObject.getString("name");
                    JSONObject beaconObject = poiObject.getJSONObject("beacon");
                    String uuid = beaconObject.getString("uuid");
                    int major = beaconObject.getInt("major");
                    int minor = beaconObject.getInt("minor");

                    beaconRegions.add(new Region(name, uuid, major, minor));
                    Log.d(TAG, "adding beacon: " + uuid + ", " + major + ", " + minor);
                }

                firebaseToken = firebaseObject.getString("token");
                firebaseRoot = firebaseObject.getString("root");
                JSONObject firebasePathsObject = firebaseObject.getJSONObject("paths");
                firebaseBeaconEventsURL = firebasePathsObject.getString("beaconEvents");
                firebaseVisitorEventsURL = firebasePathsObject.getString("visitorEvents");

                Log.d(TAG, "isAuthenticated: " + isAuthenticated);
                Log.d(TAG, "firebaseToken: " + firebaseToken);
                Log.d(TAG, "firebaseRoot: " + firebaseRoot);
                Log.d(TAG, "firebaseBeaconEventsURL: " + firebaseBeaconEventsURL);
                Log.d(TAG, "firebaseVisitorEventsURL: " + firebaseVisitorEventsURL);
            }
        } catch (JSONException e) {
            isAuthenticated = false;
            e.printStackTrace();
        }
    }
}
