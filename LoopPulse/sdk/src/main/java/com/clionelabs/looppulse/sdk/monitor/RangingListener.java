package com.clionelabs.looppulse.sdk.monitor;

import com.estimote.sdk.Beacon;

/**
 * Created by hiukim on 2014-10-16.
 */
public interface RangingListener {
    public void onBeaconEntered(Beacon beacon);
    public void onBeaconExited(Beacon beacon);
    public void onFinished();
}
