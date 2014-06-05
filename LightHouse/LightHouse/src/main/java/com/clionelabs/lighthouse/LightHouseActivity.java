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

import com.clieonelabs.looppulse.LoopPulse;

/**
 * Created by simon on 5/6/14.
 */
public class LightHouseActivity extends Activity {

    protected static final String TAG = "LightHouse";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
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

            EditText editText = (EditText) findViewById(R.id.rangingText);

            String message = String.format("Beacon detected: uuid: %s , accuracy: %.3f",
                    intent.getStringExtra("uuid"),
                    intent.getDoubleExtra("accuracy", -1));

            editText.append(message + "\n");
        }
    };




}