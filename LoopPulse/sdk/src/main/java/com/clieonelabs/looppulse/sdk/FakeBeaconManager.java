package com.clieonelabs.looppulse.sdk;

import android.content.Context;
import android.util.Log;

import com.clieonelabs.looppulse.sdk.model.BeaconEvent;
import com.estimote.sdk.Beacon;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hiukim on 2014-10-03.
 */
public class FakeBeaconManager extends AbstractBeaconManager {
    public static final String TAG = FakeBeaconManager.class.getCanonicalName();

    private final Context context;
    private final DataStore dataStore;
    private Timer timer = new Timer(true);

    public FakeBeaconManager(Context context, DataStore dataStore) {
        super();
        this.context = context;
        this.dataStore = dataStore;
    }

    @Override
    public void initialize() {
        super.initialize();
        Log.d(TAG, "initialize()");
    }

    @Override
    public void startLocationMonitoring() {
        super.startLocationMonitoring();
        Log.d(TAG, "startLocationMonitoring()");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Beacon beacon = new Beacon("A2DCA1E4-0607-4F37-9FF1-825237B278FE", "name", "macAddress", 1, 2, 3, 4);
                dataStore.createBeaconEvent(beacon, BeaconEvent.EventType.ENTER);
            }
        }, 0, 2 * 1000);
    }

    @Override
    public void stopLocationMonitoring() {
        super.stopLocationMonitoring();
        Log.d(TAG, "stopLocationMonitoring()");

        timer.cancel();
    }
}
