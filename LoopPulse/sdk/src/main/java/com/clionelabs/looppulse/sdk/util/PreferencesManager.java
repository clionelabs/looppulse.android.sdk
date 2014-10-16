package com.clionelabs.looppulse.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.clionelabs.looppulse.sdk.account.AuthenticationResult;

/**
 * Created by hiukim on 2014-10-04.
 */
public class PreferencesManager {
    private static final String TAG = PreferencesManager.class.getSimpleName();

    public static final String PREF_NAME = "com.clionelabs.looppulse";

    public static final String PARSE_APPLICATION_ID = "PARSE_APPLICATION_ID";
    public static final String PARSE_CLIENT_KEY = "PARSE_CLIENT_KEY";
    public static final String PARSE_REST_KEY = "PARSE_REST_KEY";
    public static final String FIREBASE_TOKEN = "FIREBASE_TOKEN";
    public static final String FIREBASE_ROOT_URL = "FIREBASE_ROOT";
    public static final String FIREBASE_BEACON_EVENTS_URL = "FIREBASE_BEACON_EVENTS_URL";
    public static final String FIREBASE_ENGAGEMENT_EVENTS_URL = "FIREBASE_ENGAGEMENT_EVENTS_URL";
    public static final String FIREBASE_VISITOR_EVENTS_URL = "FIREBASE_VISITOR_EVENTS_URL";

    private static PreferencesManager instance;
    private Context mContext;

    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager();
            instance.mContext = context;
        }
        return instance;
    }

    public void clearAll() {
        SharedPreferences settings = mContext.getSharedPreferences(PREF_NAME, 0);
        settings.edit().clear().commit();
    }

    public void updateWithAuthResult(AuthenticationResult result) {
        SharedPreferences settings = mContext.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PARSE_APPLICATION_ID, result.parseApplicationId);
        editor.putString(PARSE_CLIENT_KEY, result.parseClientKey);
        editor.putString(PARSE_REST_KEY, result.parseRestKey);
        editor.putString(FIREBASE_TOKEN, result.firebaseToken);
        editor.putString(FIREBASE_ROOT_URL, result.firebaseRoot);
        editor.putString(FIREBASE_BEACON_EVENTS_URL, result.firebaseBeaconEventsURL);
        editor.putString(FIREBASE_ENGAGEMENT_EVENTS_URL, result.firebaseEngagementEventsURL);
        editor.putString(FIREBASE_VISITOR_EVENTS_URL, result.firebaseVisitorEventsURL);
        editor.commit();
    }

    public String getParseApplicationId() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(PARSE_APPLICATION_ID, null);
    }

    public String getFirebaseToken() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(FIREBASE_TOKEN, null);
    }

    public String getFirebaseRootUrl() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(FIREBASE_ROOT_URL, null);
    }

    public String getFirebaseBeaconEventsUrl() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(FIREBASE_BEACON_EVENTS_URL, null);
    }

    public String getFirebaseEngagementEventsUrl() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(FIREBASE_ENGAGEMENT_EVENTS_URL, null);
    }

    public String getFirebaseVisitorEventsUrl() {
        return mContext.getSharedPreferences(PREF_NAME, 0).getString(FIREBASE_VISITOR_EVENTS_URL, null);
    }
}
