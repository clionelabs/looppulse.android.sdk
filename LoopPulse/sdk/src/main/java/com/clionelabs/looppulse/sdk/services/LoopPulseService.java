package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.clionelabs.looppulse.sdk.auth.AuthenticationListener;
import com.clionelabs.looppulse.sdk.auth.AuthenticationManager;
import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;
import com.clionelabs.looppulse.sdk.datastore.DataStoreHelper;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelper;
import com.clionelabs.looppulse.sdk.monitor.RangingListener;
import com.clionelabs.looppulse.sdk.util.DeviceUuidFactory;
import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.estimote.sdk.Beacon;

import java.util.Date;
import java.util.HashMap;

/**
 * The main component of LoopPulse SDK, which exists as a background service.
 *
 * This class service two purposes:
 *  1) acted as an entry point of receiving LoopPulse work.
 *      - For clarity purpose, the heavy work are delegated to the helper classes.
 *  2) send LocalBroadcast event via the LoopPulseServiceBroadcaster class
 *      - These events are then listened from the LoopPulse API object, which then send feedback back to the client app.
 *
 */
public class LoopPulseService extends Service {
    private static String TAG = LoopPulseService.class.getCanonicalName();

    public static final String RANGE_ACTION_INTENT = "com.clionelabs.looppulse.sdk.services.action.RANGE";
    public static final String EXTRA_AUTH_APP_ID = "com.clionelabs.looppulse.sdk.services.EXTRA_AUTH_APP_ID";
    public static final String EXTRA_AUTH_APP_TOKEN = "com.clionelabs.looppulse.sdk.services.EXTRA_AUTH_APP_TOKEN";
    public static final String EXTRA_IDENTIFY_VISITOR_EXTERNAL_ID = "com.clionelabs.looppulse.sdk.services.EXTRA_IDENTIFY_VISITOR_EXTERNAL_ID";
    public static final String EXTRA_TAG_VISITOR_PROPERTIES = "com.clionelabs.looppulse.sdk.services.EXTRA_TAG_VISITOR_PROPERTIES";
    public enum ActionType {AUTH, START_MONITORING, STOP_MONITORING, DO_RANGE, IDENTIFY_VISITOR, TAG_VISITOR, ENTER_GEOFENCE, EXIT_GEOFENCE};

    private String visitorUUID;
    private PreferencesManager preferencesManager;
    private AuthenticationManager authenticationManager;
    private DataStoreHelper dataStoreHelper;
    private MonitorHelper monitorHelper;
    private Visitor visitor;
    private Context context;
    private boolean isInitialized;

    public LoopPulseService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();

        String action = intent.getAction();
        Log.d(TAG, "onStartCommand action: " + intent.getAction());

        // It is supposed to be the first action received by LoopPulseService
        if (action.equals(ActionType.AUTH.toString())) {
            execActionAuth(intent);
            return Service.START_REDELIVER_INTENT;
        }

        if (action.equals(ActionType.STOP_MONITORING.toString())) {
            execActionStopMonitoring(intent);
            return Service.START_NOT_STICKY;
        }

        // Actions required authentication action being done before.
        if (authenticationManager.isAutenticated()) { // authenticated
            startAuthenticatedAction(intent);
        } else if (authenticationManager.isAuthInfoReady()) { // not authenticated, but contain auto info (probably because Service has been killed by system
            Log.d(TAG, "Re-authenticating...");
            final Intent fIntent = intent;
            authenticationManager.auth(new AuthenticationListener() {
                @Override
                public void onAuthenticationError(String msg) {
                    Log.d(TAG, "onAuthenticationError: " + msg);
                }

                @Override
                public void onAuthenticated() {
                    Log.d(TAG, "onAuthenticated");
                    startAuthenticatedAction(fIntent);
                }
            });
        } else { // not authenticated yet
            Log.d(TAG, "Ignoring action " + action + "; Application has not yet been authenticated.");
        }
        return Service.START_NOT_STICKY;
    }

    private void startAuthenticatedAction(Intent intent) {
        String action = intent.getAction();
        if (action.equals(ActionType.START_MONITORING.toString())) {
            execActionStartMonitoring(intent);
        } else if (action.equals(ActionType.STOP_MONITORING.toString())) {
            execActionStopMonitoring(intent);
        } else if (action.equals(ActionType.DO_RANGE.toString())) {
            execActionDoRanging(intent);
            LoopPulseReceiver.completeWakefulIntent(intent);
        } else if (action.equals(ActionType.IDENTIFY_VISITOR.toString())) {
            execIdentifyUser(intent);
        } else if (action.equals(ActionType.TAG_VISITOR.toString())) {
            execTagVisitor(intent);
        } else if (action.equals(ActionType.ENTER_GEOFENCE.toString())) {
            execActionEnterGeofence(intent);
        } else if (action.equals(ActionType.EXIT_GEOFENCE.toString())) {
            execActionExitGeofence(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
    }

    private void init() {
        if (isInitialized) return;
        context = this;
        visitor = new Visitor(context);
        visitorUUID = new DeviceUuidFactory(context).getDeviceUuid().toString();
        preferencesManager = PreferencesManager.getInstance(this);
        dataStoreHelper = new DataStoreHelper(this, preferencesManager);
        monitorHelper = new MonitorHelper(this);
        authenticationManager = new AuthenticationManager(this, dataStoreHelper, monitorHelper, preferencesManager, visitor);
        isInitialized = true;
    }

    private void execActionAuth(Intent intent) {
        String appID = intent.getStringExtra(EXTRA_AUTH_APP_ID);
        String appToken = intent.getStringExtra(EXTRA_AUTH_APP_TOKEN);
        authenticationManager.setAuthInfo(appID, appToken);

        authenticationManager.auth(new AuthenticationListener() {
            @Override
            public void onAuthenticationError(String msg) {
                Log.d(TAG, "onAuthenticationError: " + msg);
                LoopPulseServiceBroadcaster.sendAuthAuthenticationError(context, msg);
            }

            @Override
            public void onAuthenticated() {
                Log.d(TAG, "onAuthenticated");
                LoopPulseServiceBroadcaster.sendAuthAuthenticated(context);
            }
        });
    }

    private void execTagVisitor(Intent intent) {
        HashMap<String, String> properties = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_TAG_VISITOR_PROPERTIES);
        dataStoreHelper.tagVisitor(visitor.getUUID(), properties);
    }

    private void execIdentifyUser(Intent intent) {
        String externalID = intent.getStringExtra(EXTRA_IDENTIFY_VISITOR_EXTERNAL_ID);
        visitor.setExternalID(externalID);
        dataStoreHelper.identifyVisitor(visitor.getUUID(), externalID);
    }

    private void execActionStartMonitoring(Intent intent) {
        monitorHelper.startRanging();
        LoopPulseServiceBroadcaster.sendMonitoringStarted(context);
    }

    private void execActionStopMonitoring(Intent intent) {
        monitorHelper.stopRanging();
        LoopPulseServiceBroadcaster.sendMonitoringStopped(context);
    }

    private void execActionEnterGeofence(Intent intent) {
        monitorHelper.enterGeofence();
    }

    private void execActionExitGeofence(Intent intent) {
        monitorHelper.exitGeofence();
    }

    private void execActionDoRanging(Intent intent) {
        monitorHelper.doRanging(new RangingListener() {
            @Override
            public void onBeaconEntered(Beacon beacon) {
                Log.d(TAG, "onBeaconEntered " + beacon);
                BeaconEvent beaconEvent = new BeaconEvent(visitor.getUUID(), beacon, BeaconEvent.EventType.ENTER, new Date());
                dataStoreHelper.createFirebaseBeaconEvent(beaconEvent);
                LoopPulseServiceBroadcaster.sendBeaconEvent(context, beaconEvent);
            }

            @Override
            public void onBeaconExited(Beacon beacon) {
                Log.d(TAG, "onBeaconExited " + beacon);
                BeaconEvent beaconEvent = new BeaconEvent(visitor.getUUID(), beacon, BeaconEvent.EventType.EXIT, new Date());
                dataStoreHelper.createFirebaseBeaconEvent(beaconEvent);
                LoopPulseServiceBroadcaster.sendBeaconEvent(context, beaconEvent);
            }

            @Override
            public void onFinished() {
                Log.d(TAG, "Ranging Finished");
                monitorHelper.scheduleNextRanging();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
