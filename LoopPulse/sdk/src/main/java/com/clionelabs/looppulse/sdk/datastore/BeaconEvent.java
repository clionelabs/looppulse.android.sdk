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

    private String uuid;
    private int major;
    private int minor;
    private String visitorUUID;
    private String captureId;
    private EventType type;
    private Date createdAt;

    public BeaconEvent(String visitorUUID, String captureId, Beacon beacon, EventType eventType, Date createdAt) {
        this.uuid = beacon.getProximityUUID();
        this.major = beacon.getMajor();
        this.minor = beacon.getMinor();
        this.visitorUUID = visitorUUID;
        this.captureId = captureId;
        this.type = eventType;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject() {
        String typeString = "";
        switch (type) {
            case ENTER:
                typeString = "didEnterRegion";
                break;
            case EXIT:
                typeString = "didExitRegion";
                break;
            default:
                break;
        }

        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("uuid", uuid);
        eventInfo.put("major", major);
        eventInfo.put("minor", minor);
        eventInfo.put("visitor_uuid", visitorUUID);
        eventInfo.put("capture_id", captureId);
        eventInfo.put("type", typeString);
        eventInfo.put("created_at", createdAt.toString());
        return eventInfo;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(visitorUUID);
        out.writeString(captureId);
        out.writeSerializable(createdAt);
        out.writeInt(major);
        out.writeInt(minor);
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
        visitorUUID = in.readString();
        captureId = in.readString();
        createdAt = (Date) in.readSerializable();
        major = in.readInt();
        minor = in.readInt();
        type = (EventType) in.readSerializable();
        uuid = in.readString();
    }
}
