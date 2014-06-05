/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.lighthouse;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clieonelabs.looppulse.LoopPulse;

public class LighHouseApplication extends Application {

    private static final String TAG = "LightHouse";
    private LoopPulse loopPulse;

    public void onCreate() {
        super.onCreate();

        loopPulse = new LoopPulse(this, "testing", "light_house_android");
        loopPulse.startLocationMonitoringAndRanging();

        for (String event: loopPulse.getAvailableEvents()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                    new IntentFilter(event));
        }

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Got message: " + intent.getAction());
        }
    };


}
