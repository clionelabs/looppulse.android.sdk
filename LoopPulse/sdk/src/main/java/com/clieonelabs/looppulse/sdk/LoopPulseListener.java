package com.clieonelabs.looppulse.sdk;

/**
 * Created by hiukim on 2014-10-04.
 */
public interface LoopPulseListener {
    public void onAuthenticated();

    public void onAuthenticationError(String msg);
}
