package com.clionelabs.lighthouse;

/**
 * Created by simon on 30/5/14.
 */

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.ibeaconreference.RangingActivity;
import com.radiusnetworks.proximity.ibeacon.powersave.BackgroundPowerSaver;
import com.radiusnetworks.proximity.ibeacon.startup.BootstrapNotifier;
import com.radiusnetworks.proximity.ibeacon.startup.RegionBootstrap;

public class LighHouseApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "AndroidProximityReferenceApplication";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedIBeaconsSinceBoot = false;


    public void onCreate() {
        super.onCreate();

        IBeaconManager.LOG_DEBUG = true;

        com.radiusnetworks.proximity.ibeacon.IBeaconManager.getInstanceForApplication(this).setBackgroundBetweenScanPeriod(5000);

        Log.d(TAG, "setting up background monitoring for iBeacons and power saving");
        // wake up the app when an iBeacon is seen
        Region region = new Region("com.clionelabs.estimote",
                "B9407F30-F5F8-466E-AFF9-25556B57FE6D", null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the iBeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // This method is not used in this example
        Log.d(TAG, "didDetermineStateForRegion");
    }

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever an iBeacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region.");
        if (!haveDetectedIBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            // The very first time since boot that we detect an iBeacon, we launch the
            // MainActivity
            Intent intent = new Intent(this, RangingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent);
            haveDetectedIBeaconsSinceBoot = true;
        } else {
            // If we have already seen iBeacons and launched the MainActivity before, we simply
            // send a notification to the user on subsequent detections.
            Log.d(TAG, "Sending notification.");
            sendNotification();
        }


    }

    @Override
    public void didExitRegion(Region arg0) {
        Log.d(TAG, "exited region");
    }

    private void sendNotification() {
        Notification.Builder builder =
                new Notification.Builder(this)
                        .setContentTitle("Proximity Reference Application")
                        .setContentText("An iBeacon is nearby.")
                        .setSmallIcon(R.drawable.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, RangingActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
