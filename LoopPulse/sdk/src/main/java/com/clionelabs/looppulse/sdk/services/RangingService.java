package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.clionelabs.looppulse.sdk.receivers.RangingAlarmReceiver;
import com.clionelabs.looppulse.sdk.receivers.RangingRunnable;
import com.clionelabs.looppulse.sdk.receivers.RangingRunnableListener;
import com.clionelabs.looppulse.sdk.util.UsefulFunctions;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RangingService extends Service {
    private static String TAG = RangingService.class.getCanonicalName();

    private static int RANGE_PERIOD_SEC = 5;
    private static int MAX_RESCHEDULE_SEC = 1800; // 30 mins

    private BeaconManager beaconManager;
    private HashMap<String, Beacon> beaconsWithinMap; // beaconKey -> beacon
    private Region defaultRegion;
    private Date lastActiveTime;
    private final Object isRangingLock = new Object();
    private boolean isRanging;

    public static final String BROADCAST_EVENT = "com.clionelabs.looppulse.sdk.services.RangingService.Event";
    public static final String BROADCAST_EVENT_TYPE = "com.clionelabs.looppulse.sdk.services.RangingService.EventType";
    public enum EventType {INIT_SUCCESS, INIT_FAIL};

    public static final String EXTRA_TRIGGER_CLASS = "com.clionelabs.looppulse.sdk.services.RangingService.EXTRA_TRIGGER_CLASS";
    public enum ActionType {INIT, RANGE};

    public RangingService() {
    }

    public static void startAction(Context context, ActionType action) {
        Intent intent = new Intent(context, RangingService.class);
        intent.setAction(action.toString());
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(ActionType.INIT.toString())) {
            execActionInit(this);
        } else if (action.equals(ActionType.RANGE.toString())) {
            execActionRange(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
        return Service.START_NOT_STICKY;
    }

    private void execActionInit(final Context context) {
        beaconsWithinMap = new HashMap<String, Beacon>();
        defaultRegion = new Region("LoopPulse-Generic", null, null, null);
        lastActiveTime = new Date();
        beaconManager = new BeaconManager(context);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Intent intent = new Intent(RangingService.BROADCAST_EVENT);
                intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_SUCCESS.ordinal());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    private void execActionRange(Intent intent) {
        String triggerClass = intent.getStringExtra(EXTRA_TRIGGER_CLASS);

        if (!getIsRanging()) {
            setIsRanging(true);
            doRanging(this);
        } else {
            Log.d(TAG, "Already Ranging");
        }

        if (triggerClass != null && triggerClass.equals(RangingAlarmReceiver.class.getName())) {
            Log.d(TAG, "releasing RangingAlarmReceiver wake lock");
            RangingAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Do ranging for RANGE_PERIOD_SEC seconds, and get a set of currently detectedBeacons
     * Check the current detectedBeacons against the beaconsWithinSet
     * and decide whether an enter/exit events have occurred.
     * @param context
     */
    private void doRanging(final Context context) {
        new Thread(new RangingRunnable(this, beaconManager, defaultRegion, RANGE_PERIOD_SEC, new RangingRunnableListener() {
                @Override
                public void onFinishedRanging(HashMap<String, Beacon> detectedBeacons) {
                    ArrayList<Beacon> enteredBeacons = new ArrayList<Beacon>();
                    ArrayList<Beacon> exitedBeacons = new ArrayList<Beacon>();

                    // check new Enter and new Exit
                    for (String key: detectedBeacons.keySet()) {
                        if (!beaconsWithinMap.containsKey(key)) {
                            enteredBeacons.add(detectedBeacons.get(key));
                        }
                    }
                    for (String key: beaconsWithinMap.keySet()) {
                        if (!detectedBeacons.containsKey(key)) {
                            exitedBeacons.add(beaconsWithinMap.get(key));
                        }
                    }

                    for (Beacon beacon: enteredBeacons) {
                        Log.d(TAG, "EnterRegion: " + beacon);
                        BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, new Date());
                        DataStoreService.startFireBeaconAction(context, beaconEvent);

                        beaconsWithinMap.put(UsefulFunctions.getBeaconKey(beacon), beacon);
                    }
                    for (Beacon beacon: exitedBeacons) {
                        Log.d(TAG, "ExitRegion: " + beacon);
                        BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.EXIT, new Date());
                        DataStoreService.startFireBeaconAction(context, beaconEvent);

                        beaconsWithinMap.remove(UsefulFunctions.getBeaconKey(beacon));
                    }

                    boolean hasActivity = enteredBeacons.size() > 0 || exitedBeacons.size() > 0;
                    if (hasActivity) {
                        lastActiveTime = new Date();
                    }

                    setIsRanging(false);
                    scheduleNextRanging();
                }
            })
        ).start();
    }

    private void scheduleNextRanging() {
        long inactiveSec = (new Date().getTime() - lastActiveTime.getTime()) / 1000;
        int nextScheduleSec = (int) Math.min(inactiveSec / 2, MAX_RESCHEDULE_SEC);
        Log.d(TAG, "next range in " + nextScheduleSec + " seconds");
        RangingAlarmReceiver.setAlarm(this, nextScheduleSec);
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
