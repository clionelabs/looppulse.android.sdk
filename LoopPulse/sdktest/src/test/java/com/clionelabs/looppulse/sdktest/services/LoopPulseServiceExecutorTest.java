package com.clionelabs.looppulse.sdktest.services;

import android.content.Intent;

import com.clionelabs.looppulse.sdk.services.LoopPulseService;
import com.clionelabs.looppulse.sdk.services.LoopPulseServiceExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

/**
 * Created by hiukim on 2014-10-17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class LoopPulseServiceExecutorTest {
    @Before
    public void setUp() {

    }

    @Test
    public void testStartActionAuth() {
        LoopPulseServiceExecutor.startActionAuth(Robolectric.application, "appID", "appToken");
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals(LoopPulseService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals(LoopPulseService.ActionType.AUTH.toString(), serviceIntent.getAction());
        assertEquals("appID", serviceIntent.getStringExtra(LoopPulseService.EXTRA_AUTH_APP_ID));
        assertEquals("appToken", serviceIntent.getStringExtra(LoopPulseService.EXTRA_AUTH_APP_TOKEN));
    }

    @Test
    public void testStartActionStartMonitoring() {
        LoopPulseServiceExecutor.startActionStartMonitoring(Robolectric.application);
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals(LoopPulseService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals(LoopPulseService.ActionType.START_MONITORING.toString(), serviceIntent.getAction());
    }

    @Test
    public void testStartActionStopMonitoring() {
        LoopPulseServiceExecutor.startActionStopMonitoring(Robolectric.application);
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals(LoopPulseService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals(LoopPulseService.ActionType.STOP_MONITORING.toString(), serviceIntent.getAction());
    }

    @Test
    public void testStartActionIdentifyUser() {
        LoopPulseServiceExecutor.startActionIdentifyUser(Robolectric.application, "ExternalID");
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals(LoopPulseService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals(LoopPulseService.ActionType.IDENTIFY_USER.toString(), serviceIntent.getAction());
        assertEquals("ExternalID", serviceIntent.getStringExtra(LoopPulseService.EXTRA_IDENTIFY_USER_EXTERNAL_ID));
    }
}
