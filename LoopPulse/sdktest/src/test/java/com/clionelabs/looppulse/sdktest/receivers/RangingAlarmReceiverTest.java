package com.clionelabs.looppulse.sdktest.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.clionelabs.looppulse.sdk.receivers.RangingAlarmReceiver;
import com.clionelabs.looppulse.sdk.services.RangingService;
import com.clionelabs.looppulse.sdktest.SDKTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.ShadowApplication;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 * Created by hiukim on 2014-10-10.
 */

@RunWith(SDKTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class RangingAlarmReceiverTest {
    AlarmManager alarmManager;
    ShadowAlarmManager shadowAlarmManager;

    @Before
    public void setUp() {
        alarmManager = (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        shadowAlarmManager = Robolectric.shadowOf(alarmManager);
        assertEquals(0, shadowAlarmManager.getScheduledAlarms().size());
    }

    @Test
    public void testReceiverRegistered() {
        List<ShadowApplication.Wrapper> registeredReceivers = Robolectric.getShadowApplication().getRegisteredReceivers();

        assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound)
                receiverFound = RangingAlarmReceiver.class.getSimpleName().equals(wrapper.broadcastReceiver.getClass().getSimpleName());
        }
        assertTrue(receiverFound); //will be false if not found
    }

    @Test
    public void testIntentHandling() {
        /** TEST 1
         ----------
         We defined the Broadcast receiver with a certain action, so we should check if we have
         receivers listening to the defined action
         */
        Intent intent = new Intent(RangingAlarmReceiver.RANGE_ACTION_INTENT);

        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        assertTrue(shadowApplication.hasReceiverForIntent(intent));

        /**
         * TEST 2
         * ----------
         * Lets be sure that we only have a single receiver assigned for this intent
         */
        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertEquals("Expected one broadcast receiver", 1, receiversForIntent.size());

        /**
         * TEST 3
         * ----------
         * Fetch the Broadcast receiver and cast it to the correct class.
         * Next call the "onReceive" method and check if the MyBroadcastIntentService was started
         */
        RangingAlarmReceiver receiver = (RangingAlarmReceiver) receiversForIntent.get(0);
        receiver.onReceive(Robolectric.getShadowApplication().getApplicationContext(), intent);

        Intent serviceIntent = Robolectric.getShadowApplication().peekNextStartedService();
        assertEquals("Expected the RangingService service to be invoked",
                RangingService.class.getCanonicalName(), serviceIntent.getComponent().getClassName());
        assertEquals("Expected Range Action",
                RangingService.ActionType.RANGE.toString(), serviceIntent.getAction());
    }

    @Test
    public void testSetAndCancelAlarm() {
        // Set
        RangingAlarmReceiver.setAlarm(Robolectric.application, 0);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        // Cancel
        RangingAlarmReceiver.cancelAlarm(Robolectric.application);
        assertEquals(0, shadowAlarmManager.getScheduledAlarms().size());
    }

    @Test
    public void testOverrideAlarm() {
        // Set as 100 seconds
        RangingAlarmReceiver.setAlarm(Robolectric.application, 100);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertEquals(scheduledAlarm.type, AlarmManager.RTC_WAKEUP);
        assertTrue((new Date()).getTime() / 1000 + 100 - scheduledAlarm.triggerAtTime / 1000 <= 1);

        // Reset as 1000 seconds
        RangingAlarmReceiver.setAlarm(Robolectric.application, 1000);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertEquals(scheduledAlarm.type, AlarmManager.RTC_WAKEUP);
        assertTrue((new Date()).getTime() / 1000 + 1000 - scheduledAlarm.triggerAtTime / 1000 <= 1);

        // Reset as 10 seconds
        RangingAlarmReceiver.setAlarm(Robolectric.application, 10);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());

        scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertEquals(scheduledAlarm.type, AlarmManager.RTC_WAKEUP);
        assertTrue((new Date()).getTime() / 1000 + 10 - scheduledAlarm.triggerAtTime / 1000 <= 1);
    }
}
