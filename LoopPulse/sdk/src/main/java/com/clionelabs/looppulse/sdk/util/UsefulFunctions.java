package com.clionelabs.looppulse.sdk.util;

import com.estimote.sdk.Beacon;

/**
 * Created by hiukim on 2014-10-08.
 */
public class UsefulFunctions {

    /**
     * Generate a unique key for beacon region
     *
     * @param beacon
     * @return key
     */
    public static String getBeaconKey(Beacon beacon) {
        return beacon.getProximityUUID() + "-" + beacon.getMajor() + "-" + beacon.getMinor();
    }
}
