package com.clionelabs.looppulse.samples.megabox;

import android.app.Application;
import android.util.Log;

import com.clionelabs.looppulse.sdk.LoopPulse;
import com.clionelabs.looppulse.sdk.LoopPulseListener;

import java.util.Timer;
import java.util.TimerTask;

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
        loopPulse = new LoopPulse(this.getApplicationContext(), this, APPLICATION_TOKEN, APPLICATION_ID);
    }

    @Override
    public void onAuthenticated() {
        Log.d(TAG, "onAuthenticated()");
        loopPulse.startLocationMonitoring();
        loopPulse.identifyVisitorWithExternalId("external 123");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loopPulse.stopLocationMonitoring();
            }
        }, 10 * 1000);

//        for (String event: loopPulse.getAvailableEvents()) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
//                    new IntentFilter(event));
//        }
    }

    @Override
    public void onAuthenticationError(String msg) {
        Log.d(TAG, "onAuthenticationError: " + msg);
    }

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Got message: " + intent.getAction() + " uuid:" + intent.getStringExtra("uuid") + " major:" + intent.getIntExtra("major", -1) + " minor:" + intent.getIntExtra("minor", -1));
//        }
//    };
}
