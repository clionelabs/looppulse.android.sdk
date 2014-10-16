package com.clionelabs.looppulse.sdk.datastore;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hiukim on 2014-10-16.
 */
public class DataStoreHelper {
    private static String TAG = DataStoreHelper.class.getCanonicalName();

    private Context context;
    private PreferencesManager preferencesManager;
    private boolean isFirebaseAuthenticated;
    private String visitorUUID;
    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private ArrayList<FirebaseAuthenticationListener> authListeners;
    private final Object isAuthenticatingLock = new Object();
    private boolean isAuthenticating;

    public DataStoreHelper(Context context, PreferencesManager preferencesManager, String visitorUUID) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.visitorUUID = visitorUUID;
        this.isFirebaseAuthenticated = false;
        this.authListeners = new ArrayList<FirebaseAuthenticationListener>();
    }

    public void authenticateFirebase(final FirebaseAuthenticationListener listener) {
        if (isFirebaseAuthenticated) {
            listener.onAuthenticated(true);
            return;
        }

        authListeners.add(listener);
        if (getIsAuthenticating()) {
            return;
        }
        setIsAuthenticating(true);

        Firebase.setAndroidContext(context);
        this.rootRef = new Firebase(preferencesManager.getFirebaseRootUrl());
        this.beaconEventsRef = new Firebase(preferencesManager.getFirebaseBeaconEventsUrl());
        this.visitorEventsRef = new Firebase(preferencesManager.getFirebaseVisitorEventsUrl());
        this.engagementEventsRef = new Firebase(preferencesManager.getFirebaseEngagementEventsUrl());

        this.rootRef.authWithCustomToken(preferencesManager.getFirebaseToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "firebase authenticated: " + authData);
                isFirebaseAuthenticated = true;
                Iterator<FirebaseAuthenticationListener> iterator = authListeners.iterator();
                while (iterator.hasNext()) {
                    FirebaseAuthenticationListener listener = iterator.next();
                    listener.onAuthenticated(true);
                    iterator.remove();
                }

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "firebase authentication error: " + firebaseError);
                Iterator<FirebaseAuthenticationListener> iterator = authListeners.iterator();
                while (iterator.hasNext()) {
                    FirebaseAuthenticationListener listener = iterator.next();
                    listener.onAuthenticated(false);
                    iterator.remove();
                }
            }
        });
    }

    public void createFirebaseBeaconEvent(BeaconEvent event) {
        createFirebaseEvent(beaconEventsRef, event);
    }

    public void createFirebaseVisitorIdentifyEvent(VisitorIdentifyEvent event) {
        createFirebaseEvent(visitorEventsRef, event);
    }

    private void createFirebaseEvent(Firebase firebaseRef, FirebaseEvent event) {
        if (!isFirebaseAuthenticated) {
            Log.e(TAG, "Firebase not authenticated yet");
            return;
        }

        firebaseRef.push().setValue(event.toFirebaseObject(visitorUUID), new Firebase.CompletionListener() {
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

    private boolean getIsAuthenticating() {
        synchronized (isAuthenticatingLock) {
            return isAuthenticating;
        }
    }

    private void setIsAuthenticating(boolean isAuthenticating) {
        synchronized (isAuthenticatingLock) {
            this.isAuthenticating = isAuthenticating;
        }
    }
}
