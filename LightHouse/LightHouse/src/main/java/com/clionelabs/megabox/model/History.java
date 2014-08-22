/*
 * Copyright (c) 2014 Clione Labs. All rights reserved.
 */

package com.clionelabs.megabox.model;

import com.clionelabs.lighthouse.R;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * TODO Should be two Classes
 * Created by gilbert on 14-8-22.
 */
public class History {

    private String type;
    private String body;
    private String location;
    private Long createdAt;
    private Long sortedBy;

    private Integer durationInSeconds;
    private Long enteredAt;
    private Long exitedAt;

    private static HashMap<String, Integer> sTypeDrawableResIdMap;

    private static Integer sMinute = 60;

    private static String MSG_TYPE = "message";
    private static String VISIT_TYPE = "visit";

    static {
        sTypeDrawableResIdMap = Maps.newHashMap();
        sTypeDrawableResIdMap.put(MSG_TYPE, R.drawable.icon_star);
        sTypeDrawableResIdMap.put(VISIT_TYPE, R.drawable.icon_pin);
    }

    public History() { }

    public History(final String type, final String body, final Long createdAt, final Long sortedBy) {
        this.type = type;
        this.body = body;
        this.createdAt = createdAt;
        this.sortedBy = sortedBy;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    @JsonIgnore
    public Long getEnteredAt() {
        return enteredAt;
    }

    @JsonIgnore
    public Long getExitedAt() {
        return exitedAt;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBody() {
        return body;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getSortedBy() {
        return sortedBy;
    }

    public String getDescription() {
        if (type.equals(MSG_TYPE)) {
            return body;
        } else {
            return location;
        }
    }

    @JsonIgnore
    public Integer getHistoryDrawableId() {
        return sTypeDrawableResIdMap.get(this.type);
    }

    public String getTimeString() {
        final Long time = createdAt != null ? createdAt : enteredAt;
        final SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(new Date(time));
    }

    public String getDuration() {
        final double minute = durationInSeconds.doubleValue() / sMinute;
        final NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        return nf.format(minute);
    }
}
