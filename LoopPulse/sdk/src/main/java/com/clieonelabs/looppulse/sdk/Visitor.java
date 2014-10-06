/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse.sdk;

import android.content.Context;

import com.clieonelabs.looppulse.sdk.util.DeviceUuidFactory;

/**
 * Created by simon on 6/6/14.
 */
public class Visitor {
    private String uuid;
    private String externalID;

    public Visitor(Context context) {
        this.uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public boolean hasExternalID() {
        return externalID != null && externalID.length() > 0;
    }
}
