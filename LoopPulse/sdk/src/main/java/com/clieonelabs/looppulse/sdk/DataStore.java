/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse.sdk;

import android.content.Context;
import android.util.Log;

import com.clieonelabs.looppulse.sdk.model.BeaconEvent;
import com.clieonelabs.looppulse.sdk.model.FirebaseEvent;
import com.clieonelabs.looppulse.sdk.model.VisitorEvent;
import com.estimote.sdk.Beacon;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Date;
import java.util.Map;

/**
 * Created by hiukim on 2014-10-03.
 */
public class DataStore { //implements EstimoteBeaconManager.EventsListener {
    private static String TAG = DataStore.class.getCanonicalName();

    private Firebase rootRef;
    private Firebase beaconEventsRef;
    private Firebase visitorEventsRef;
    private Firebase engagementEventsRef;
    private final Context context;
    private Visitor visitor;

    public DataStore(Context context, Visitor visitor, String firebaseToken, Map<String, String> firebaseURLs, final DataStoreResultHandler resultHandler) {
        this.context = context;
        this.visitor = visitor;

        Firebase.setAndroidContext(this.context);
        this.rootRef = new Firebase(firebaseURLs.get("root"));
        this.beaconEventsRef = new Firebase(firebaseURLs.get("beacon_events"));
        this.visitorEventsRef = new Firebase(firebaseURLs.get("visitor_events"));
        this.engagementEventsRef = new Firebase(firebaseURLs.get("engagement_events"));

        this.rootRef.authWithCustomToken(firebaseToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "firebase authenticated: " + authData);
//                createTestBeaconEvent();
                resultHandler.onAuthenticated();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, "firebase authentication error: " + firebaseError);
                resultHandler.onAuthenticationError();
            }
        });
    }

    public void createBeaconEvent(Beacon beacon, BeaconEvent.EventType type) {
        BeaconEvent beaconEvent = new BeaconEvent(beacon, type, visitor.getUUID(), new Date());
        createFirebaseEvent(beaconEventsRef, beaconEvent);
    }

    public void identifyVisitorWithExternalId(String externalId) {
        VisitorEvent visitorEvent = new VisitorEvent(VisitorEvent.EventType.IDENTIFY, visitor.getUUID(), externalId, new Date());
        createFirebaseEvent(visitorEventsRef, visitorEvent);
    }

    private void createFirebaseEvent(Firebase firebaseRef, FirebaseEvent event) {
        firebaseRef.push().setValue(event.toFirebaseObject(), new Firebase.CompletionListener() {
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

//    public void registerVisitor(Visitor visitor) {
//        identifyVisitor(visitor);
//    }
//
//    public void identifyVisitor(Visitor visitor) {
//        final Firebase visitorsRef = firebase.child("visitors");
//        final Firebase visitorRef = visitorsRef.child(visitor.getUUID());
//        latestVisitor = visitor;
//
//        visitorRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() == null) {
//                    HashMap<String, Object> newVisitorInfo = new HashMap<String, Object>();
//                    newVisitorInfo.put("created_at", new Date().toString());
//                    newVisitorInfo.put("device_brand", latestVisitor.getDeviceBrand());
//                    newVisitorInfo.put("device_model", latestVisitor.getDeviceModel());
//                    newVisitorInfo.put("os_type", "android");
//                    newVisitorInfo.put("os_version", latestVisitor.getOSVersion());
//                    visitorRef.setValue(newVisitorInfo, ServerValue.TIMESTAMP);
//                }
//
//                if (latestVisitor.hasExternalID()) {
//                    HashMap<String, Object> visitorInfo = new HashMap<String, Object>();
//                    visitorInfo.put("external_id", latestVisitor.getExternalID());
//                    visitorRef.updateChildren(visitorInfo);
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Log.d("LoopPulse", "FirebaseError " + firebaseError.toString());
//            }
//        });
//    }
//
//    // Implements EstimoteBeaconManager.EventsListener
//    public void logEnterEstimoteRegion(com.estimote.sdk.Region region)
//    {
//        Date createdAt = new Date();
//        Intent intent = new Intent(LoopPulse.EVENT_DID_ENTER_REGION);
//        intent.putExtra("created_at", createdAt.toString());
//        intent.putExtra("major", region.getMajor());
//        intent.putExtra("minor", region.getMinor());
//        intent.putExtra("uuid", region.getProximityUUID());
//        intent.putExtra("type", "didEnterRegion");
//        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
////        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//        createBeaconEvent(intent, createdAt);
//    }
//
//    public void logExitEstimoteRegion(com.estimote.sdk.Region region)
//    {
//        Date createdAt = new Date();
//        Intent intent = new Intent(LoopPulse.EVENT_DID_EXIT_REGION);
//        intent.putExtra("created_at", createdAt.toString());
//        intent.putExtra("major", region.getMajor());
//        intent.putExtra("minor", region.getMinor());
//        intent.putExtra("uuid", region.getProximityUUID());
//        intent.putExtra("type", "didExitRegion");
//        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
////        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//        createBeaconEvent(intent, createdAt);
//
//    }
//
//    public void logRangeEstimoteBeaconInRegion(Beacon beacon, com.estimote.sdk.Region region)
//    {
//        Date createdAt = new Date();
//        Intent intent = new Intent(LoopPulse.EVENT_DID_RANGE_BEACONS);
//        //intent.putExtra("accuracy");
//        intent.putExtra("created_at", createdAt);
//        intent.putExtra("major", beacon.getMajor());
//        intent.putExtra("minor", beacon.getMinor());
//        intent.putExtra("proximity", beacon.getMeasuredPower());
//        intent.putExtra("rssi", beacon.getRssi());
//        intent.putExtra("uuid", beacon.getProximityUUID());
//        intent.putExtra("type", "didRangeBeacons");
//        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
////        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//
//        createBeaconEvent(intent, createdAt);
//    }
//
//
//    // Helper methods
//
//    protected void createBeaconEvent(Intent intent, Date createdAt) {
//        Firebase eventRef = beaconEventsRef.push();
//        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
//        for (String key : intent.getExtras().keySet()) {
//            Object value = intent.getExtras().get(key);
//            if (value != null) {
//                eventInfo.put(key, value);
//            }
//        }
//        eventRef.setValue(eventInfo, createdAt.getTime());
//    }

}
