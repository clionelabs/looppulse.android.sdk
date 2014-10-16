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
//public class MegaBoxApplication extends Application implements LoopPulseListener {
public class MegaBoxApplication extends Application implements LoopPulseListener {
    private static final String TAG = "MegaBoxApplication";
    private static String APPLICATION_ID = "28AuRvYh3vSA3Cueq";
    private static String APPLICATION_TOKEN = "5kmjyYLKvy2xqbuZNwqe";

//    private LoopPulse loopPulse;
    private LoopPulse loopPulse;

    public void onCreate() {
        super.onCreate();
//        loopPulse = new LoopPulse(this.getApplicationContext(), this, APPLICATION_TOKEN, APPLICATION_ID);
        loopPulse = new LoopPulse(this.getApplicationContext(), this);
        loopPulse.authenticate(APPLICATION_ID, APPLICATION_TOKEN);
    }

    private void testIdentifyUser() {
        loopPulse.identifyUser("external ABC");
//        loopPulse.identifyVisitorWithExternalId("external 123");
    }

    private void testMonitoring() {
        loopPulse.startMonitoring();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loopPulse.stopMonitoring();
            }
        }, 30 * 1000);
    }

    @Override
    public void onAuthenticated() {
        Log.d(TAG, "onAuthenticated()");
        testIdentifyUser();
        testMonitoring();
    }

    @Override
    public void onAuthenticationError(String msg) {
        Log.d(TAG, "onAuthenticationError: " + msg);
    }

    @Override
    public void onMonitoringStarted() {
        Log.d(TAG, "onMonitoringStarted");
    }

    @Override
    public void onMonitoringStopped() {
        Log.d(TAG, "onMonitoringStopped");
    }
}
