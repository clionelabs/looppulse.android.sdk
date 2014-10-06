package com.clionelabs.looppulse.sdk.model;

import com.estimote.sdk.Beacon;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-06.
 */
public class BeaconEvent implements FirebaseEvent {
    public enum EventType {ENTER, EXIT, RANGE};

    private double accuracy;
    private Date createdAt;
    private int major;
    private int minor;
    private String proximity;
    private int rssi;
    private EventType type;
    private String uuid;
    private String visitorUUID;

    public BeaconEvent(Beacon beacon, EventType eventType, String visitorUUID, Date createdAt) {
        this.accuracy = 0.0; // TODO
        this.major = beacon.getMajor();
        this.minor = beacon.getMinor();
        this.proximity = ""; // TODO
        this.rssi = beacon.getRssi();
        this.uuid = beacon.getProximityUUID();
        this.visitorUUID = visitorUUID;
        this.type = eventType;
        this.createdAt = createdAt;
    }

    public HashMap<String, Object> toFirebaseObject() {
        String typeString = "";
        switch (type) {
            case ENTER:
                typeString = "didEnterRegion";
                break;
            case EXIT:
                typeString = "didExitRegion";
                break;
            case RANGE:
                typeString = "didRangeBeacons";
                break;
            default:
                break;
        }

        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("accuracy", accuracy);
        eventInfo.put("created_at", createdAt.toString());
        eventInfo.put("major", major);
        eventInfo.put("minor", minor);
        eventInfo.put("proximity", proximity);
        eventInfo.put("rssi", rssi);
        eventInfo.put("uuid", uuid);
        eventInfo.put("type", typeString);
        eventInfo.put("visitor_uuid", visitorUUID);
        return eventInfo;
    }
}
