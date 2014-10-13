package com.clionelabs.looppulse.sdktest.services;

import android.content.Intent;

import com.clionelabs.looppulse.sdk.services.DataStoreService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

/**
 * Created by hiukim on 2014-10-14.
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class DataStoreServiceTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testStartInitAction() {
        DataStoreService.startAction(Robolectric.application, DataStoreService.ActionType.INIT);

        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the DataStoreService service to be invoked",
                DataStoreService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected Init Action",
                DataStoreService.ActionType.INIT.toString(), serviceIntent.getAction());
    }

    @Test
    public void testStartFireBeaconEventAction() {
        DataStoreService.startAction(Robolectric.application, DataStoreService.ActionType.FIRE_BEACON_EVENT);

        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the RangingService service to be invoked",
                DataStoreService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected  Init Action",
                DataStoreService.ActionType.FIRE_BEACON_EVENT.toString(), serviceIntent.getAction());
    }

    @Test
    public void testFireIdentifyUserEventAction() {
        DataStoreService.startAction(Robolectric.application, DataStoreService.ActionType.FIRE_IDENTIFY_VISITOR_EVENT);
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the RangingService service to be invoked",
                DataStoreService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected Range Action",
                DataStoreService.ActionType.FIRE_IDENTIFY_VISITOR_EVENT.toString(), serviceIntent.getAction());
    }
}
