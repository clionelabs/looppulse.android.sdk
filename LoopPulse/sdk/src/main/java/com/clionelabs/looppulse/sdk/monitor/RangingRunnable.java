package com.clionelabs.looppulse.sdk.monitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hiukim on 2014-10-08.
 *
 * This Runnable will execute the ranging action continuously for a defined period of time (rangePeriodSec)
 */
public class RangingRunnable implements Runnable {
    private static String TAG = RangingRunnable.class.getCanonicalName();
    public static String MSG_TYPE = "MSG_TYPE";
    public static String MSG_FINISH = "FINISH";
    public static String MSG_RANGE = "RANGE";
    public static String BEACONS_LIST = "BEACONS_LIST";

    private Region region; // Need multiple?
    private int rangePeriodSec;
    private BeaconManager beaconManager;
    private Handler handler;

    public RangingRunnable (BeaconManager beaconManager, Region region, int rangePeriodSec, Handler handler) {
        this.beaconManager = beaconManager;
        this.region = region;
        this.rangePeriodSec = rangePeriodSec;
        this.handler = handler;
    }

    public void run() {
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "onBeaconsDiscovered: " + beacons);
                // TODO: filter out LoopPulse beacons first
                onDiscovered(beacons);
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
                    onFinished();
                } catch (Exception e) {
                    Log.e(TAG, "Cannot stop ranging", e);
                }
            }
        }, rangePeriodSec * 1000);
    }

    private void onDiscovered(List<Beacon> beacons) {
        Message msgObj = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_TYPE, MSG_RANGE);
        bundle.putParcelableArrayList(BEACONS_LIST, new ArrayList<Beacon>(beacons));
        msgObj.setData(bundle);
        handler.sendMessage(msgObj);
    }

    private void onFinished() {
        Message msgObj = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_TYPE, MSG_FINISH);
        msgObj.setData(bundle);
        handler.sendMessage(msgObj);
    }
}