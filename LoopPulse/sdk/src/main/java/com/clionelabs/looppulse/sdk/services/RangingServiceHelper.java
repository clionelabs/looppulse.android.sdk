package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.util.Log;

import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.clionelabs.looppulse.sdk.receivers.RangingAlarmReceiver;
import com.clionelabs.looppulse.sdk.receivers.RangingRunnable;
import com.clionelabs.looppulse.sdk.receivers.RangingRunnableListener;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.Date;

/**
 * Created by hiukim on 2014-10-14.
 */
public class RangingServiceHelper {
    private static String TAG = RangingService.class.getCanonicalName();

    private static int RANGE_PERIOD_SEC = 5;
    private static int MAX_RESCHEDULE_SEC = 1800; // 30 mins

    private Context context;
    private BeaconManager beaconManager;
    private Region defaultRegion;
    private final Object isRangingLock = new Object();
    private boolean isRanging;
    private RangingStatus rangingStatus;
    private RangingServiceHelperListener listener;

    public RangingServiceHelper(Context context, Region region, BeaconManager beaconManager, RangingStatus rangingStatus, RangingServiceHelperListener listener) {
        this.context = context;
        this.defaultRegion = region;
        this.beaconManager = beaconManager;
        this.listener = listener;
        this.rangingStatus = rangingStatus;

        init();
    }

    public void executeActionRange() {
        if (!getIsRanging()) {
            setIsRanging(true);
            doRanging();
        } else {
            Log.d(TAG, "Already Ranging");
        }
    }

    /**
     * Do ranging for RANGE_PERIOD_SEC seconds, and get a set of currently detectedBeacons
     * Check the current detectedBeacons against the beaconsWithinSet
     * and decide whether an enter/exit events have occurred.
     */
    private void doRanging() {
        new Thread(new RangingRunnable(context, beaconManager, defaultRegion, RANGE_PERIOD_SEC, rangingStatus, new RangingRunnableListener() {
            @Override
            public void onFinishedRanging() {
                for (Beacon beacon: rangingStatus.getEnteredBeacons()) {
                    Log.d(TAG, "EnterRegion: " + beacon);
                    BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, new Date());
                    DataStoreService.startFireBeaconAction(context, beaconEvent);
                }
                for (Beacon beacon: rangingStatus.getExcitedBeacons()) {
                    Log.d(TAG, "ExitRegion: " + beacon);
                    BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.EXIT, new Date());
                    DataStoreService.startFireBeaconAction(context, beaconEvent);
                }
                setIsRanging(false);
                scheduleNextRanging();
                listener.onFinishedRanging();
            }
        })
        ).start();
    }

    private void scheduleNextRanging() {
        long inactiveSec = (new Date().getTime() - rangingStatus.getLastActiveTime().getTime()) / 1000;
        int nextScheduleSec = (int) Math.min(inactiveSec / 2, MAX_RESCHEDULE_SEC);
        Log.d(TAG, "next range in " + nextScheduleSec + " seconds");
        RangingAlarmReceiver.setAlarm(context, nextScheduleSec);
    }

    private void init() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                listener.onFinishedInit();
            }
        });
    }

    private boolean getIsRanging() {
        synchronized (isRangingLock) {
            return isRanging;
        }
    }

    private void setIsRanging(boolean isRanging) {
        synchronized (isRangingLock) {
            this.isRanging = isRanging;
        }
    }
}
