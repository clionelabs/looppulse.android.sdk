package com.clionelabs.looppulse.sdktest.services;

import com.clionelabs.looppulse.sdk.services.RangingServiceHelper;
import com.clionelabs.looppulse.sdk.services.RangingServiceHelperListener;
import com.clionelabs.looppulse.sdk.monitor.RangingStatus;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.fail;

/**
 * Created by hiukim on 2014-10-14.
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18) // Robolectric only support SDK up to 18 at this moment, but our SDK use 20
public class RangingServiceHelperTest {
    Region region;
    BeaconManager beaconManagerMock;
    RangingStatus rangingStatusMock;
    RangingServiceHelperListener listenerMock;
    boolean hasFinishedInit = false;
    boolean hasFinishedRanged = false;

    @Before
    public void setUp() {
        region = new Region("LoopPulse-Generic", null, null, null);
        beaconManagerMock = Mockito.mock(BeaconManager.class);
        rangingStatusMock = Mockito.mock(RangingStatus.class);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BeaconManager.ServiceReadyCallback callback = (BeaconManager.ServiceReadyCallback) invocation.getArguments()[0];
                callback.onServiceReady();
                return null;
            }
        }).when(beaconManagerMock).connect(Mockito.any(BeaconManager.ServiceReadyCallback.class));
        hasFinishedInit = false;
        hasFinishedRanged = false;
    }

    @Test
    public void testInit() throws InterruptedException {
        RangingServiceHelperListener listener = new RangingServiceHelperListener() {
            @Override
            public void onFinishedInit() {
                hasFinishedInit = true;
            }

            @Override
            public void onFinishedRanging() {

            }
        };

        RangingServiceHelper helper = new RangingServiceHelper(Robolectric.application, region, beaconManagerMock, rangingStatusMock, listener);

        // Any better way to wait until callback finish?
        helperWaitUntilFinishInit();

        Mockito.verify(beaconManagerMock).connect(Mockito.any(BeaconManager.ServiceReadyCallback.class));
    }

    private void helperWaitUntilFinishInit() throws InterruptedException {
        // Any better way to wait until callback finish?
        int sleepSec = 0;
        while(!hasFinishedInit){
            Thread.sleep(100);
            sleepSec += 100;
            if (sleepSec > 1000) {
                fail("Not receiving onFinish callback");
                break;
            }
        }
    }
}
