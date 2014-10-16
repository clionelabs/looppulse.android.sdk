package com.clionelabs.looppulse.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.clionelabs.looppulse.sdk.account.AccountHelper;
import com.clionelabs.looppulse.sdk.account.AuthenticationListener;
import com.clionelabs.looppulse.sdk.account.DeviceUuidFactory;
import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;
import com.clionelabs.looppulse.sdk.datastore.DataStoreHelper;
import com.clionelabs.looppulse.sdk.datastore.VisitorIdentifyEvent;
import com.clionelabs.looppulse.sdk.monitor.MonitorHelper;
import com.clionelabs.looppulse.sdk.monitor.RangingListener;
import com.clionelabs.looppulse.sdk.util.PreferencesManager;
import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;

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
    public enum ActionType {AUTH, START_MONITORING, STOP_MONITORING, DO_RANGE, IDENTIFY_USER, ENTER_GEOFENCE, EXIT_GEOFENCE};

    private String visitorUUID;
    private PreferencesManager preferencesManager;
    private AccountHelper accountHelper;
    private DataStoreHelper dataStoreHelper;
    private MonitorHelper monitorHelper;
    private HelperAcquirer helperAcquirer;
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
        } else if (action.equals(ActionType.ENTER_GEOFENCE.toString())) {
            execActionEnterGeofence(intent);
        } else if (action.equals(ActionType.EXIT_GEOFENCE.toString())) {
            execActionExitGeofence(intent);
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
        helperAcquirer = new HelperAcquirer();
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
        execActionWithRequiredHelpers(new Helper[] {dataStoreHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String externalID = intent.getStringExtra(EXTRA_IDENTIFY_USER_EXTERNAL_ID);
                VisitorIdentifyEvent event = accountHelper.identifyUser(externalID);
                dataStoreHelper.createFirebaseVisitorIdentifyEvent(event);
                return null;
            }
        });
    }

    private void execActionStartMonitoring(Intent intent) {
        execActionWithRequiredHelpers(new Helper[] {dataStoreHelper, monitorHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                monitorHelper.startRanging();

                Log.d(TAG, "Start Monitoring Ready");
                LoopPulseServiceBroadcaster.sendMonitoringStarted(context);
                return null;
            }
        });
    }

    private void execActionStopMonitoring(Intent intent) {
        execActionWithRequiredHelpers(new Helper[] {monitorHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                monitorHelper.stopRanging();
                return null;
            }
        });
    }

    private void execActionEnterGeofence(Intent intent) {
        execActionWithRequiredHelpers(new Helper[] {monitorHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                monitorHelper.enterGeofence();
                return null;
            }
        });
    }

    private void execActionExitGeofence(Intent intent) {
        execActionWithRequiredHelpers(new Helper[] {monitorHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                monitorHelper.exitGeofence();
                return null;
            }
        });
    }

    private void execActionDoRanging(Intent intent) {
        execActionWithRequiredHelpers(new Helper[] {monitorHelper}, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
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
                return null;
            }
        });
    }

    private void execActionWithRequiredHelpers(Helper[] helpers, final Callable<Void> func) {
        helperAcquirer.acquireHelpers(new ArrayList<Helper>(Arrays.asList(helpers)), new HelperAcquirerListener() {
            @Override
            public void onReady() {
                try {
                    func.call();
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to call func: " + ex);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
