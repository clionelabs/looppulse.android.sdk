package com.clionelabs.looppulse.sdk;

import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;

/**
 * Created by hiukim on 2014-10-16.
 */
public interface LoopPulseListener {
    public void onAuthenticated();

    public void onAuthenticationError(String msg);

    public void onMonitoringStarted();

    public void onMonitoringStopped();

    public void onBeaconDetected(BeaconEvent event);
}
