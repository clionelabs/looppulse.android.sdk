/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.megabox.model;

import com.clionelabs.lighthouse.R;
import com.google.common.collect.Maps;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by gilbert on 14-8-22.
 */
public class History {

    private String type;
    private String body;
    private Long createdAt;
    private Long sortedBy;

    private static HashMap<String, Integer> sTypeDrawableResIdMap;

    private static Long sMinute = 60L * 1000;

    static {
        sTypeDrawableResIdMap = Maps.newHashMap();
        sTypeDrawableResIdMap.put("message", R.drawable.icon_star);
        sTypeDrawableResIdMap.put("visit", R.drawable.icon_pin);
    }

    public History() { }

    public History(final String type, final String body, final Long createdAt, final Long sortedBy) {
        this.type = type;
        this.body = body;
        this.createdAt = createdAt;
        this.sortedBy = sortedBy;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getSortedBy() {
        return sortedBy;
    }

    public Integer getHistoryDrawableId() {
        return sTypeDrawableResIdMap.get(this.type);
    }

    public String getTimeString() {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(new Date(this.createdAt));
    }

    public String getDuration() {
        final double minute = (sortedBy - createdAt) / sMinute;
        final NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        return nf.format(minute);
    }
}
