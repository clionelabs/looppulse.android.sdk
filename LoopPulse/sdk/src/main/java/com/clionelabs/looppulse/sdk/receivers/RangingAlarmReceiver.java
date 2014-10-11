package com.clionelabs.looppulse.sdk.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.clionelabs.looppulse.sdk.services.RangingService;

public class RangingAlarmReceiver extends WakefulBroadcastReceiver {
    private static String TAG = RangingAlarmReceiver.class.getCanonicalName();
    public static String RANGE_ACTION_INTENT = "com.clionelabs.looppulse.sdk.action.RANGE";

    public RangingAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "rangingAlarm onReceive()");
        Intent service = new Intent(context, RangingService.class);
        service.setAction(RangingService.ActionType.RANGE.toString());
        service.putExtra(RangingService.EXTRA_TRIGGER_CLASS, RangingAlarmReceiver.class.getName());
        startWakefulService(context, service);
    }

    public static void setAlarm(Context context, int delaySec) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(RANGE_ACTION_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delaySec * 1000, pi);
    }

    public static void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(RANGE_ACTION_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
    }
}
