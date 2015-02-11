package com.clionelabs.looppulse.samples.tw91mai;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clionelabs.looppulse.sdk.LoopPulse;
import com.clionelabs.looppulse.sdk.LoopPulseListener;
import com.clionelabs.looppulse.sdk.datastore.BeaconEvent;


public class MainActivity extends Activity implements LoopPulseListener {
    private static final String TAG = "MegaBoxApplication";
    private static String APPLICATION_ID = "wMr6w5prRpYt5A7xQ";
    private static String APPLICATION_TOKEN = "wefijoweifj";
    private LoopPulse loopPulse;
    private boolean isLoopPulseAuthenticated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loopPulse = new LoopPulse(this, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        } else if (id == R.id.action_start_monitoring) {
            loopPulse.authenticate(APPLICATION_ID, APPLICATION_TOKEN);
        } else if (id == R.id.action_stop_monitoring) {
            loopPulse.stopLocationMonitoring();
        }

        return super.onOptionsItemSelected(item);
    }

    private void addEventLabel(String msg) {
        TextView textView = new TextView(this);
        textView.setText(msg);
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainView);
        mainLayout.addView(textView);
        mainLayout.addView(new TextView(this));
    }

    @Override
    public void onAuthenticated() {
        addEventLabel("onAuthenticated");
        isLoopPulseAuthenticated = true;
        loopPulse.startLocationMonitoring();
    }

    @Override
    public void onAuthenticationError(String msg) {
        addEventLabel("onAuthenticationError: " + msg);
    }

    @Override
    public void onMonitoringStarted() {
        addEventLabel("onMonitoringStarted");
    }

    @Override
    public void onMonitoringStopped() {
        addEventLabel("onMonitoringStopped");
    }

    @Override
    public void onBeaconDetected(BeaconEvent event) {
        addEventLabel(event.toFirebaseObject().toString());
    }
}
