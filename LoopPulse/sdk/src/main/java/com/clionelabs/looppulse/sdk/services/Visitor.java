/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.clionelabs.looppulse.sdk.util.DeviceUuidFactory;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;

/**
 * Created by simon on 6/6/14.
 */
public class Visitor {
    private static String TAG = Visitor.class.getCanonicalName();
    private String uuid;
    private String model;
    private String systemVersion;
    private boolean enableTracking;

    public Visitor(Context context) {
        this.model = Build.MODEL;
        this.systemVersion = Build.VERSION.RELEASE;
    }

    // Note: getAdvertisingIdInfo is a blocking call. So cannot be run on main thread.
    public void acquireUUID(Context context) {
        Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            Log.d(TAG, "retrieved advertising id: " + adInfo.getId());
        } catch (Exception e) {
            Log.d(TAG, "fail retrieving advertising id: " + e);
        }
        if (adInfo != null) {
            this.uuid = adInfo.getId();
            this.enableTracking = !adInfo.isLimitAdTrackingEnabled();
        } else { // fallback
            this.uuid = new DeviceUuidFactory(context).getDeviceUuid().toString();
            this.enableTracking = true;
        }
    }

    public String getUUID() {
        return uuid;
    }

    public String getModel() {
        return model;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public boolean isEnableTracking() {
        return enableTracking;
    }
}
