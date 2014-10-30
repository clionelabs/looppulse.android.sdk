package com.clionelabs.looppulse.sdk.account;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hiukim on 2014-10-04.
 */
public class AuthenticationResult {
    public boolean isAuthenticated;

    public String parseApplicationId;
    public String parseClientKey;
    public String parseRestKey;

    public String firebaseToken;
    public String firebaseRoot;
    public String firebaseBeaconEventsURL;
    public String firebaseEngagementEventsURL;
    public String firebaseVisitorEventsURL;

    public AuthenticationResult(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            isAuthenticated = jsonObject.getBoolean("authenticated");

            if (isAuthenticated) {
                JSONObject systemObject = jsonObject.getJSONObject("system");
                JSONObject parseObject = systemObject.getJSONObject("parse");
                JSONObject firebaseObject = systemObject.getJSONObject("firebase");

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
