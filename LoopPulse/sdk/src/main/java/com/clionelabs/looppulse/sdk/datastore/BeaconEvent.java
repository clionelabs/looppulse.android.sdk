package com.clionelabs.looppulse.sdk.datastore;

import android.os.Parcel;
import android.os.Parcelable;

import com.estimote.sdk.Beacon;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-06.
 */
public class BeaconEvent implements FirebaseEvent, Parcelable {
    public enum EventType {ENTER, EXIT, RANGE};

    private double accuracy;
    private Date createdAt;
    private int major;
    private int minor;
    private String proximity;
    private int rssi;
    private EventType type;
    private String uuid;


    public BeaconEvent(Beacon beacon, EventType eventType, Date createdAt) {
        /**
         *  Don't forget to update Parcelable methods if the properties are changed!
         */
        this.accuracy = 0.0; // TODO
        this.major = beacon.getMajor();
        this.minor = beacon.getMinor();
        this.proximity = ""; // TODO
        this.rssi = beacon.getRssi();
        this.uuid = beacon.getProximityUUID();
        this.type = eventType;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject(String visitorUUID) {
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(accuracy);
        out.writeSerializable(createdAt);
        out.writeInt(major);
        out.writeInt(minor);
        out.writeString(proximity);
        out.writeInt(rssi);
        out.writeSerializable(type);
        out.writeString(uuid);
    }

    public static final Parcelable.Creator<BeaconEvent> CREATOR = new Parcelable.Creator<BeaconEvent>() {
        public BeaconEvent createFromParcel(Parcel in) {
            return new BeaconEvent(in);
        }

        public BeaconEvent[] newArray(int size) {
            return new BeaconEvent[size];
        }
    };

    private BeaconEvent(Parcel in) {
        accuracy = in.readDouble();
        createdAt = (Date) in.readSerializable();
        major = in.readInt();
        minor = in.readInt();
        proximity = in.readString();
        rssi = in.readInt();
        type = (EventType) in.readSerializable();
        uuid = in.readString();
    }
}
