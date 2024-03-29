package com.clionelabs.looppulse.sdk.datastore;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-07.
 */
public class VisitorIdentifyEvent implements FirebaseEvent {
    private static String typeName = "Identify";
    private String visitorUUID;
    private String externalID;
    private Date createdAt;

    public VisitorIdentifyEvent(String visitorUUID, String externalID, Date createdAt) {
        this.visitorUUID = visitorUUID;
        this.externalID = externalID;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject() {
        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("type", typeName);
        eventInfo.put("created_at", createdAt.toString());
        eventInfo.put("visitorUUID", visitorUUID);
        eventInfo.put("externalID", externalID);
        return eventInfo;
    }
}
