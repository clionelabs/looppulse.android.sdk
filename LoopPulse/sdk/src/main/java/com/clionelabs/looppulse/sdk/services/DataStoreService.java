package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.clionelabs.looppulse.sdk.model.VisitorIdentifyEvent;

public class DataStoreService extends Service implements DataStoreServiceHelperListener {
    private static String TAG = DataStoreService.class.getCanonicalName();

    public static final String BROADCAST_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.Event";
    public static final String BROADCAST_EVENT_TYPE = "com.clionelabs.looppulse.sdk.services.DataStoreService.EventType";
    public static final String EXTRA_BEACON_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.EXTRA_BEACON_EVENT";
    public static final String EXTRA_IDENTIFY_VISITOR_EVENT = "com.clionelabs.looppulse.sdk.services.DataStoreService.EXTRA_IDENTIFY_VISITOR_EVENT";
    public enum EventType {INIT_SUCCESS, INIT_FAIL};
    public enum ActionType {INIT, FIRE_BEACON_EVENT, FIRE_IDENTIFY_VISITOR_EVENT};

    private DataStoreServiceHelper helper;

    public DataStoreService() {
    }

    public static void startAction(Context context, ActionType action) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(action.toString());
        context.startService(intent);
    }

    public static void startFireBeaconAction(Context context, BeaconEvent beaconEvent) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(ActionType.FIRE_BEACON_EVENT.toString());
        intent.putExtra(EXTRA_BEACON_EVENT, beaconEvent);
        context.startService(intent);
    }

    public static void startFireIdentifyVisitorAction(Context context, VisitorIdentifyEvent event) {
        Intent intent = new Intent(context, DataStoreService.class);
        intent.setAction(ActionType.FIRE_IDENTIFY_VISITOR_EVENT.toString());
        intent.putExtra(EXTRA_IDENTIFY_VISITOR_EVENT, event);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "action: " + intent.getAction());
        if (action.equals(ActionType.INIT.toString())) {
            execActionInit();
        } else if (action.equals(ActionType.FIRE_BEACON_EVENT.toString())) {
            execActionFireBeaconEvent(intent);
        } else if (action.equals(ActionType.FIRE_IDENTIFY_VISITOR_EVENT.toString())) {
            execActionFireIdentifyUserEvent(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
        return Service.START_NOT_STICKY;
    }

    private void execActionInit() {
        helper = new DataStoreServiceHelper(this, this);
    }

    private void execActionFireBeaconEvent(Intent intent) {
        if (helper == null) {
            throw new RuntimeException("DataStoreService has not been initialized yet.");
        }
        BeaconEvent event = intent.getParcelableExtra(EXTRA_BEACON_EVENT);
        helper.createFirebaseBeaconEvent(event);
    }

    private void execActionFireIdentifyUserEvent(Intent intent) {
        if (helper == null) {
            throw new RuntimeException("DataStoreService has not been initialized yet.");
        }
        VisitorIdentifyEvent event = intent.getParcelableExtra(EXTRA_IDENTIFY_VISITOR_EVENT);
        helper.createFirebaseVisitorIdentifyEvent(event);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Implements DataStoreServiceHelperListener
    @Override
    public void onFinishedInit(boolean isSuccess) {
        if (isSuccess) {
            Intent intent = new Intent(DataStoreService.BROADCAST_EVENT);
            intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_SUCCESS.ordinal());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Intent intent = new Intent(DataStoreService.BROADCAST_EVENT);
            intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_FAIL.ordinal());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        Intent intent = new Intent(RangingService.BROADCAST_EVENT);
        intent.putExtra(BROADCAST_EVENT_TYPE, EventType.INIT_SUCCESS.ordinal());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
