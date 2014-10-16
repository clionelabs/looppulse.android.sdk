package com.clionelabs.looppulse.sdktest.model;

import com.clionelabs.looppulse.sdk.monitor.RangingStatus;
import com.clionelabs.looppulse.sdktest.SDKTestRunner;
import com.estimote.sdk.Beacon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 * Created by hiukim on 2014-10-14.
 */

@RunWith(SDKTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class RangingStatusTest {
    private SecureRandom random = new SecureRandom();
    private ArrayList<Beacon> beacons;
    private RangingStatus rangingStatus;
    private HashMap<String, Beacon> currentRangedBeaconsMap;
    private HashMap<String, Beacon> beaconsWithinMap;

    @Before
    public void setUp() {
        beacons = new ArrayList<Beacon>();
        for (int i = 0; i < 10; i++) {
            beacons.add(helpCreateBeacon());
        }
        rangingStatus = new RangingStatus();
        currentRangedBeaconsMap = rangingStatus.getCurrentRangedBeaconsMap();
        beaconsWithinMap = rangingStatus.getBeaconsWithinMap();
    }

    @Test
    public void testLastActivityTime() throws InterruptedException {
        long correctLastActiveTime = rangingStatus.getLastActiveTime().getTime();

        // Receiving a single beacon
        Thread.sleep(1);
        List<Beacon> newRangedBeacons = new ArrayList<Beacon>();
        newRangedBeacons.add(beacons.get(0));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();
        assertFalse(correctLastActiveTime == rangingStatus.getLastActiveTime().getTime());

        correctLastActiveTime = rangingStatus.getLastActiveTime().getTime();

        // Receiving same beacon - no change
        Thread.sleep(1);
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();
        assertTrue(correctLastActiveTime == rangingStatus.getLastActiveTime().getTime());

        correctLastActiveTime = rangingStatus.getLastActiveTime().getTime();

        // Exit
        Thread.sleep(1);
        newRangedBeacons.clear();
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();
        assertFalse(correctLastActiveTime == rangingStatus.getLastActiveTime().getTime());
    }

    @Test
    public void testGetEnteredExitedBeacons() {
        List<Beacon> enteredBeacons;
        List<Beacon> exitedBeacons;

        // Receiving a single beacon
        List<Beacon> newRangedBeacons = new ArrayList<Beacon>();
        newRangedBeacons.add(beacons.get(0));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);

        enteredBeacons = rangingStatus.getEnteredBeacons();
        exitedBeacons = rangingStatus.getExcitedBeacons();
        assertEquals(1, enteredBeacons.size());
        assertEquals(0, exitedBeacons.size());
        assertTrue(helpIsMapContainBeacon(enteredBeacons, beacons.get(0)));

        rangingStatus.updateStatus();

        // Receiving 2 more additional beacons
        newRangedBeacons.add(beacons.get(1));
        newRangedBeacons.add(beacons.get(2));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        enteredBeacons = rangingStatus.getEnteredBeacons();
        exitedBeacons = rangingStatus.getExcitedBeacons();
        assertEquals(2, enteredBeacons.size());
        assertEquals(0, exitedBeacons.size());
        assertTrue(helpIsMapContainBeacon(enteredBeacons, beacons.get(1)));
        assertTrue(helpIsMapContainBeacon(enteredBeacons, beacons.get(2)));

        rangingStatus.updateStatus();

        // 2 exit, 1 stayed, 1 enter
        newRangedBeacons.remove(beacons.get(0));
        newRangedBeacons.remove(beacons.get(1));
        newRangedBeacons.add(beacons.get(3));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        enteredBeacons = rangingStatus.getEnteredBeacons();
        exitedBeacons = rangingStatus.getExcitedBeacons();
        assertEquals(1, enteredBeacons.size());
        assertEquals(2, exitedBeacons.size());
        assertTrue(helpIsMapContainBeacon(enteredBeacons, beacons.get(3)));
        assertTrue(helpIsMapContainBeacon(exitedBeacons, beacons.get(0)));
        assertTrue(helpIsMapContainBeacon(exitedBeacons, beacons.get(1)));
    }

    @Test
    public void testUpdateStatus() {
        // Empty
        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(0, beaconsWithinMap.keySet().size());

        // Receiving a single beacon
        List<Beacon> newRangedBeacons = new ArrayList<Beacon>();
        newRangedBeacons.add(beacons.get(0));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();

        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(1, beaconsWithinMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(0)));

        // Test: receiving two more beacons, together with the existing one
        newRangedBeacons.add(beacons.get(1));
        newRangedBeacons.add(beacons.get(2));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();

        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(3, beaconsWithinMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(0)));
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(1)));
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(2)));

        // Test: beacon-1 is missing
        newRangedBeacons.remove(beacons.get(1));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();
        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(2, beaconsWithinMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(0)));
        assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(2)));

        // Test: receiving 7 more beacons, and removed all the previous
        newRangedBeacons.clear();
        for (int i = 3; i < 10; i++) {
            newRangedBeacons.add(beacons.get(i));
        }
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        rangingStatus.updateStatus();
        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(7, beaconsWithinMap.keySet().size());
        for (int i = 3; i < 10; i++) {
            assertTrue(helpIsMapContainBeacon(beaconsWithinMap, beacons.get(i)));
        }
    }

    @Test
    public void testReceiveRangingBeacons() {
        assertEquals(0, currentRangedBeaconsMap.keySet().size());
        assertEquals(0, beaconsWithinMap.keySet().size());

        // Test: receiving a single beacon
        List<Beacon> newRangedBeacons = new ArrayList<Beacon>();
        newRangedBeacons.add(beacons.get(0));

        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        assertEquals(1, currentRangedBeaconsMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(0)));

        // Test: receiving the same beacon
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        assertEquals(1, currentRangedBeaconsMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(0)));

        // Test: receiving two more beacons
        newRangedBeacons.add(beacons.get(1));
        newRangedBeacons.add(beacons.get(2));
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        assertEquals(3, currentRangedBeaconsMap.keySet().size());
        assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(0)));
        assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(1)));
        assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(2)));

        // Test: receiving 10 more beacons, including the original 3
        for (int i = 3; i < 10; i++) {
            newRangedBeacons.add(beacons.get(i));
        }
        rangingStatus.receiveRangingBeacons(newRangedBeacons);
        assertEquals(10, currentRangedBeaconsMap.keySet().size());
        for (int i = 0; i < 10; i++) {
            assertTrue(helpIsMapContainBeacon(currentRangedBeaconsMap, beacons.get(i)));
        }

        assertEquals(0, beaconsWithinMap.keySet().size());
    }


    private boolean helpIsMapContainBeacon(List<Beacon> beacons, Beacon beacon) {
        for (Beacon b: beacons) {
            if (b.getProximityUUID() == beacon.getProximityUUID()) return true;
        }
        return false;
    }
    private boolean helpIsMapContainBeacon(HashMap<String, Beacon> map, Beacon beacon) {
        for (Beacon b: map.values()) {
            if (b.getProximityUUID() == beacon.getProximityUUID()) return true;
        }
        return false;
    }

    private Beacon helpCreateBeacon() {
        String proximityUUID = UUID.randomUUID().toString();
        String beaconName = "DUMMY_beaconName";
        String macAddress = "DUMMY_macAddress";
        int major = 1;
        int minor = 100;
        int measuredPower = 1000;
        int rssi = 2;
        return new Beacon(proximityUUID, beaconName, macAddress, major, minor, measuredPower, rssi);
    }
}
