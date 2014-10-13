package com.clionelabs.looppulse.sdk.services;

import com.estimote.sdk.Beacon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hiukim on 2014-10-14.
 */
public class RangingStatus {
    private HashMap<String, Beacon> beaconsWithinMap; // key -> beacon
    private HashMap<String, Beacon> currentRangedBeaconsMap; // key -> beacon
    private Date lastActiveTime;

    public RangingStatus() {
        beaconsWithinMap = new HashMap<String, Beacon>(); // key -> beacon
        currentRangedBeaconsMap = new HashMap<String, Beacon>(); // key -> beacon
        lastActiveTime = new Date();
    }

    /**
     *  update the beaconsWithin using the currentRangedBeacons, and then clear currentRangedBeacons.
     */
    public void updateStatus() {
        if (getEnteredBeacons().size() > 0 || getExcitedBeacons().size() > 0) {
            lastActiveTime = new Date();
        }

        for (Beacon beacon: getEnteredBeacons()) {
            beaconsWithinMap.put(getBeaconKey(beacon), beacon);
        }
        for (Beacon beacon: getExcitedBeacons()) {
            beaconsWithinMap.remove(getBeaconKey(beacon));
        }
        currentRangedBeaconsMap.clear();
    }

    /**
     * get the newly entered beacons comparing beaconsWithin and currentRangedBeacons
     * @return list of newly entered Beacons
     */
    public ArrayList<Beacon> getEnteredBeacons() {
        ArrayList<Beacon> enteredBeacons = new ArrayList<Beacon>();
        for (String key: currentRangedBeaconsMap.keySet()) {
            if (!beaconsWithinMap.containsKey(key)) {
                enteredBeacons.add(currentRangedBeaconsMap.get(key));
            }
        }
        return enteredBeacons;
    }

    /**
     * get the newly exited beacons comparing beaconsWithin and currentRangedBeacons
     * @return list of newly exited Beacons
     */
    public ArrayList<Beacon> getExcitedBeacons() {
        ArrayList<Beacon> exitedBeacons = new ArrayList<Beacon>();
        for (String key: beaconsWithinMap.keySet()) {
            if (!currentRangedBeaconsMap.containsKey(key)) {
                exitedBeacons.add(beaconsWithinMap.get(key));
            }
        }
        return exitedBeacons;
    }

    /**
     * Push the received beacons into currentRangedBeacons, if not already existed
     * @param beacons
     */
    public void receiveRangingBeacons(List<Beacon> beacons) {

        for (Beacon beacon: beacons) {
            // TODO: check whether it is a LoopPulse beacon
            String key = getBeaconKey(beacon);
            if (!currentRangedBeaconsMap.containsKey(key)) {
                currentRangedBeaconsMap.put(key, beacon);
            }
        }
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public HashMap<String, Beacon> getBeaconsWithinMap () {
        return beaconsWithinMap;
    }

    public HashMap<String, Beacon> getCurrentRangedBeaconsMap () {
        return currentRangedBeaconsMap;
    }

    private String getBeaconKey(Beacon beacon) {
        return beacon.getProximityUUID() + "-" + beacon.getMajor() + "-" + beacon.getMinor();
    }
}
