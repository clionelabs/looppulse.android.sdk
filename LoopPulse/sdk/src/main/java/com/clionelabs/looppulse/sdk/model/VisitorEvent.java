package com.clionelabs.looppulse.sdk.model;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-07.
 */
public class VisitorEvent implements FirebaseEvent {
    public enum EventType {IDENTIFY};

    private EventType type;
    private String visitorUUID;
    private String externalID;
    private Date createdAt;

    public VisitorEvent(EventType type, String visitorUUID, String externalID, Date createdAt) {
        this.type = type;
        this.visitorUUID = visitorUUID;
        this.externalID = externalID;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject() {
        String typeString = "";
        switch (type) {
            case IDENTIFY:
                typeString = "identify";
                break;
            default:
                break;
        }

        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("type", typeString);
        eventInfo.put("created_at", createdAt.toString());
        eventInfo.put("visitorUUID", visitorUUID);
        eventInfo.put("externalID", externalID);
        return eventInfo;
    }
}
