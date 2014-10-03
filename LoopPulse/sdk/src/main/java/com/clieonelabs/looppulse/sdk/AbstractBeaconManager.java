/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse.sdk;

/**
 * Created by simon on 6/6/14.
 */
public abstract class AbstractBeaconManager {

    abstract public void applicationDidLaunch();

    abstract public void startLocationMonitoring();

    abstract public void stopLocationMonitoringAndRanging();

    abstract public void startLocationMonitoringAndRanging();
}
