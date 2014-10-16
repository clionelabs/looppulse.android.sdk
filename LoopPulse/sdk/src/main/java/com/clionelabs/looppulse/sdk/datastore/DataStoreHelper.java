package com.clionelabs.looppulse.sdk.datastore;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.Helper;
import com.clionelabs.looppulse.sdk.services.HelperListener;
import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by hiukim on 2014-10-16.
 */
public class DataStoreHelper implements Helper {
    private static String TAG = DataStoreHelper.class.getCanonicalName();

    private Context context;
    private PreferencesManager preferencesManager;
    private String visitorUUID;
    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private boolean isReady;

    public DataStoreHelper(Context context, PreferencesManager preferencesManager, String visitorUUID) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.visitorUUID = visitorUUID;
        this.isReady = false;
    }

    @Override
    public void setup(final HelperListener listener) {
        if (isReady) {
            listener.onReady();
            return;
        }

        Firebase.setAndroidContext(context);
        this.rootRef = new Firebase(preferencesManager.getFirebaseRootUrl());
        this.beaconEventsRef = new Firebase(preferencesManager.getFirebaseBeaconEventsUrl());
        this.visitorEventsRef = new Firebase(preferencesManager.getFirebaseVisitorEventsUrl());
        this.engagementEventsRef = new Firebase(preferencesManager.getFirebaseEngagementEventsUrl());

        this.rootRef.authWithCustomToken(preferencesManager.getFirebaseToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "firebase authenticated: " + authData);
                isReady = true;
                listener.onReady();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "firebase authentication error: " + firebaseError);
                listener.onError();
            }
        });
    }

    public void createFirebaseBeaconEvent(BeaconEvent event) {
        createFirebaseEvent(beaconEventsRef, event);
    }

    public void createFirebaseVisitorIdentifyEvent(VisitorIdentifyEvent event) {
        createFirebaseEvent(visitorEventsRef, event);
    }

    private void createFirebaseEvent(Firebase firebaseRef, final FirebaseEvent event) {
        if (!isReady) {
            Log.e(TAG, "Firebase not authenticated yet");
            return;
        }

        firebaseRef.push().setValue(event.toFirebaseObject(visitorUUID), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.d(TAG, "Data saved successfully: " + event.toFirebaseObject(visitorUUID));
                }
            }
        });
    }
}
