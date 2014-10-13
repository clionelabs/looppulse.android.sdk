package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.PreferencesManager;
import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.clionelabs.looppulse.sdk.model.FirebaseEvent;
import com.clionelabs.looppulse.sdk.model.Visitor;
import com.clionelabs.looppulse.sdk.model.VisitorIdentifyEvent;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by hiukim on 2014-10-14.
 */
public class DataStoreServiceHelper {
    private static String TAG = DataStoreService.class.getCanonicalName();

    private Context context;
    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private Visitor visitor;
    private DataStoreServiceHelperListener listener;

    public DataStoreServiceHelper(Context context, DataStoreServiceHelperListener listener) {
        this.context = context;
        this.listener = listener;
        init();
    }

    public void createFirebaseBeaconEvent(BeaconEvent event) {
        createFirebaseEvent(beaconEventsRef, event);
    }

    public void createFirebaseVisitorIdentifyEvent(VisitorIdentifyEvent event) {
        createFirebaseEvent(visitorEventsRef, event);
    }

    private void createFirebaseEvent(Firebase firebaseRef, FirebaseEvent event) {
        firebaseRef.push().setValue(event.toFirebaseObject(visitor.getUUID()), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.d(TAG, "Data saved successfully.");
                }
            }
        });
    }

    private void init() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance(context);

        this.visitor = new Visitor(context);
        Firebase.setAndroidContext(context);
        this.rootRef = new Firebase(preferencesManager.getFirebaseRootUrl());
        this.beaconEventsRef = new Firebase(preferencesManager.getFirebaseBeaconEventsUrl());
        this.visitorEventsRef = new Firebase(preferencesManager.getFirebaseVisitorEventsUrl());
        this.engagementEventsRef = new Firebase(preferencesManager.getFirebaseEngagementEventsUrl());

        this.rootRef.authWithCustomToken(preferencesManager.getFirebaseToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "firebase authenticated: " + authData);
                listener.onFinishedInit(true);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "firebase authentication error: " + firebaseError);
                listener.onFinishedInit(false);
            }
        });
    }
}
