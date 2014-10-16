package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.clionelabs.looppulse.sdk.StartMonitoringBootstrap;
import com.clionelabs.looppulse.sdk.StartMonitoringBootstrapListener;
import com.clionelabs.looppulse.sdk.account.AccountHelper;
import com.clionelabs.looppulse.sdk.account.AuthenticationListener;
import com.clionelabs.looppulse.sdk.datastore.DataStoreHelper;
import com.clionelabs.looppulse.sdk.datastore.FirebaseAuthenticationListener;
import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;
import com.clionelabs.looppulse.sdk.datastore.VisitorIdentifyEvent;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelper;
import com.clionelabs.looppulse.sdk.monitor.RangingListener;
import com.clionelabs.looppulse.sdk.account.DeviceUuidFactory;
import com.estimote.sdk.Beacon;

import java.util.Date;

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
    public static final String EXTRA_IDENTIFY_USER_EXTERNAL_ID = "com.clionelabs.looppulse.sdk.services.EXTRA_IDENTIFY_USER_EXTERNAL_ID";
    public enum ActionType {AUTH, START_MONITORING, STOP_MONITORING, DO_RANGE, IDENTIFY_USER};

    private String visitorUUID;
    private PreferencesManager preferencesManager;
    private AccountHelper accountHelper;
    private DataStoreHelper dataStoreHelper;
    private MonitorHelper monitorHelper;
    private Context context;
    private boolean isInitialized;

    public LoopPulseService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();

        String action = intent.getAction();
        Log.d(TAG, "onStartCommand action: " + intent.getAction());
        if (action.equals(ActionType.AUTH.toString())) {
            execActionAuth(intent);
        } else if (action.equals(ActionType.START_MONITORING.toString())) {
            execActionStartMonitoring(intent);
        } else if (action.equals(ActionType.STOP_MONITORING.toString())) {
            execActionStopMonitoring(intent);
        } else if (action.equals(ActionType.DO_RANGE.toString())) {
            execActionDoRanging(intent);
            LoopPulseReceiver.completeWakefulIntent(intent);
        } else if (action.equals(ActionType.IDENTIFY_USER.toString())) {
            execIdentifyUser(intent);
        } else {
            Log.d(TAG, "unrecognized action");
        }
        return Service.START_NOT_STICKY;
    }

    private void init() {
        if (isInitialized) return;
        context = this;
        visitorUUID = new DeviceUuidFactory(context).getDeviceUuid().toString();
        preferencesManager = PreferencesManager.getInstance(this);
        accountHelper = new AccountHelper(this, preferencesManager);
        dataStoreHelper = new DataStoreHelper(this, preferencesManager, visitorUUID);
        monitorHelper = new MonitorHelper(this);
        isInitialized = true;
    }

    private void execActionAuth(Intent intent) {
        String appID = intent.getStringExtra(EXTRA_AUTH_APP_ID);
        String appToken = intent.getStringExtra(EXTRA_AUTH_APP_TOKEN);
        accountHelper.auth(appID, appToken, new AuthenticationListener() {
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

    private void execIdentifyUser(final Intent intent) {
        dataStoreHelper.authenticateFirebase(new FirebaseAuthenticationListener() {
            @Override
            public void onAuthenticated(boolean isSuccess) {
                String externalID = intent.getStringExtra(EXTRA_IDENTIFY_USER_EXTERNAL_ID);
                VisitorIdentifyEvent event = accountHelper.identifyUser(externalID);
                dataStoreHelper.createFirebaseVisitorIdentifyEvent(event);
            }
        });
    }

    private void execActionStartMonitoring(Intent intent) {
        // There are multiple entities that need to be setup asynchronously. We just wrap the login inside the BootStrap class for clarity.
        // the bootstrap onReady() callback is triggered when all the entities are ready.
        StartMonitoringBootstrap bootstrap = new StartMonitoringBootstrap(dataStoreHelper, monitorHelper, new StartMonitoringBootstrapListener() {
            @Override
            public void onReady() {
                monitorHelper.startRanging();

                Log.d(TAG, "Start Monitoring Ready");
                LoopPulseServiceBroadcaster.sendMonitoringStarted(context);
            }
        });
        bootstrap.start();
    }

    private void execActionStopMonitoring(Intent intent) {
        monitorHelper.stopRanging();
    }

    private void execActionDoRanging(Intent intent) {
        monitorHelper.doRanging(new RangingListener() {
            @Override
            public void onBeaconEntered(Beacon beacon) {
                Log.d(TAG, "onBeaconEntered " + beacon);
                BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, new Date());
                dataStoreHelper.createFirebaseBeaconEvent(beaconEvent);
            }

            @Override
            public void onBeaconExited(Beacon beacon) {
                Log.d(TAG, "onBeaconExited " + beacon);
                BeaconEvent beaconEvent = new BeaconEvent(beacon, BeaconEvent.EventType.EXIT, new Date());
                dataStoreHelper.createFirebaseBeaconEvent(beaconEvent);
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
