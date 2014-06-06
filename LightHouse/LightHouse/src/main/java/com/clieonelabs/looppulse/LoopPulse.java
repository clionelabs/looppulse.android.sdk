/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse;

import android.app.Application;
import android.content.Context;

public class LoopPulse {

    public static final String EVENT_DID_ENTER_REGION = "LP_EVENT_DID_ENTER_REGION";
    public static final String EVENT_DID_EXIT_REGION = "LP_EVENT_EXIT_REGION";
    public static final String EVENT_DID_RANGE_BEACONS = "LP_EVENT_DID_RANGE_BEACONS";

    public static final String TAG = "LoopPuslse";

    private Application application;
    private Context context;
    private String token;
    private String clientID;
    private DataStore dataStore;
    private Visitor visitor;
    private BeaconManager beaconManager;

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
        this.beaconManager = new RadiusNetorksBeaconManager(application, dataStore);

        this.dataStore.registerVisitor(visitor);
        this.beaconManager.applicationDidLaunch();
    }

    public String[] getAvailableEvents() {
        return new String[] { EVENT_DID_ENTER_REGION, EVENT_DID_EXIT_REGION, EVENT_DID_RANGE_BEACONS };
    }

    public void startLocationMonitoring() {
        beaconManager.startLocationMonitoring();
    }

    public void stopLocationMonitoringAndRanging() {
        beaconManager.stopLocationMonitoringAndRanging();
    }

    public void startLocationMonitoringAndRanging() {  // debug
        beaconManager.startLocationMonitoringAndRanging();
    }

}
