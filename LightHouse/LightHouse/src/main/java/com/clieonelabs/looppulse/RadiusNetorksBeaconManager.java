/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

import java.util.Collection;

/**
 * Created by simon on 6/6/14.
 */
public class RadiusNetorksBeaconManager extends BeaconManager implements BootstrapNotifier, RangeNotifier, IBeaconConsumer {

    public static final String TAG = "LoopPuslse";
    private final Application application;
    private final Context context;
    private final DataStore dataStore;

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private IBeaconManager beaconManager;
    private boolean shouldStartMonitoring;
    private boolean shouldStartRanging;

    public RadiusNetorksBeaconManager(Application application, DataStore dataStore) {
        this.application = application;
        this.context = application.getApplicationContext();
        this.dataStore = dataStore;
    }

    @Override
    public void applicationDidLaunch() {
        IBeaconManager.LOG_DEBUG = true;

        beaconManager = com.radiusnetworks.proximity.ibeacon.IBeaconManager.getInstanceForApplication(context);
        beaconManager.setBackgroundBetweenScanPeriod(300000);

        Region region = new Region("LoopPulseBootstrapRegion", "B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the iBeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(application);

    }

    @Override
    public void startLocationMonitoring() {
        shouldStartMonitoring = true;
        beaconManager.bind(this);
    }

    @Override
    public void stopLocationMonitoringAndRanging() {
        beaconManager.unBind(this);
    }

    @Override
    public void startLocationMonitoringAndRanging() {
        shouldStartRanging = true;
        beaconManager.bind(this);
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
        try {
            beaconManager.setRangeNotifier(this);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "did exit region");
        this.dataStore.logExitRegion(region);
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
        if (shouldStartMonitoring) {
            beaconManager.setMonitorNotifier(this);
            try {
                beaconManager.startMonitoringBeaconsInRegion(new Region("LoopPulseMonitoringRegion", "B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            shouldStartMonitoring = false;
        }

        if (shouldStartRanging) {
            beaconManager.setRangeNotifier(this);
            try {
                beaconManager.startRangingBeaconsInRegion(new Region("LoopPulseRangingRegion", null, null, null));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            shouldStartRanging = false;
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
