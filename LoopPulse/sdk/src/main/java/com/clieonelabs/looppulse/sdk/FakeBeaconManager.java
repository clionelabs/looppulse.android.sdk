package com.clieonelabs.looppulse.sdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by hiukim on 2014-10-03.
 */
public class FakeBeaconManager extends AbstractBeaconManager {
    public static final String TAG = FakeBeaconManager.class.getCanonicalName();

    private final Application application;
    private final Context context;
    private final DataStore dataStore;

    public FakeBeaconManager(Application application, DataStore dataStore) {
        this.application = application;
        this.context = application.getApplicationContext();
        this.dataStore = dataStore;
    }

    @Override
    public void applicationDidLaunch() {
        Log.d(TAG, "applicationDidLaunch()");
    }

    @Override
    public void startLocationMonitoring() {
        Log.d(TAG, "startLocationMonitoring()");
    }

    @Override
    public void startLocationMonitoringAndRanging() {
        Log.d(TAG, "startLocationMonitoringAndRanging()");
    }

    @Override
    public void stopLocationMonitoringAndRanging() {
        Log.d(TAG, "stopLocationMonitoringAndRanging()");
    }
}
