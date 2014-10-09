package com.clionelabs.looppulse.sdk.test.model;

import com.clionelabs.looppulse.sdk.model.VisitorIdentifyEvent;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by hiukim on 2014-10-10.
 */
public class VisitorIdentifyEventTest extends TestCase {

    Field externalIDField;
    Field createdAtField;

    String externalId = "DUMMY_EXTERNAL_ID";
    Date now = new Date();

    protected void setUp() {
        try {
            externalIDField = VisitorIdentifyEvent.class.getDeclaredField("externalID");
            createdAtField = VisitorIdentifyEvent.class.getDeclaredField("createdAt");
            externalIDField.setAccessible(true);
            createdAtField.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            fail("NoSuchFieldException: " + ex);
        }
    }

    public void testConstructor() {
        VisitorIdentifyEvent event = new VisitorIdentifyEvent(externalId, now);
        helpValidateEventFields(event);
    }

    private void helpValidateEventFields(VisitorIdentifyEvent event) {
        try {
            assertEquals(externalIDField.get(event), externalId);
            assertEquals(((Date) createdAtField.get(event)).getTime(), now.getTime());
        } catch (IllegalAccessException ex) {
            fail("IllegalAccessException: " + ex);
        }
    }
}
