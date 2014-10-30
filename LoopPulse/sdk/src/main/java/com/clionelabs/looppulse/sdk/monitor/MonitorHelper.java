package com.clionelabs.looppulse.sdk.monitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.Helper;
import com.clionelabs.looppulse.sdk.services.HelperListener;
import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by hiukim on 2014-10-16.
 */
public class MonitorHelper implements Helper {
    private static String TAG = MonitorHelper.class.getCanonicalName();

    private static int RANGE_PERIOD_SEC = 5;
    private static int MAX_RESCHEDULE_SEC = 1800; // 30 mins

    private Context context;
    private BeaconManager beaconManager;
    private Region defaultRegion;
    private final Object isRangingLock = new Object();
    private boolean isRanging;
    private RangingStatus rangingStatus;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isMonitoring = false;
    private boolean isReady;

    // TODO: The following values should read from server configuration. here might be multiple geofence regions as well.
    private final String GEOFENCE_requestId = "1";
    private final double GEOFENCE_latitude = 22.286763;
    private final double GEOFENCE_longitude = 114.190319;
    private final float GEOFENCE_radius = 50.0f;

    public MonitorHelper(Context context) {
        this.context = context;
        this.defaultRegion = new Region("LoopPulse-Generic", null, null, null); // TODO
        this.beaconManager = new BeaconManager(context);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.isReady = false;
    }

    @Override
    public void setup(final HelperListener listener) {
        if (isReady) {
            listener.onReady();
            return;
        }

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                isReady = true;
                listener.onReady();
            }
        });
    }

    /**
     * Do ranging for RANGE_PERIOD_SEC seconds, and get a set of currently detectedBeacons
     * Check the current detectedBeacons against the beaconsWithinSet
     * and decide whether an enter/exit events have occurred.
     */
    public void doRanging(final RangingListener listener) {
        if (getIsRanging()) {
            Log.d(TAG, "Already Ranging");
            return;
        }
        setIsRanging(true);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String type = msg.getData().getString(RangingRunnable.MSG_TYPE);
                if (type.equals(RangingRunnable.MSG_RANGE)) {
                    ArrayList<Beacon> beacons = msg.getData().getParcelableArrayList(RangingRunnable.BEACONS_LIST);
                    rangingStatus.receiveRangingBeacons(beacons);
                } else if (type.equals(RangingRunnable.MSG_FINISH)) {
                    for (Beacon beacon: rangingStatus.getEnteredBeacons()) {
                        listener.onBeaconEntered(beacon);
                    }
                    for (Beacon beacon: rangingStatus.getExcitedBeacons()) {
                        listener.onBeaconExited(beacon);
                    }
                    setIsRanging(false);
                    rangingStatus.updateStatus();
                    listener.onFinished();
                    scheduleNextRanging();
                }
            }
        };
        new Thread(new RangingRunnable(beaconManager, defaultRegion, RANGE_PERIOD_SEC, handler)).start();
    }

    public void startRanging() {
        this.isMonitoring = true;
        this.rangingStatus = new RangingStatus();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable(); // this call is asynchronous? make it more reliable?
        }
        scheduleNextRanging();

        if (playServicesConnected()) {
            GeofenceRequester geofenceRequester = new GeofenceRequester(context);
            ArrayList<GeofenceLocation> locations = new ArrayList<GeofenceLocation>();
            locations.add(new GeofenceLocation(GEOFENCE_requestId, GEOFENCE_latitude, GEOFENCE_longitude, GEOFENCE_radius));
            geofenceRequester.addGeofences(locations);
        }
    }

    /**
     * TODO: Improve the next ranging time, to make it more responsive and power-saving
     */
    public void scheduleNextRanging() {
        if (!this.isMonitoring) return;

        long last = rangingStatus.getLastActiveTime().getTime();
        if (rangingStatus.getLastEnterGeofenceTime() != null) {
            if (rangingStatus.getLastEnterGeofenceTime().after(rangingStatus.getLastActiveTime())) {
                last = rangingStatus.getLastEnterGeofenceTime().getTime();
            }
        }

        long inactiveSec = (new Date().getTime() - last) / 1000;
        int nextScheduleSec = (int) Math.min(inactiveSec / 2, MAX_RESCHEDULE_SEC);
        Log.d(TAG, "next range in " + nextScheduleSec + " seconds");
        LoopPulseServiceExecutor.setRangeAlarm(context, nextScheduleSec);
    }

    public void stopRanging() {
        this.isMonitoring = false;
        LoopPulseServiceExecutor.cancelRangeAlarm(context);

        if (playServicesConnected()) {
            GeofenceRemover geofenceRemover = new GeofenceRemover(context);
            ArrayList<String> requestIds = new ArrayList<String>();
            requestIds.add(GEOFENCE_requestId);
            geofenceRemover.removeGeofencesById(requestIds);
        }
    }

    public void enterGeofence() {
        if (!this.isMonitoring) return;
        rangingStatus.enteredGeofence();
        // Trigger Ranging action earlier
        scheduleNextRanging();
    }

    public void exitGeofence() {
        if (!this.isMonitoring) return;
        rangingStatus.exitedGeofence();
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

    /**
     * Verify that Google Play services is available before making a request.
     * @return true if Google Play services is available, otherwise false
     */
    private boolean playServicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
            // Google Play services was not available for some reason
        } else {
            Log.d(TAG, "google play services not available: " + resultCode);
            return false;
        }
    }
}
