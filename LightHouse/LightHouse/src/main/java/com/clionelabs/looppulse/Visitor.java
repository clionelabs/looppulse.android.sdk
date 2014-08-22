/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clieonelabs.looppulse;

import android.content.Context;
import android.os.Build;

import com.clieonelabs.looppulse.util.DeviceUuidFactory;

/**
 * Created by simon on 6/6/14.
 */
public class Visitor {
    private final Context context;
    private String uuid;
    private String externalID;

    // Survey of many ways to generate an unique ID
    // http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id

    public Visitor(Context context) {
        this.context = context;
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

    public String getDeviceBrand() {
        return Build.BRAND;
    }

    public String getDeviceModel() {
        return Build.MODEL;
    }

    public String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

}
