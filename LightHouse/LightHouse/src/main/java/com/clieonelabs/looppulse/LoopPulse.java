/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

import java.util.Collection;

public class LoopPulse implements BootstrapNotifier, RangeNotifier, IBeaconConsumer {

    public static final String EVENT_DID_ENTER_REGION = "LP_EVENT_DID_ENTER_REGION";
    public static final String EVENT_DID_EXIT_REGION = "LP_EVENT_EXIT_REGION";
    public static final String EVENT_DID_RANGE_BEACONS = "LP_EVENT_DID_RANGE_BEACONS";

    public static final String TAG = "LoopPuslse";

    private Application application;
    private Context context;
    private String token;
    private String clientID;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private IBeaconManager beaconManager;
    private DataStore dataStore;
    private Visitor visitor;

    public LoopPulse(Application application, String token, String clientID) {
        if (token == null || token.length() == 0) {
            throw new IllegalArgumentException("token argument cannot be empty");
        }
        if (clientID == null || clientID.length() == 0) {
            throw new IllegalArgumentException("clientID argument cannot be empty");
        }
        this.application = application;
        this.context = application.getApplicationContext();
        this.token = token;
        this.clientID = clientID;
        this.dataStore = new DataStore(context, clientID);
        this.visitor = new Visitor(context);

        this.dataStore.registerVisitor(visitor);
    }

    public void startLocationMonitoring() {

    }

    public void stopLocationMonitoringAndRanging() {
    }

    public String[] getAvailableEvents() {
        return new String[] { EVENT_DID_ENTER_REGION, EVENT_DID_EXIT_REGION, EVENT_DID_RANGE_BEACONS };
    }

    public void startLocationMonitoringAndRanging() {  // debug
        IBeaconManager.LOG_DEBUG = true;


        beaconManager = com.radiusnetworks.proximity.ibeacon.IBeaconManager.getInstanceForApplication(context);
        beaconManager.setBackgroundBetweenScanPeriod(5000);

        Region region = new Region("LoopPulseBootstrapRegion", "B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the iBeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(application);

        if (!beaconManager.isBound(this)) {
            beaconManager.bind(this);
        }
    }

    @Override
    public Context getApplicationContext() {
        return context.getApplicationContext();
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // This method is not used in this example
        Log.d(TAG, "didDetermineStateForRegion");
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "did enter region.");
        this.dataStore.logEnterRegion(region);
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "did exit region");
        this.dataStore.logExitRegion(region);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
        Log.d(TAG, "did range region");

        for (IBeacon beacon : iBeacons) {
            this.dataStore.logRangeBeaconInRegion(beacon, region);
        }
    }

    // Implementation of IBeaconConsumer

    public void onIBeaconServiceConnect() {
        beaconManager.setRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("LoopPulseRangingRegion", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return context.bindService(intent, serviceConnection, i);
    }


}
