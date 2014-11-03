package com.clionelabs.looppulse.sdk.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.HashMap;

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

    public static void startActionIdentifyVisitor(Context context, String externalID) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.IDENTIFY_VISITOR.toString());
        intent.putExtra(LoopPulseService.EXTRA_IDENTIFY_VISITOR_EXTERNAL_ID, externalID);
        context.startService(intent);
    }

    public static void startActionTagVisitor(Context context, HashMap<String, String> properties) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.TAG_VISITOR.toString());
        intent.putExtra(LoopPulseService.EXTRA_TAG_VISITOR_PROPERTIES, properties);
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

    public static void setGeofenceEventTrigger(Context context, PendingIntent geofencePendingIntent, LocationClient mLocationClient, ArrayList<Geofence> geofences, LocationClient.OnAddGeofencesResultListener listener) {
        Intent intent = new Intent(context, LoopPulseService.class);
        intent.setAction(LoopPulseService.ActionType.ENTER_GEOFENCE.toString());
        geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationClient.addGeofences(geofences, geofencePendingIntent, listener);
    }
}
