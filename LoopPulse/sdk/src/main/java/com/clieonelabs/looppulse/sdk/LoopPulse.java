package com.clieonelabs.looppulse.sdk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hiukim on 2014-10-03.
 */
public class LoopPulse {
    public static final String TAG = "LoopPulse";
    public static final String AUTH_URL = "http://192.168.0.101:3000/api/authenticate/applications/";

    private LoopPulseListener loopPulseListener;
    private Context context;
    private String token;
    private String clientID;
    private DataStore dataStore;
    private Visitor visitor;
    private AbstractBeaconManager beaconManager;
    private PreferencesManager preferencesManager;

    private int initializationsMask;
    private boolean initializationEncounteredError;
    private enum Initialization {
        DATASTORE, BEACON_MANAGER;

        public int value() {
            return 1 << ordinal();
        }
    }

    public LoopPulse(Context context, LoopPulseListener loopPulseListener, String token, String clientID) {
        this(context, loopPulseListener, token, clientID, new HashMap<String, Object>());
    }

    public LoopPulse(Context context, LoopPulseListener loopPulseListener, String token, String clientID, Map<String, Object> options) {
        Log.d(TAG, "Initializing LoopPulse. clientID: " + clientID + ", token:" + token);

        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token argument cannot be empty");
        }
        if (clientID == null || clientID.length() == 0) {
            throw new IllegalArgumentException("clientID argument cannot be empty");
        }
        this.context = context;
        this.visitor = new Visitor(context);
        this.loopPulseListener = loopPulseListener;
        this.preferencesManager = PreferencesManager.getInstance(this.context);
        this.token = token;
        this.clientID = clientID;

        (new AuthTask()).execute((Void) null);
    }

    public void startLocationMonitoring() {
        if (!isInitialized()) throw new RuntimeException("LoopPulse is not initialized yet.");
        beaconManager.startLocationMonitoring();
    }

    public void stopLocationMonitoring() {  // debug
        if (!isInitialized()) throw new RuntimeException("LoopPulse is not initialized yet.");
        beaconManager.stopLocationMonitoring();
    }

    public void identifyVisitorWithExternalId(String externalId) {
        if (!isInitialized()) throw new RuntimeException("LoopPulse is not initialized yet.");
        visitor.setExternalID(externalId);
    }

    private void initDataStore() {
        HashMap<String, String> firebaseURLs = new HashMap<String, String>();

        String firebaseToken = preferencesManager.getFirebaseToken();
        firebaseURLs.put("root", preferencesManager.getFirebaseRootUrl());
        firebaseURLs.put("beacon_events", preferencesManager.getFirebaseBeaconEventsUrl());
        firebaseURLs.put("visitor_events", preferencesManager.getFirebaseVisitorEventsUrl());
        firebaseURLs.put("engagement_events", preferencesManager.getFirebaseEngagementEventsUrl());
        this.dataStore = new DataStore(context, visitor, firebaseToken, firebaseURLs, new DataStoreResultHandler() {
            @Override
            public void onAuthenticated() {
                initialized(Initialization.DATASTORE);
            }

            @Override
            public void onAuthenticationError() {
                if (!initializationEncounteredError) {
                    loopPulseListener.onAuthenticationError("Failed to connect Firebase data store");
                    initializationEncounteredError = true;
                }
            }
        });
    }

    private void initBeaconManager() {
        //        this.visitor = new Visitor(context);

        // Default beaconManager is estimote
//        this.beaconManager = new EstimoteBeaconManager(application, dataStore);
        this.beaconManager = new FakeBeaconManager(context, dataStore);

//        this.dataStore.registerVisitor(visitor);
        this.beaconManager.initialize();

        initialized(Initialization.BEACON_MANAGER);
    }

    private void initialized(Initialization flag) {
        initializationsMask |= flag.value();
        if (isInitialized()) {
            loopPulseListener.onAuthenticated();
        }
    }

    private boolean isInitialized() {
        return (initializationsMask == (1 << Initialization.values().length) - 1);
    }

    private class AuthTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet get = new HttpGet(AUTH_URL + clientID);
                get.setHeader("x-auth-token", token);
                HttpResponse response = httpclient.execute(get);

                StatusLine statusLine = response.getStatusLine();
                Log.d(TAG, "status: " + statusLine.getStatusCode());
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    String responseString = out.toString();
                    Log.d(TAG, "response: " + responseString);
                    return responseString;
                } else {
                    response.getEntity().getContent().close(); // Closes the connection.
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseString) {
            if (responseString == null) {
                loopPulseListener.onAuthenticationError("Failed to authenticate");
                return;
            }

            AuthResult result = new AuthResult(responseString);
            if (!result.isAuthenticated) {
                loopPulseListener.onAuthenticationError("Invalid clientID/Token");
                return;
            }

            Log.d(TAG, "isAuthenticated: " + result.isAuthenticated);
            Log.d(TAG, "parseApplicationId: " + result.parseApplicationId);
            Log.d(TAG, "parseClientKey: " + result.parseClientKey);
            Log.d(TAG, "parseRestKey: " + result.parseRestKey);
            Log.d(TAG, "firebaseToken: " + result.firebaseToken);
            Log.d(TAG, "firebaseRoot: " + result.firebaseRoot);
            Log.d(TAG, "firebaseBeaconEventsURL: " + result.firebaseBeaconEventsURL);
            Log.d(TAG, "firebaseEngagementEventsURL: " + result.firebaseEngagementEventsURL);
            Log.d(TAG, "firebaseVisitorEventsURL: " + result.firebaseVisitorEventsURL);
            preferencesManager.updateWithAuthResult(result);

            initDataStore();
            initBeaconManager();
        }

        @Override
        protected void onCancelled() {
            loopPulseListener.onAuthenticationError("Authentication terminated");
        }
    }
}
