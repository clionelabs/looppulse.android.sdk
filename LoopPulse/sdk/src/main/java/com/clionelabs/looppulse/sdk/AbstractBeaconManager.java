/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.looppulse.sdk;

/**
 * Created by hiukim on 2014-10-07.
 */
public abstract class AbstractBeaconManager {
    protected enum Status {INACTIVE, READY, MONITORING}
    protected Status status;

    public AbstractBeaconManager() {
        status = Status.INACTIVE;
    }

    public void initialize() {
        if (status != Status.INACTIVE) throw new RuntimeException("Beacon Manager has already been initialized");
        status = Status.READY;
    }

    public void startLocationMonitoring() {
        if (status == Status.INACTIVE) throw new RuntimeException("Beacon Manager is Inactive");
        if (status == Status.MONITORING) throw new RuntimeException("Beacon Manager is already monitoring");
        status = Status.MONITORING;
    }

    public void stopLocationMonitoring() {
        if (status != Status.MONITORING) throw new RuntimeException("Beacon Manager is not currently monitoring");
        status = Status.READY;
    }
}
