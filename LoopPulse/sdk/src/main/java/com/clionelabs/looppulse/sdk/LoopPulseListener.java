package com.clionelabs.looppulse.sdk;

/**
 * Created by hiukim on 2014-10-16.
 */
public interface LoopPulseListener {
    public void onAuthenticated();

    public void onAuthenticationError(String msg);

    public void onMonitoringStarted();

    public void onMonitoringStopped();
}
