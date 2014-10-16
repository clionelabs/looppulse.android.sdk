package com.clionelabs.looppulse.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.LoopPulseServiceBroadcaster;
import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;

/**
 * Created by hiukim on 2014-10-16.
 *
 * This is the API class of the SDK
 * This class serves two purposes:
 *      1) observe LocalBroadcast from LoopPulseService, and send feedback back to the client app via the listener.
 *      2) accept API calls from client app, and start relevant LoopPulseService actions.
 *
 * Noted that this is supposed to be instance object created from the client app, and will get destroyed anytime. e.g. If the client
 * app created this class in an Activity, then this instance will be destroyed together with the Activity. So anything that
 * require persistence should goes into the LoopPulseService (which is a long-lasting background service).
 */
public class LoopPulse {
    private static String TAG = LoopPulse.class.getCanonicalName();

    private Context context;
    private LoopPulseListener loopPulseListener;
    private BroadcastReceiver loopPulseServiceEventsReceiver;

    public LoopPulse(Context context, LoopPulseListener loopPulseListener) {
        this.context = context;
        this.loopPulseListener = loopPulseListener;
        init();

        // Enable Estimote Debug
        com.estimote.sdk.utils.L.enableDebugLogging(true);
    }

    public void init() {
        loopPulseServiceEventsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String eventType = intent.getStringExtra(LoopPulseServiceBroadcaster.EXTRA_BROADCAST_EVENT);
                Log.d(TAG, "receiving LoopPulseService broadcast message: " + eventType);
                if (eventType.equals(LoopPulseServiceBroadcaster.AUTHENTICATED)) {
                    loopPulseListener.onAuthenticated();
                } else if (eventType.equals(LoopPulseServiceBroadcaster.AUTHENTICATION_ERROR)) {
                    String msg = intent.getStringExtra(LoopPulseServiceBroadcaster.EXTRA_MSG);
                    loopPulseListener.onAuthenticationError(msg);
                } else if (eventType.equals(LoopPulseServiceBroadcaster.MONITORING_STARTED)) {
                    loopPulseListener.onMonitoringStarted();
                } else if (eventType.equals(LoopPulseServiceBroadcaster.MONITORING_STOPPED)) {
                    loopPulseListener.onMonitoringStopped();
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(
                loopPulseServiceEventsReceiver, new IntentFilter(LoopPulseServiceBroadcaster.INTENT_NAME));
    }

    public void authenticate(String appID, String appToken) {
        LoopPulseServiceExecutor.startActionAuth(context, appID, appToken);
    }

    public void startMonitoring() {
        LoopPulseServiceExecutor.startActionStartMonitoring(context);
    }

    public void stopMonitoring() {
        LoopPulseServiceExecutor.startActionStopMonitoring(context);
    }

    public void identifyUser(String externalID) {
        LoopPulseServiceExecutor.startActionIdentifyUser(context, externalID);
    }
}
