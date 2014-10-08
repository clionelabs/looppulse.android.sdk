package com.clionelabs.looppulse.sdk.receivers;

import com.estimote.sdk.Beacon;

import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-08.
 */
public interface RangingRunnableListener {
    public void onFinishedRanging(HashMap<String, Beacon> detectedBeacons);
}
