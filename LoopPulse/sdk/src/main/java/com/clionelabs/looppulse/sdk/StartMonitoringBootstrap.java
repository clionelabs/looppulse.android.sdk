package com.clionelabs.looppulse.sdk;

import android.util.Log;

import com.clionelabs.looppulse.sdk.datastore.DataStoreHelper;
import com.clionelabs.looppulse.sdk.datastore.FirebaseAuthenticationListener;
import com.clionelabs.looppulse.sdk.monitor.ConnectListener;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelper;

/**
 * Created by hiukim on 2014-10-16.
 */
public class StartMonitoringBootstrap {
    private static String TAG = StartMonitoringBootstrap.class.getCanonicalName();

    private enum Initialization {
        DATASTORE, MONITOR;
        public int value() {
            return 1 << ordinal();
        }
    }
    private final Object initializationLock = new Object();
    private int initializationsMask;

    private DataStoreHelper dataStoreHelper;
    private MonitorHelper monitorHelper;
    private StartMonitoringBootstrapListener listener;

    public StartMonitoringBootstrap(DataStoreHelper dataStoreHelper, MonitorHelper monitorHelper, StartMonitoringBootstrapListener listener) {
        this.dataStoreHelper = dataStoreHelper;
        this.monitorHelper = monitorHelper;
        this.listener = listener;
    }

    public void start() {
        dataStoreHelper.authenticateFirebase(new FirebaseAuthenticationListener() {
            @Override
            public void onAuthenticated(boolean isSuccess) {
                Log.d(TAG, "dataStore onAuthenticated: " + isSuccess);
                initialized(Initialization.DATASTORE);
            }
        });
        monitorHelper.connect(new ConnectListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "monitor onConnected()");
                initialized(Initialization.MONITOR);
            }
        });
    }

    private void initialized(Initialization flag) {
        synchronized (initializationLock) {
            initializationsMask |= flag.value();
        }
        if (isInitialized()) {
            listener.onReady();
        }
    }

    private boolean isInitialized() {
        synchronized (initializationLock) {
            return (initializationsMask == (1 << Initialization.values().length) - 1);
        }
    }
}
