package com.clionelabs.looppulse.sdktest.model;

import com.clionelabs.looppulse.sdktest.SDKTestRunner;
import com.clionelabs.looppulse.sdk.model.VisitorIdentifyEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by hiukim on 2014-10-12.
 */

@RunWith(SDKTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class VisitorIdentifyEventTest {

    Field externalIDField;
    Field createdAtField;

    String externalId = "DUMMY_EXTERNAL_ID";
    Date now = new Date();

    @Before
    public void setUp() {
        try {
            externalIDField = VisitorIdentifyEvent.class.getDeclaredField("externalID");
            createdAtField = VisitorIdentifyEvent.class.getDeclaredField("createdAt");
            externalIDField.setAccessible(true);
            createdAtField.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            fail("NoSuchFieldException: " + ex);
        }
    }

    @Test
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
