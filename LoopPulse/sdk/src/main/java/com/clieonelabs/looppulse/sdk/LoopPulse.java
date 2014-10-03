package com.clieonelabs.looppulse.sdk;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.client.Firebase;

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
    public static final String EVENT_DID_ENTER_REGION = "LP_EVENT_DID_ENTER_REGION";
    public static final String EVENT_DID_EXIT_REGION = "LP_EVENT_EXIT_REGION";
    public static final String EVENT_DID_RANGE_BEACONS = "LP_EVENT_DID_RANGE_BEACONS";

    public static final String AUTH_URL = "http://192.168.0.101:3000/api/authenticate/applications/";

    private Application application;
    private LoopPulseListener loopPulseListener;
    private Context context;
    private String token;
    private String clientID;
    private DataStore dataStore;
    private Visitor visitor;
    private AbstractBeaconManager beaconManager;
    private PreferencesManager preferencesManager;

    public LoopPulse(Application application, LoopPulseListener loopPulseListener, String token, String clientID) {
        this(application, loopPulseListener, token, clientID, new HashMap<String, Object>());
    }

    public LoopPulse(Application application, LoopPulseListener loopPulseListener, String token, String clientID, Map<String, Object> options) {
        Log.d(TAG, "Initializing LoopPulse. clientID: " + clientID + ", token:" + token);

        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token argument cannot be empty");
        }
        if (clientID == null || clientID.length() == 0) {
            throw new IllegalArgumentException("clientID argument cannot be empty");
        }
        this.application = application;
        this.loopPulseListener = loopPulseListener;
        this.context = application.getApplicationContext();
        this.preferencesManager = PreferencesManager.getInstance(this.context);
        this.token = token;
        this.clientID = clientID;

        (new AuthTask()).execute((Void) null);
    }

    public String[] getAvailableEvents() {
        return new String[] { EVENT_DID_ENTER_REGION, EVENT_DID_EXIT_REGION, EVENT_DID_RANGE_BEACONS };
    }

    public void startLocationMonitoring() {
        beaconManager.startLocationMonitoring();
    }

    public void stopLocationMonitoringAndRanging() {
        beaconManager.stopLocationMonitoringAndRanging();
    }

    public void startLocationMonitoringAndRanging() {  // debug
        beaconManager.startLocationMonitoringAndRanging();
    }


    private void initFromPreferences() {
        Firebase.setAndroidContext(this.context);

        //        this.dataStore = new DataStore(context, clientID);
//        this.visitor = new Visitor(context);

        // Default beaconManager is estimote
//        this.beaconManager = new EstimoteBeaconManager(application, dataStore);
        this.beaconManager = new FakeBeaconManager(application, dataStore);

//        this.dataStore.registerVisitor(visitor);
        this.beaconManager.applicationDidLaunch();


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
            if (responseString == null) return;
            AuthResult result = new AuthResult(responseString);

            Log.d(TAG, "isAuthenticated: " + result.isAuthenticated);
            Log.d(TAG, "parseApplicationId: " + result.parseApplicationId);
            Log.d(TAG, "parseClientKey: " + result.parseClientKey);
            Log.d(TAG, "parseRestKey: " + result.parseRestKey);
            Log.d(TAG, "firebaseToken: " + result.firebaseToken);
            Log.d(TAG, "firebaseBeaconEventsURL: " + result.firebaseBeaconEventsURL);
            Log.d(TAG, "firebaseEngagementEventsURL: " + result.firebaseEngagementEventsURL);
            Log.d(TAG, "firebaseVisitorEventsURL: " + result.firebaseVisitorEventsURL);

            preferencesManager.updateWithAuthResult(result);

            initFromPreferences();
            loopPulseListener.didAuthenticated();
        }

        @Override
        protected void onCancelled() {
        }
    }
}
