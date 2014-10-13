package com.clionelabs.looppulse.sdktest.services;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by hiukim on 2014-10-14.
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20

public class DataStoreServiceHelperTest {

    //TODO: testInit (Constructor)

    //TODO: testCreateFirebaseBeaconEvent

    //TODO: testCreateFirebaseVisitorIdentifyEvent
}
