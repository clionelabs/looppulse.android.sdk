package com.clionelabs.looppulse.sdk.datastore;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.auth.AuthenticationResult;
import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-16.
 */
public class DataStoreHelper {
    private static String TAG = DataStoreHelper.class.getCanonicalName();

    private Context context;
    private PreferencesManager preferencesManager;
    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private boolean isReady;

    public DataStoreHelper(Context context, PreferencesManager preferencesManager) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.isReady = false;
    }

    public void setup(AuthenticationResult authenticationResult, final DataStoreHelperSetupListener listener) {
        Firebase.setAndroidContext(context);

        this.rootRef = new Firebase(authenticationResult.firebaseRoot);
        this.beaconEventsRef = new Firebase(authenticationResult.firebaseBeaconEventsURL);
        this.visitorEventsRef = new Firebase(authenticationResult.firebaseVisitorEventsURL);
        this.engagementEventsRef = new Firebase(authenticationResult.firebaseEngagementEventsURL);

        this.rootRef.authWithCustomToken(authenticationResult.firebaseToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "FireBase authenticated: " + authData);
                isReady = true;
                listener.onReady();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "FireBase authentication error: " + firebaseError);
                listener.onError();
            }
        });
    }

    public void tagVisitor(String visitorUUID, HashMap<String, String> properties) {
        VisitorTagEvent event = new VisitorTagEvent(visitorUUID, properties, new Date());
        createFirebaseEvent(visitorEventsRef, event);
    }

    public void identifyVisitor(String visitorUUID, String externalID) {
        VisitorIdentifyEvent event = new VisitorIdentifyEvent(visitorUUID, externalID, new Date());
        createFirebaseEvent(visitorEventsRef, event);
    }

    public void createFirebaseBeaconEvent(BeaconEvent event) {
        createFirebaseEvent(beaconEventsRef, event);
    }

    private void createFirebaseEvent(Firebase firebaseRef, final FirebaseEvent event) {
        if (!isReady) {
            Log.e(TAG, "Firebase not authenticated yet");
            return;
        }

        firebaseRef.push().setValue(event.toFirebaseObject(), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.d(TAG, "Data saved successfully: " + event.toFirebaseObject());
                }
            }
        });
    }
}
