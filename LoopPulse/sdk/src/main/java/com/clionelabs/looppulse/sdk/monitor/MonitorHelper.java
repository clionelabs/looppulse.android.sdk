package com.clionelabs.looppulse.sdk.monitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clionelabs.looppulse.sdk.auth.AuthenticationResult;
import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hiukim on 2014-10-16.
 */
public class MonitorHelper {
    private static String TAG = MonitorHelper.class.getCanonicalName();

    private static int RANGE_PERIOD_SEC = 5;
    private static int MAX_RESCHEDULE_SEC = 1800; // 30 mins

    private Context context;
    private BeaconManager beaconManager;
    private Region allRegion;
    private final Object isRangingLock = new Object();
    private boolean isRanging;
    private RangingStatus rangingStatus;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isMonitoring = false;
    private boolean isReady;
    private ArrayList<GeofenceLocation> geofenceLocations;
    private ArrayList<Region> monitorRegions;

    public MonitorHelper(Context context) {
        this.context = context;
        this.allRegion = new Region("LoopPulse-Generic", null, null, null);
        this.beaconManager = new BeaconManager(context);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.geofenceLocations = new ArrayList<GeofenceLocation>();
        this.monitorRegions = new ArrayList<Region>();
        this.isReady = false;
    }

    public void setup(AuthenticationResult authenticationResult, final MonitorHelperSetupListener listener) {
        for (GeofenceLocation geofenceLocation: authenticationResult.geofenceLocations) {
            this.geofenceLocations.add(geofenceLocation);
        }

        for (Region region: authenticationResult.beaconRegions) {
            this.monitorRegions.add(region);
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
     * Filter out the beacons which are not currently under monitor.
     */
    public void filterMonitorBeacons(List<Beacon> beacons) {
        Iterator<Beacon> iter = beacons.iterator();
        while (iter.hasNext()) {
            Beacon beacon = iter.next();
            boolean existed = false;
            for (Region region: monitorRegions) {
                // Need to check major, minor?
                if (region.getProximityUUID().equals(beacon.getProximityUUID())) {
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                iter.remove();
            }
        }
    }

    /**
     * Do ranging for RANGE_PERIOD_SEC seconds, and get a set of currently detectedBeacons
     * Check the current detectedBeacons against the beaconsWithinSet
     * and decide whether an enter/exit events have occurred.
     */
    public void doRanging(final RangingListener listener) {
        if (!isReady) {
            Log.d(TAG, "BeaconManager is not connected yet.");
            return;
        }
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
                    filterMonitorBeacons(beacons);
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

        // We will monitor all regions, and then filter-out the LoopPulse ones later.
        new Thread(new RangingRunnable(beaconManager, allRegion, RANGE_PERIOD_SEC, handler)).start();
    }

    public void startRanging() {
        this.isMonitoring = true;
        this.rangingStatus = new RangingStatus();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable(); // this call is asynchronous? make it more reliable?
        }
        scheduleNextRanging();

        if (playServicesConnected()) {
            if (geofenceLocations.size() > 0) {
                GeofenceRequester geofenceRequester = new GeofenceRequester(context);
                geofenceRequester.addGeofences(geofenceLocations);
            }
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
            if (geofenceLocations.size() > 0) {
                GeofenceRemover geofenceRemover = new GeofenceRemover(context);
                ArrayList<String> requestIds = new ArrayList<String>();
                for (GeofenceLocation geofenceLocation : geofenceLocations) {
                    requestIds.add(geofenceLocation.getRequestId());
                }
                geofenceRemover.removeGeofencesById(requestIds);
            }
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
