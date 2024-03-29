package com.clionelabs.looppulse.samples.megabox;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clionelabs.looppulse.sdk.LoopPulse;
import com.clionelabs.looppulse.sdk.LoopPulseListener;
import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity implements LoopPulseListener {
    private static final String TAG = "MegaBoxApplication";
    private static String APPLICATION_ID = "5of345ljkfaLKJKJL";
    private static String APPLICATION_TOKEN = "34LKJ043nkjajoifuer9";
    private LoopPulse loopPulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loopPulse = new LoopPulse(this, this);
        loopPulse.authenticate(APPLICATION_ID, APPLICATION_TOKEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_start_monitoring) {
            loopPulse.startLocationMonitoring();
        }

        if (id == R.id.action_stop_monitoring) {
            loopPulse.stopLocationMonitoring();
        }

        return super.onOptionsItemSelected(item);
    }

    private void testIdentifyUser() {
        loopPulse.identifyUser("external ABC");
    }

    private void testTagVisitor() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        loopPulse.tagVisitor(properties);
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
        addEventLabel("onAuthenticated");
        testIdentifyUser();
        testTagVisitor();
        testMonitoring();
    }

    @Override
    public void onAuthenticationError(String msg) {
        Log.d(TAG, "onAuthenticationError: " + msg);
        addEventLabel("onAuthenticationError: " + msg);
    }

    @Override
    public void onMonitoringStarted() {
        Log.d(TAG, "onMonitoringStarted");
        addEventLabel("noMonitoringStarted");
    }

    @Override
    public void onMonitoringStopped() {
        Log.d(TAG, "onMonitoringStopped");
        addEventLabel("onMonitoringStopped");
    }

    @Override
    public void onBeaconDetected(BeaconEvent event) {
        Log.d(TAG, "onBeaconDetected: " + event);
        addEventLabel(event.toFirebaseObject().toString());
    }

    private void addEventLabel(String msg) {
        TextView textView = new TextView(this);
        textView.setText(msg);
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainView);
        mainLayout.addView(textView);
    }
}
