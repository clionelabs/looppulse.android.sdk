/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.lighthouse;

import java.util.Date;

/**
 * Created by simon on 5/6/14.
 */
public class BeaconEvent {

    Date createdAt;
    Date lastSeenAt;
    String uuid;
    int major;
    int minor;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }
}
