package com.clionelabs.looppulse.sdk.test.model;

import android.os.Parcel;

import com.clionelabs.looppulse.sdk.model.BeaconEvent;
import com.estimote.sdk.Beacon;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-09.
 */
public class BeaconEventTest extends TestCase {
    private static final String TAG = BeaconEventTest.class.getCanonicalName();
    Field majorField;
    Field minorField;
    Field rssiField;
    Field uuidField;
    Field typeField;
    Field createdAtField;

    String proximityUUID = "dummy__x-xxxx-xxxx-xxxx-xxxxxxxxxxxx"; // need 32-characters
    String beaconName = "DUMMY_beaconName";
    String macAddress = "DUMMY_macAddress";
    int major = 1;
    int minor = 100;
    int measuredPower = 1000;
    int rssi = 2;
    Date now = new Date();
    Beacon beacon;

    protected void setUp() {
        try {
            majorField = BeaconEvent.class.getDeclaredField("major");
            minorField = BeaconEvent.class.getDeclaredField("minor");
            rssiField = BeaconEvent.class.getDeclaredField("rssi");
            uuidField = BeaconEvent.class.getDeclaredField("uuid");
            typeField = BeaconEvent.class.getDeclaredField("type");
            createdAtField = BeaconEvent.class.getDeclaredField("createdAt");

            majorField.setAccessible(true);
            minorField.setAccessible(true);
            rssiField.setAccessible(true);
            uuidField.setAccessible(true);
            typeField.setAccessible(true);
            createdAtField.setAccessible(true);

            beacon = new Beacon(proximityUUID, beaconName, macAddress, major, minor, measuredPower, rssi);

        } catch (NoSuchFieldException ex) {
            fail("NoSuchFieldException: " + ex);
        }
    }

    public void testConstructor() {
        BeaconEvent event = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, now);
        helpValidateEventFields(event);
    }

    public void testParcelable() {
        Parcel parcel = Parcel.obtain();
        BeaconEvent event = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, now);
        helpValidateEventFields(event);
        event.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        BeaconEvent outEvent = BeaconEvent.CREATOR.createFromParcel(parcel);
        helpValidateEventFields(outEvent);
    }

    public void testToFirebaseObject() {
        String visitorUUID = "DUMMY_visitorUUID";
        BeaconEvent event = new BeaconEvent(beacon, BeaconEvent.EventType.ENTER, now);
        HashMap<String, Object> eventInfo = event.toFirebaseObject(visitorUUID);

        assertEquals(eventInfo.get("created_at"), now.toString());
        assertEquals(eventInfo.get("major"), major);
        assertEquals(eventInfo.get("minor"), minor);
        assertEquals(eventInfo.get("rssi"), rssi);
        assertEquals(eventInfo.get("uuid"), proximityUUID);
        assertEquals(eventInfo.get("visitor_uuid"), visitorUUID);
        assertEquals(eventInfo.get("type"), "didEnterRegion");
    }

    private void helpValidateEventFields(BeaconEvent event) {
        try {
            assertEquals(majorField.getInt(event), major);
            assertEquals(minorField.getInt(event), minor);
            assertEquals(rssiField.getInt(event), rssi);
            assertEquals(uuidField.get(event), proximityUUID);
            assertEquals(((Date) createdAtField.get(event)).getTime(), now.getTime());
            assertEquals(typeField.get(event), BeaconEvent.EventType.ENTER);
        } catch (IllegalAccessException ex) {
            fail("IllegalAccessException: " + ex);
        }
    }
}
