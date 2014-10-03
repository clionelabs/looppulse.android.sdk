package com.clieonelabs.looppulse.samples.megabox;

import android.app.Application;
import android.util.Log;

import com.clieonelabs.looppulse.sdk.LoopPulse;
import com.clieonelabs.looppulse.sdk.LoopPulseListener;

/**
 * Created by hiukim on 2014-10-03.
 */
public class MegaBoxApplication extends Application implements LoopPulseListener {
    private static final String TAG = "MegaBoxApplication";
    private static String APPLICATION_ID = "28AuRvYh3vSA3Cueq";
    private static String APPLICATION_TOKEN = "5kmjyYLKvy2xqbuZNwqe";

    private LoopPulse loopPulse;

    public void onCreate() {
        super.onCreate();
        loopPulse = new LoopPulse(this, this, APPLICATION_TOKEN, APPLICATION_ID);
    }

    @Override
    public void didAuthenticated() {
        Log.d(TAG, "didAuthenticated()");
        loopPulse.startLocationMonitoringAndRanging();

//        for (String event: loopPulse.getAvailableEvents()) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
//                    new IntentFilter(event));
//        }
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Got message: " + intent.getAction() + " uuid:" + intent.getStringExtra("uuid") + " major:" + intent.getIntExtra("major", -1) + " minor:" + intent.getIntExtra("minor", -1));
//        }
//    };
}
