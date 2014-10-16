package com.clionelabs.looppulse.sdk.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class LoopPulseReceiver extends WakefulBroadcastReceiver {
    private static String TAG = LoopPulseReceiver.class.getCanonicalName();

    public LoopPulseReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "LoopPulseReceiver onReceive(): " + intent.getAction());
        String action = intent.getAction();
        if (action.equals(LoopPulseService.RANGE_ACTION_INTENT)) {
            Intent service = new Intent(context, LoopPulseService.class);
            service.setAction(LoopPulseService.ActionType.DO_RANGE.toString());
            startWakefulService(context, service);
        }
    }
}
