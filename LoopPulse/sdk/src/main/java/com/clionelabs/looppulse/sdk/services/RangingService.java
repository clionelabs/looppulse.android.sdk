package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clionelabs.looppulse.sdk.receivers.RangingAlarmReceiver;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

public class RangingService extends Service implements RangingServiceHelperListener {
    private static String TAG = RangingService.class.getCanonicalName();

    public static final String BROADCAST_EVENT = "com.clionelabs.looppulse.sdk.services.RangingService.Event";
    public static final String BROADCAST_EVENT_TYPE = "com.clionelabs.looppulse.sdk.services.RangingService.EventType";
    public enum EventType {INIT_SUCCESS, INIT_FAIL};

    public static final String EXTRA_TRIGGER_CLASS = "com.clionelabs.looppulse.sdk.services.RangingService.EXTRA_TRIGGER_CLASS";
    public enum ActionType {INIT, RANGE};

    private RangingServiceHelper helper;

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
            execActionInit();
        } else if (action.equals(ActionType.RANGE.toString())) {
            execActionRange(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
        return Service.START_NOT_STICKY;
    }

    private void execActionInit() {
        Region region = new Region("LoopPulse-Generic", null, null, null);
        BeaconManager beaconManager = new BeaconManager(this);
        RangingStatus rangingStatus = new RangingStatus();
        helper = new RangingServiceHelper(this, region, beaconManager, rangingStatus, this);
    }

    private void execActionRange(Intent intent) {
        if (helper == null) {
            throw new RuntimeException("RangingService has not been initialized yet.");
        }

        helper.executeActionRange();

        String triggerClass = intent.getStringExtra(EXTRA_TRIGGER_CLASS);
        if (triggerClass != null && triggerClass.equals(RangingAlarmReceiver.class.getName())) {
            Log.d(TAG, "releasing RangingAlarmReceiver wake lock");
            RangingAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Implement RangingServiceHelperListener
    @Override
    public void onFinishedInit() {
        Intent intent = new Intent(RangingService.BROADCAST_EVENT);
        intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_SUCCESS.ordinal());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onFinishedRanging() {

    }
}
