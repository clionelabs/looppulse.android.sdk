package com.clionelabs.looppulse.sdktest.services;

import android.content.Intent;

import com.clionelabs.looppulse.sdk.services.RangingService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

//import static org.mockito.Mockito.*;

/**
 * Created by hiukim on 2014-10-13.
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class RangingServiceTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testStartInitAction() {
        RangingService.startAction(Robolectric.application, RangingService.ActionType.INIT);

        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the RangingService service to be invoked",
                RangingService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected  Init Action",
                RangingService.ActionType.INIT.toString(), serviceIntent.getAction());
    }

    @Test
    public void testStartRangeAction() {
        RangingService.startAction(Robolectric.application, RangingService.ActionType.RANGE);
        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the RangingService service to be invoked",
                RangingService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected Range Action",
                RangingService.ActionType.RANGE.toString(), serviceIntent.getAction());
    }
}
