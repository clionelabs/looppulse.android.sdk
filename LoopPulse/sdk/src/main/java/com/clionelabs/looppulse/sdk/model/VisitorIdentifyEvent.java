package com.clionelabs.looppulse.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-07.
 */
public class VisitorIdentifyEvent implements FirebaseEvent, Parcelable {
    private static String typeName = "Identify";
    private String externalID;
    private Date createdAt;

    public VisitorIdentifyEvent(String externalID, Date createdAt) {
        this.externalID = externalID;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject(String visitorUUID) {
        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("type", typeName);
        eventInfo.put("created_at", createdAt.toString());
        eventInfo.put("visitorUUID", visitorUUID);
        eventInfo.put("externalID", externalID);
        return eventInfo;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(externalID);
        out.writeSerializable(createdAt);
    }

    public static final Parcelable.Creator<VisitorIdentifyEvent> CREATOR = new Parcelable.Creator<VisitorIdentifyEvent>() {
        public VisitorIdentifyEvent createFromParcel(Parcel in) {
            return new VisitorIdentifyEvent(in);
        }

        public VisitorIdentifyEvent[] newArray(int size) {
            return new VisitorIdentifyEvent[size];
        }
    };

    private VisitorIdentifyEvent(Parcel in) {
        externalID = in.readString();
        createdAt = (Date) in.readSerializable();
    }
}
