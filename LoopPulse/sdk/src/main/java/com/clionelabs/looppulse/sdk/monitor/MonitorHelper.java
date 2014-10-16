package com.clionelabs.looppulse.sdk.monitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by hiukim on 2014-10-16.
 */
public class MonitorHelper {
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

    public MonitorHelper(Context context) {
        this.context = context;
        this.defaultRegion = new Region("LoopPulse-Generic", null, null, null); // TODO
        this.beaconManager = new BeaconManager(context);
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(final ConnectListener listener) {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                listener.onConnected();
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
    }

    public void scheduleNextRanging() {
        if (!this.isMonitoring) return;

        long inactiveSec = (new Date().getTime() - rangingStatus.getLastActiveTime().getTime()) / 1000;
        int nextScheduleSec = (int) Math.min(inactiveSec / 2, MAX_RESCHEDULE_SEC);
        Log.d(TAG, "next range in " + nextScheduleSec + " seconds");
        LoopPulseServiceExecutor.setRangeAlarm(context, nextScheduleSec);
    }

    public void stopRanging() {
        this.isMonitoring = false;
        LoopPulseServiceExecutor.cancelRangeAlarm(context);
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
}
