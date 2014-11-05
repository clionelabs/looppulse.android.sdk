package com.clionelabs.looppulse.sdk.auth;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.clionelabs.looppulse.sdk.datastore.DataStoreHelperSetupListener;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelper;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelperSetupListener;
import com.clionelabs.looppulse.sdk.services.Visitor;
import com.clionelabs.looppulse.sdk.datastore.DataStoreHelper;
import com.clionelabs.looppulse.sdk.util.PreferencesManager;

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

/**
 * Created by hiukim on 2014-10-16.
 *
 * This class abstract the multiple asynchronous calls on application authentication by providing a single onAuthenticated event back to the listener.
 * The asynchronous calls include the following:
 *      1) authenticate LoopPulse application
 *      2) authenticate firebase
 *      3) connecting beacon manager
 *          Note: 2) and 3) can only be started after 1) is successfully returned.
 */

public class AuthenticationManager {
    private static String TAG = AuthenticationManager.class.getCanonicalName();
    private static final String AUTH_URL = "http://localhost:8010/api/authenticate/applications/";
//    private static final String AUTH_URL = "http://192.168.0.102:3000/api/authenticate/applications/";

    private Context context;
    private AuthenticationListener authenticationListener;
    private PreferencesManager preferencesManager;
    private DataStoreHelper dataStoreHelper;
    private MonitorHelper monitorHelper;
    private Visitor visitor;
    private boolean isAutenticated;

    private enum HelperType {DATASTORE, MONITOR};
    private int helpersReadyMask;

    public AuthenticationManager(Context context, DataStoreHelper dataStoreHelper, MonitorHelper monitorHelper, PreferencesManager preferencesManager, Visitor visitor) {
        this.context = context;
        this.dataStoreHelper = dataStoreHelper;
        this.monitorHelper = monitorHelper;
        this.preferencesManager = preferencesManager;
        this.visitor = visitor;
        this.isAutenticated = false;
        this.helpersReadyMask = 0;
    }

    public void setAuthInfo(String appID, String appToken) {
        preferencesManager.updateAuthInfo(appID, appToken);
    }

    public void auth(AuthenticationListener listener) {
        authenticationListener = listener;
        String appID = preferencesManager.getAppId();
        String appToken = preferencesManager.getAppToken();
        (new AuthTask()).execute(appID, appToken);
    }

    public boolean isAutenticated() {
        return isAutenticated;
    }

    public boolean isAuthInfoReady() {
        return preferencesManager.getAppId() != null;
    }

    public void setHelperReady(HelperType type) {
        helpersReadyMask |= (1 << type.ordinal());
    }

    public boolean isAllHelpersReady() {
        return helpersReadyMask == (1 << HelperType.values().length) - 1;
    }

    private class AuthTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // get advertising id
                visitor.acquireUUID(context);

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
                    authenticationListener.onAuthenticationError("Error connecting to application server.");
                }
                return;
            }

            AuthenticationResult result = new AuthenticationResult(responseString);
            if (!result.isAuthenticated) {
                if (authenticationListener != null) {
                    authenticationListener.onAuthenticationError("Failed to authenticate application.");
                }
                return;
            }

            // TODO: Can also create a listener for monitor setup. Now we assume the asynchronous setup works.
            monitorHelper.setup(result, new MonitorHelperSetupListener() {
                @Override
                public void onReady() {
                    if (authenticationListener != null) {
                        setHelperReady(HelperType.MONITOR);
                        if (isAllHelpersReady()) {
                            isAutenticated = true;
                            authenticationListener.onAuthenticated();
                        }
                    }
                }

                @Override
                public void onError() {
                    if (authenticationListener != null) {
                        authenticationListener.onAuthenticationError("Failed setting up Beacon Monitor.");
                    }
                }
            });

            dataStoreHelper.setup(result, new DataStoreHelperSetupListener() {
                @Override
                public void onReady() {
                    if (authenticationListener != null) {
                        setHelperReady(HelperType.DATASTORE);
                        if (isAllHelpersReady()) {
                            isAutenticated = true;
                            authenticationListener.onAuthenticated();
                        }
                    }
                }

                @Override
                public void onError() {
                    if (authenticationListener != null) {
                        authenticationListener.onAuthenticationError("Failed to connect to FireBase.");
                    }
                }
            });
        }

        @Override
        protected void onCancelled() {
            if (authenticationListener != null) {
                authenticationListener.onAuthenticationError("Authentication cancelled");
            }
        }
    }
}
