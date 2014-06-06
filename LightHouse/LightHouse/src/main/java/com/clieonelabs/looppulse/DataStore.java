/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.Region;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by simon on 6/6/14.
 */
public class DataStore {

    private final Context context;
    private final String clientID;
    private final Firebase firebase;
    private Firebase beaconEventsRef;
    private Visitor latestVisitor;

    public DataStore(Context context, String clientID) {
        this.context = context;
        this.clientID = clientID;
        this.firebase = new Firebase("https://looppulse-dev.firebaseio.com/clients/" + clientID);
        this.beaconEventsRef = this.firebase.child("beacon_events");
    }

    public void registerVisitor(Visitor visitor) {
        identifyVisitor(visitor);
    }

    public void identifyVisitor(Visitor visitor) {
        final Firebase visitorsRef = firebase.child("visitors");
        final Firebase visitorRef = visitorsRef.child(visitor.getUUID());
        latestVisitor = visitor;

        visitorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> newVisitorInfo = new HashMap<String, Object>();
                    newVisitorInfo.put("created_at", new Date().toString());
                    newVisitorInfo.put("device_brand", latestVisitor.getDeviceBrand());
                    newVisitorInfo.put("device_model", latestVisitor.getDeviceModel());
                    newVisitorInfo.put("os_type", "android");
                    newVisitorInfo.put("os_version", latestVisitor.getOSVersion());
                    visitorRef.setValue(newVisitorInfo, ServerValue.TIMESTAMP);
                }

                if (latestVisitor.hasExternalID()) {
                    HashMap<String, Object> visitorInfo = new HashMap<String, Object>();
                    visitorInfo.put("external_id", latestVisitor.getExternalID());
                    visitorRef.updateChildren(visitorInfo);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("LoopPulse", "FirebaseError " + firebaseError.toString());
            }
        });

    }

    public void logEnterRegion(Region region) {
        Date createdAt = new Date();
        Intent intent = new Intent(LoopPulse.EVENT_DID_ENTER_REGION);
        intent.putExtra("created_at", createdAt.toString());
        intent.putExtra("major", region.getMajor());
        intent.putExtra("minor", region.getMinor());
        intent.putExtra("uuid", region.getProximityUuid());
        intent.putExtra("type", "didEnterRegion");
        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        createBeaconEvent(intent, createdAt);
    }

    public void logExitRegion(Region region) {
        Date createdAt = new Date();
        Intent intent = new Intent(LoopPulse.EVENT_DID_EXIT_REGION);
        intent.putExtra("created_at", createdAt.toString());
        intent.putExtra("major", region.getMajor());
        intent.putExtra("minor", region.getMinor());
        intent.putExtra("uuid", region.getProximityUuid());
        intent.putExtra("type", "didExitRegion");
        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        createBeaconEvent(intent, createdAt);
    }

    public void logRangeBeaconInRegion(IBeacon beacon, Region region) {
        Date createdAt = new Date();
        Intent intent = new Intent(LoopPulse.EVENT_DID_RANGE_BEACONS);
        intent.putExtra("accuracy", beacon.getAccuracy());
        intent.putExtra("created_at", createdAt);
        intent.putExtra("major", beacon.getMajor());
        intent.putExtra("minor", beacon.getMinor());
        intent.putExtra("proximity", beacon.getProximity());
        intent.putExtra("rssi", beacon.getRssi());
        intent.putExtra("uuid", beacon.getProximityUuid());
        intent.putExtra("type", "didRangeBeacons");
        intent.putExtra("visitor_uuid", latestVisitor.getUUID());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        createBeaconEvent(intent, createdAt);
    }

    protected void createBeaconEvent(Intent intent, Date createdAt) {
        Firebase eventRef = beaconEventsRef.push();
        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        for (String key : intent.getExtras().keySet()) {
            Object value = intent.getExtras().get(key);
            if (value != null) {
                eventInfo.put(key, value);
            }
        }
        eventRef.setValue(eventInfo, createdAt.getTime());
    }

}
