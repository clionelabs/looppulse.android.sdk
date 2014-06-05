/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.lighthouse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.EditText;
import android.widget.ListView;

import com.clieonelabs.looppulse.LoopPulse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by simon on 5/6/14.
 */
public class LightHouseActivity extends Activity {

    protected static final String TAG = "LightHouse";
    private ListView listView;
    private ArrayList<BeaconEvent> events = new ArrayList<BeaconEvent>();
    private LightHouseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lighthouse_activity);
        
        listView = (ListView) findViewById(R.id.listView);
        adapter = new LightHouseAdapter(this, 0);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(LoopPulse.EVENT_DID_RANGE_BEACONS));
    }

    @Override
    protected void onPause() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BeaconEvent recentEvent = findRecentBeaconEvent(intent);
            if (recentEvent != null) {
                updateEvent(recentEvent);
            }
            else {
                createEvent(intent);
            }
        }
    };

    private void createEvent(Intent intent) {
        BeaconEvent event = new BeaconEvent();
        event.setUUID(intent.getStringExtra("uuid"));
        event.setMajor(intent.getIntExtra("major", -1));
        event.setMinor(intent.getIntExtra("minor", -1));
        event.setCreatedAt(new Date());
        event.setLastSeenAt(new Date());
        events.add(0, event);
        adapter.clear();
        adapter.addAll(events);
    }

    private void updateEvent(BeaconEvent event) {
        event.setLastSeenAt(new Date());
        adapter.clear();
        adapter.addAll(events);
    }

    private BeaconEvent findRecentBeaconEvent(Intent intent) {
        String uuid = intent.getStringExtra("uuid");
        int major = intent.getIntExtra("major", -1);
        int minor = intent.getIntExtra("minor", -1);
        for (BeaconEvent event : events) {
            if (uuid.equals(event.getUUID()) && major == event.getMajor() && minor == event.getMinor()) {
                if (new Date().getTime() - event.getLastSeenAt().getTime() <= 5000) {
                    return event;
                }
                return null;
            }
        }
        return null;
    }

}