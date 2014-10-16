package com.clionelabs.looppulse.sdk.account;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.clionelabs.looppulse.sdk.datastore.VisitorIdentifyEvent;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by hiukim on 2014-10-16.
 */
public class AccountHelper {
    private static String TAG = AccountHelper.class.getCanonicalName();
    private static final String AUTH_URL = "http://localhost:8010/api/authenticate/applications/";

    private Context context;
    private AuthenticationListener authenticationListener;
    private PreferencesManager preferencesManager;
    private Visitor visitor;

    public AccountHelper(Context context, PreferencesManager preferencesManager) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.visitor = new Visitor(context);
    }

    public void auth(String appID, String appToken, AuthenticationListener listener) {
        authenticationListener = listener;
        (new AuthTask()).execute(appID, appToken);
    }

    public VisitorIdentifyEvent identifyUser(String externalID) {
        visitor.setExternalID(externalID);
        VisitorIdentifyEvent event = new VisitorIdentifyEvent(externalID, new Date());
        return event;
    }

    private class AuthTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String appID = params[0];
                String appToken = params[1];

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost(AUTH_URL + appID);
                post.setHeader("x-auth-token", appToken);
                post.setHeader("Content-Type", "application/json");
                JSONObject sdkObj = new JSONObject();
                sdkObj.put("version", "0.5");

                JSONObject deviceObj = new JSONObject();
                deviceObj.put("model", visitor.getModel());
                deviceObj.put("systemVersion", visitor.getSystemVersion());

                JSONObject sessionObj = new JSONObject();
                sessionObj.put("visitorUUID", visitor.getUUID());
                sessionObj.put("sdk", sdkObj);
                sessionObj.put("device", deviceObj);

                JSONObject obj = new JSONObject();
                obj.put("session", sessionObj);
                post.setEntity(new StringEntity(obj.toString()));

                HttpResponse response = httpclient.execute(post);

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
            } catch (JSONException ex) {
                Log.e(TAG, "error building auth request: " + ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String responseString) {
            if (responseString == null) {
                if (authenticationListener != null) {
                    authenticationListener.onAuthenticationError("Failed to authenticate");
                }
                return;
            }

            AuthenticationResult result = new AuthenticationResult(responseString);
            if (!result.isAuthenticated) {
                if (authenticationListener != null) {
                    authenticationListener.onAuthenticationError("Invalid clientID/Token");
                }
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

            if (authenticationListener != null) {
                authenticationListener.onAuthenticated();
            }
        }

        @Override
        protected void onCancelled() {
            if (authenticationListener != null) {
                authenticationListener.onAuthenticationError("Authentication cancelled");
            }
        }
    }
}
