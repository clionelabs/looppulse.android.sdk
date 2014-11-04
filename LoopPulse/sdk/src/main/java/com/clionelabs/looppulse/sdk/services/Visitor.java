/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.os.Build;

import com.clionelabs.looppulse.sdk.util.DeviceUuidFactory;

/**
 * Created by simon on 6/6/14.
 */
public class Visitor {
    private String uuid;
    private String externalID;
    private String model;
    private String systemVersion;

    public Visitor(Context context) {
        this.uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
        this.model = Build.MODEL;
        this.systemVersion = Build.VERSION.RELEASE;
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

    public String getModel() {
        return model;
    }

    public String getSystemVersion() {
        return systemVersion;
    }
}
