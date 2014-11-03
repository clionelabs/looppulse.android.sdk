package com.clionelabs.looppulse.sdk.datastore;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by hiukim on 2014-11-04.
 */
public class VisitorTagEvent  implements FirebaseEvent {
    private static String typeName = "tag";
    private String visitorUUID;
    private HashMap<String, String> properties;
    private Date createdAt;

    public VisitorTagEvent(String visitorUUID, HashMap<String, String> properties, Date createdAt) {
        this.visitorUUID = visitorUUID;
        this.properties = properties;
        this.createdAt = createdAt;
    }

    @Override
    public HashMap<String, Object> toFirebaseObject() {
        HashMap<String, Object> eventInfo = new HashMap<String, Object>();
        eventInfo.put("type", typeName);
        eventInfo.put("created_at", createdAt.toString());
        eventInfo.put("visitorUUID", visitorUUID);
        eventInfo.put("properties", properties);
        return eventInfo;
    }
}
