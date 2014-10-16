package com.clionelabs.looppulse.sdk.datastore;

import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-09.
 */
public interface FirebaseEvent {
    public HashMap<String, Object> toFirebaseObject(String visitorUUID);
}
