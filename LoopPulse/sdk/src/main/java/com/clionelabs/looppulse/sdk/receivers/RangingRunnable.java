package com.clionelabs.looppulse.sdk.receivers;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.util.UsefulFunctions;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hiukim on 2014-10-08.
 *
 * This Runnable will execute the ranging action continuously for a defined period of time (set in constructor)
 * and return a set of detected beacons within this period back to the listener.
 */
public class RangingRunnable implements Runnable {
    private static String TAG = RangingRunnable.class.getCanonicalName();
    private Context context;
    private Region region; // Need multiple?
    private RangingRunnableListener listener;
    private int rangePeriodSec;
    private BeaconManager beaconManager;

    public RangingRunnable (Context context, BeaconManager beaconManager, Region region, int rangePeriodSec, RangingRunnableListener listener) {
        this.context = context;
        this.beaconManager = beaconManager;
        this.region = region;
        this.listener = listener;
        this.rangePeriodSec = rangePeriodSec;
    }

    public void run() {
        final HashMap<String, Beacon> currentBeacons = new HashMap<String, Beacon>();

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "onBeaconsDiscovered: " + beacons);
                for (Beacon beacon: beacons) {
                    // TODO: check whether it is a LoopPulse beacon
                    String key = UsefulFunctions.getBeaconKey(beacon);
                    if (!currentBeacons.containsKey(key)) {
                        currentBeacons.put(key, beacon);
                    }
                }
            }
        });

        // beaconManager will continuous ranging every one second until we stop it.
        try {
            beaconManager.startRanging(region);
        } catch (Exception e) {
            Log.e(TAG, "Cannot start ranging", e);
        }

        // we schedule a timer to stop the ranging action after RANGE_PERIOD_SEC seconds.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // check enter event
                    beaconManager.stopRanging(region);
                    listener.onFinishedRanging(currentBeacons);
                } catch (Exception e) {
                    Log.e(TAG, "Cannot stop ranging", e);
                }
            }
        }, rangePeriodSec * 1000);

    }
}
