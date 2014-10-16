package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;

/**
 * Created by hiukim on 2014-10-16.
 */
public class LoopPulseServiceBroadcaster {

    public static final String INTENT_NAME = "com.clionelabs.looppulse.sdk.services.LoopPulseServiceBroadcaster.INTENT_NAME";
    public static final String EXTRA_BROADCAST_EVENT = "com.clionelabs.looppulse.sdk.services.LoopPulseServiceBroadcaster.EXTRA_BROADCAST_EVENT";
    public static final String AUTHENTICATED = "EXTRA_AUTHENTICATED";
    public static final String AUTHENTICATION_ERROR = "EXTRA_AUTHENTICATION_ERROR";
    public static final String MONITORING_STARTED = "EXTRA_MONITORING_STARTED";
    public static final String MONITORING_STOPPED = "EXTRA_MONITORING_STOPPED";
    public static final String BEACON_DETECTED = "EXTRA_BEACON_DETECTED";
    public static final String EXTRA_MSG = "com.clionelabs.looppulse.sdk.services.LoopPulseServiceBroadcaster.EXTRA_MSG";
    public static final String EXTRA_BEACON_EVENT = "com.clionelabs.looppulse.sdk.services.LoopPulseServiceBroadcaster.EXTRA_BEACON_EVENT";

    public static void sendAuthAuthenticated(Context context) {
        Intent intent = new Intent(LoopPulseServiceBroadcaster.INTENT_NAME);
        intent.putExtra(EXTRA_BROADCAST_EVENT, AUTHENTICATED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendAuthAuthenticationError(Context context, String msg) {
        Intent intent = new Intent(LoopPulseServiceBroadcaster.INTENT_NAME);
        intent.putExtra(EXTRA_BROADCAST_EVENT, AUTHENTICATION_ERROR);
        intent.putExtra(EXTRA_MSG, msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendMonitoringStarted(Context context) {
        Intent intent = new Intent(LoopPulseServiceBroadcaster.INTENT_NAME);
        intent.putExtra(EXTRA_BROADCAST_EVENT, MONITORING_STARTED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendMonitoringStopped(Context context) {
        Intent intent = new Intent(LoopPulseServiceBroadcaster.INTENT_NAME);
        intent.putExtra(EXTRA_BROADCAST_EVENT, MONITORING_STOPPED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendBeaconEvent(Context context, BeaconEvent event) {
        Intent intent = new Intent(LoopPulseServiceBroadcaster.INTENT_NAME);
        intent.putExtra(EXTRA_BROADCAST_EVENT, BEACON_DETECTED);
        intent.putExtra(EXTRA_BEACON_EVENT, event);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
