package com.clionelabs.looppulse.sdk.model;

import java.util.HashMap;

/**
 * Created by hiukim on 2014-10-09.
 */
public interface FirebaseEvent {
    public HashMap<String, Object> toFirebaseObject(String visitorUUID);
}
