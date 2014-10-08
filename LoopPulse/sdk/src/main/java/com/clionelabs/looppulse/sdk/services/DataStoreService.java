package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clionelabs.looppulse.sdk.PreferencesManager;
import com.clionelabs.looppulse.sdk.model.Visitor;
import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.clionelabs.looppulse.sdk.model.FirebaseEvent;
import com.clionelabs.looppulse.sdk.model.VisitorIdentifyEvent;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class DataStoreService extends Service {
    private static String TAG = DataStoreService.class.getCanonicalName();

    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private Visitor visitor;

    public static final String BROADCAST_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.Event";
    public static final String BROADCAST_EVENT_TYPE = "com.clionelabs.looppulse.sdk.services.DataStoreService.EventType";
    public static final String EXTRA_BEACON_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.EXTRA_BEACON_EVENT";
    public static final String EXTRA_IDENTIFY_VISITOR_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.EXTRA_IDENTIFY_VISITOR_EVENT";
    public enum EventType {INIT_SUCCESS, INIT_FAIL};
    public enum ActionType {INIT, FIRE_BEACON_EVENT, FIRE_IDENTIFY_VISITOR_EVENT};

    public DataStoreService() {
    }

    public static void startAction(Context context, ActionType action) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(action.toString());
        context.startService(intent);
    }

    public static void startFireBeaconAction(Context context, BeaconEvent beaconEvent) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(ActionType.FIRE_BEACON_EVENT.toString());
        intent.putExtra(EXTRA_BEACON_EVENT, beaconEvent);
        context.startService(intent);
    }

    public static void startFireIdentifyVisitorAction(Context context, VisitorIdentifyEvent event) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(ActionType.FIRE_IDENTIFY_VISITOR_EVENT.toString());
        intent.putExtra(EXTRA_IDENTIFY_VISITOR_EVENT, event);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "action: " + intent.getAction());
        if (action.equals(ActionType.INIT.toString())) {
            execActionInit(this);
        } else if (action.equals(ActionType.FIRE_BEACON_EVENT.toString())) {
            execActionFireBeaconEvent(intent);
        } else if (action.equals(ActionType.FIRE_IDENTIFY_VISITOR_EVENT.toString())) {
            execActionFireIdentifyUserEvent(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
        return Service.START_NOT_STICKY;
    }

    private void execActionFireBeaconEvent(Intent intent) {
        BeaconEvent event = intent.getParcelableExtra(EXTRA_BEACON_EVENT);
        createFirebaseEvent(beaconEventsRef, event);
    }

    private void execActionFireIdentifyUserEvent(Intent intent) {
        VisitorIdentifyEvent event = intent.getParcelableExtra(EXTRA_IDENTIFY_VISITOR_EVENT);
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

    private void execActionInit(final Context context) {
        PreferencesManager preferencesManager = PreferencesManager.getInstance(this);

        this.visitor = new Visitor(context);

        Firebase.setAndroidContext(this);
        this.rootRef = new Firebase(preferencesManager.getFirebaseRootUrl());
        this.beaconEventsRef = new Firebase(preferencesManager.getFirebaseBeaconEventsUrl());
        this.visitorEventsRef = new Firebase(preferencesManager.getFirebaseVisitorEventsUrl());
        this.engagementEventsRef = new Firebase(preferencesManager.getFirebaseEngagementEventsUrl());

        this.rootRef.authWithCustomToken(preferencesManager.getFirebaseToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "firebase authenticated: " + authData);

                Intent intent = new Intent(DataStoreService.BROADCAST_EVENT);
                intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_SUCCESS.ordinal());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "firebase authentication error: " + firebaseError);

                Intent intent = new Intent(DataStoreService.BROADCAST_EVENT);
                intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_FAIL.ordinal());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
