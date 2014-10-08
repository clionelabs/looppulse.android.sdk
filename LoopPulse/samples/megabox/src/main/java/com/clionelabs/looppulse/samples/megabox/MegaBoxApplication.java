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

    private void testIdentifyUser() {
        loopPulse.identifyVisitorWithExternalId("external 123");
    }

    private void testMonitoring() {
        loopPulse.startLocationMonitoring();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loopPulse.stopLocationMonitoring();
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
}
