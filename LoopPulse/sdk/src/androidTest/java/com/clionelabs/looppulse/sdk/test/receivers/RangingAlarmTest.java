package com.clionelabs.looppulse.sdk.test.receivers;

import android.app.AlarmManager;
import android.content.Context;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowAlarmManager;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by hiukim on 2014-10-10.
 */

@RunWith(RobolectricTestRunner.class)
public class RangingAlarmTest {
//    private static String TAG = RangingAlarmReceiverTest.class.getCanonicalName();

    @Test
    public void testSetAlarm() {
//        RangingAlarmReceiver.setAlarm();

        AlarmManager alarmManager = (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        ShadowAlarmManager shadowAlarmManager = Robolectric.shadowOf(alarmManager);

//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        ShadowAlarmManager shadowAlarmManager = Robolectric.shadowOf(am);
//        Log.d(TAG, "scheduled count: " + shadowAlarmManager.getScheduledAlarms().size());
//        assertEquals(shadowAlarmManager.getScheduledAlarms().size(), 2);
    }

    @Test
    public void testSetAlarm2() throws Exception {

    }
}
