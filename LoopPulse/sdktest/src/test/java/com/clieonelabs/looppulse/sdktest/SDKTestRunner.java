package com.clieonelabs.looppulse.sdktest;

import com.clionelabs.looppulse.sdk.LoopPulse;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by hiukim on 2014-10-11.
 */
public class SDKTestRunner extends RobolectricTestRunner {
    public SDKTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        ShadowLog.stream = System.out;
    }

    @Override protected AndroidManifest getAppManifest(Config config) {
        //Any better way to specify the ManiFest file path?
        String lpClassFilePath = LoopPulse.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String myAppPath = lpClassFilePath.substring(0, lpClassFilePath.lastIndexOf("/"));
        String manifestPath = myAppPath + "/AndroidManifest.xml";
        String resPath = myAppPath + "/res";
        String assetPath = myAppPath + "/assets";
        return createAppManifest(Fs.fileFromPath(manifestPath), Fs.fileFromPath(resPath), Fs.fileFromPath(assetPath));
    }
}
