package com.clionelabs.looppulse.sdk.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hiukim on 2014-10-16.
 */
public class LoopPulseServiceExecutor {
    public static void startActionAuth(Context context, String appID, String appToken) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.AUTH.toString());
        intent.putExtra(LoopPulseService.EXTRA_AUTH_APP_ID, appID);
        intent.putExtra(LoopPulseService.EXTRA_AUTH_APP_TOKEN, appToken);
        context.startService(intent);
    }

    public static void startActionStartMonitoring(Context context) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.START_MONITORING.toString());
        context.startService(intent);
    }

    public static void startActionStopMonitoring(Context context) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.STOP_MONITORING.toString());
        context.startService(intent);
    }

    public static void startActionIdentifyUser(Context context, String externalID) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.IDENTIFY_USER.toString());
        intent.putExtra(LoopPulseService.EXTRA_IDENTIFY_USER_EXTERNAL_ID, externalID);
        context.startService(intent);
    }

    public static void setRangeAlarm(Context context, int delaySec) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(LoopPulseService.RANGE_ACTION_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delaySec * 1000, pi);
    }

    public static void cancelRangeAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(LoopPulseService.RANGE_ACTION_INTENT);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
    }
}
