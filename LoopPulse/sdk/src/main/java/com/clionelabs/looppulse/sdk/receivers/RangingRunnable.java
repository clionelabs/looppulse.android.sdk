package com.clionelabs.looppulse.sdk.receivers;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.RangingStatus;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hiukim on 2014-10-08.
 *
 * This Runnable will execute the ranging action continuously for a defined period of time (set in constructor)
 * and the rangingStatus will be updated accordingly
 */
public class RangingRunnable implements Runnable {
    private static String TAG = RangingRunnable.class.getCanonicalName();
    private Context context;
    private Region region; // Need multiple?
    private RangingRunnableListener listener;
    private int rangePeriodSec;
    private RangingStatus rangingStatus;
    private BeaconManager beaconManager;

    public RangingRunnable (Context context, BeaconManager beaconManager, Region region, int rangePeriodSec, RangingStatus rangingStatus, RangingRunnableListener listener) {
        this.context = context;
        this.beaconManager = beaconManager;
        this.region = region;
        this.rangingStatus = rangingStatus;
        this.rangePeriodSec = rangePeriodSec;
        this.listener = listener;
    }

    public void run() {
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "onBeaconsDiscovered: " + beacons);
                // TODO: filter out LoopPulse beacons first
                rangingStatus.receiveRangingBeacons(beacons);
            }
        });

        // beaconManager will continuous ranging every one second until we stop it.
        try {
            beaconManager.startRanging(region);
        } catch (Exception e) {
            Log.e(TAG, "Cannot start ranging", e);
        }

        // we schedule a timer to stop the ranging action after rangePeriodSec seconds.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // check enter event
                    beaconManager.stopRanging(region);
                    listener.onFinishedRanging(); // Important: has to be called before rangingStatus.updateStatus()
                    rangingStatus.updateStatus();
                } catch (Exception e) {
                    Log.e(TAG, "Cannot stop ranging", e);
                }
            }
        }, rangePeriodSec * 1000);

    }
}
