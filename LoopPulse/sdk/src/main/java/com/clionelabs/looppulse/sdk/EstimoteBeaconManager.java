/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.looppulse.sdk;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;

/**
 ********************************************
 **     THIS CLASS IS CURRENT NOT WORKING  **
 ********************************************
 *
 * Created by simon on 4/7/14.
 */
public class EstimoteBeaconManager extends AbstractBeaconManager implements BeaconManager.RangingListener, BeaconManager.MonitoringListener {

    public static final String TAG = "LoopPulse";

    private final Application application;
    private final Context context;
    private final DataStore dataStore;
    private BeaconManager beaconManager;

    public interface EventsListener {
        public void logEnterEstimoteRegion(Region region);
        public void logExitEstimoteRegion(Region region);
        public void logRangeEstimoteBeaconInRegion(Beacon beacon, Region region);
    }

    public EstimoteBeaconManager(Application application, DataStore dataStore) {
        this.application = application;
        this.context = application.getApplicationContext();
        this.dataStore = dataStore;
    }

    private List<Region> beaconRegions;

    public List<Region> getBeaconRegions() {
        if (beaconRegions == null) {
            beaconRegions = new ArrayList<Region>();

            Region region1 = new Region("LoopPulse-Generic", "B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null);
            Region region2 = new Region("LoopPulse-Generic2", "74278BDA-B644-4520-8F0C-720EAF059935", null, null);
            Region region3 = new Region("LoopPulse-Generic3", "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", null, null);

            beaconRegions.add(region1);
            beaconRegions.add(region2);
            beaconRegions.add(region3);
        }
        return beaconRegions;
    }

    @Override
    public void initialize() {
        beaconManager = new BeaconManager(context);
        beaconManager.setRangingListener(this);
        beaconManager.setMonitoringListener(this);
        beaconManager.setBackgroundScanPeriod(5000, 25000);
    }

    @Override
    public void startLocationMonitoring() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    for (Region region : getBeaconRegions()) {
                        beaconManager.startMonitoring(region);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
    }

//    @Override
    public void startLocationMonitoringAndRanging() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    for (Region region : getBeaconRegions()) {
                        beaconManager.startMonitoring(region);
                        beaconManager.startRanging(region);
                    }

                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start monitoring or ranging", e);
                }
            }
        });
    }

//    @Override
    public void stopLocationMonitoringAndRanging() {
        beaconManager.disconnect();
    }

    // BeaconManager.MonitoringListener

    @Override
    public void onEnteredRegion(Region region, List<Beacon> beacons) {
        Log.d(TAG, "onEnteredRegion: " + beacons);
//        dataStore.logEnterEstimoteRegion(region);
    }

    @Override
    public void onExitedRegion(Region region) {
        Log.d(TAG, "onExitedRegion: " + region);
//        dataStore.logExitEstimoteRegion(region);
    }

    // Implements BeaconManager.RangingListener

    @Override
    public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
        Log.d(TAG, "onBeaconsDiscovered: " + beacons);
        for (Beacon beacon : beacons) {
//            dataStore.logRangeEstimoteBeaconInRegion(beacon, region);
        }
    }
}
