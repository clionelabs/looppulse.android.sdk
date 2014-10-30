package com.clionelabs.looppulse.sdk.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Due to the fact that many helpers class need to be setup asynchronously, and the
 * multiple LoopPulseService intents might arrived simultaneously, each of which require
 * a set of different helpers. This class is to alleviate the pain of acquiring relevant
 * helpers before the LoopPulseService intents are handled.
 *
 * TODO: Currently, we assume the onReady callback will be called from the helpers, but it might
 * not be true if errors occured inside the setup method of the helper class. We probably need to
 * implement a timeout mechanisms to return timeout error to the listeners.
 *
 * Created by hiukim on 2014-10-16.
 */
public class HelperAcquirer {
    private HashSet<Helper> readyHelpers;
    private HashSet<Helper> errorHelpers;
    private HashMap<HelperAcquirerListener, ArrayList<Helper>> acquirerListeners;
    private HashMap<Helper, Object> settingUpLocks;
    private HashMap<Helper, Boolean> isSettingUpFlags;

    public HelperAcquirer() {
        errorHelpers = new HashSet<Helper>();
        readyHelpers = new HashSet<Helper>();
        acquirerListeners = new HashMap<HelperAcquirerListener, ArrayList<Helper>>();
        settingUpLocks = new HashMap<Helper, Object>();
        isSettingUpFlags = new HashMap<Helper, Boolean>();
    }

    /**
     * Loop through all the helpers, and set it up (helper setup is asynchronous), if
     *      i) it's not already been setup before
     *      ii) A setting up call is undergoing
     *
     * We also put the listener on the waiting list (acquirerListeners), and we check
     * whether all the helpers are ready for that listener later, when any helper is
     * successfully setup.
     *
     * @param helpers List of required helpers
     * @param listener Listener for callback when all the required helpers are ready
     */
    public void acquireHelpers(ArrayList<Helper> helpers, HelperAcquirerListener listener) {
        acquirerListeners.put(listener, helpers);

        for (final Helper helper: helpers) {
            if (!settingUpLocks.containsKey(helper)) {
                settingUpLocks.put(helper, new Object());
            }
            if (!isSettingUpFlags.containsKey(helper)) {
                isSettingUpFlags.put(helper, false);
            }

            if (readyHelpers.contains(helper)) continue;
            if (getIsSettingUpFlag(helper)) continue;
            setIsSettingUpFlag(helper, true);

            helper.setup(new HelperListener() {
                @Override
                public void onReady() {
                    addReadyHelper(helper);
                    setIsSettingUpFlag(helper, false);
                }

                @Override
                public void onError() {
                    addErrorHelper(helper);
                    setIsSettingUpFlag(helper, false);
                }
            });
        }

        checkExistingListenerReady();
    }

    private boolean getIsSettingUpFlag(Helper helper) {
        synchronized (settingUpLocks.get(helper)) {
            return isSettingUpFlags.get(helper);
        }
    }
    private void setIsSettingUpFlag(Helper helper, boolean isSettingUp) {
        synchronized (settingUpLocks.get(helper)) {
            isSettingUpFlags.put(helper, isSettingUp);
        }
    }

    private void addReadyHelper(Helper helper) {
        readyHelpers.add(helper);
        checkExistingListenerReady();
    }

    private void addErrorHelper(Helper helper) {
        errorHelpers.add(helper);
        checkExistingListenerReady();
    }

    /**
     * Loop through all the awaiting listeners. Trigger onReady callback and remove them
     * when all the helpers are ready, or at least one has error.
     */
    private void checkExistingListenerReady() {
        Iterator<HelperAcquirerListener> it = acquirerListeners.keySet().iterator();
        while (it.hasNext()) {
            HelperAcquirerListener listener = it.next();
            boolean isAllReady = true;
            boolean hasError = false;
            for (Helper h: acquirerListeners.get(listener)) {
                if (!readyHelpers.contains(h)) {
                    isAllReady = false;
                }
                if (errorHelpers.contains(h)) {
                    hasError = true;
                }
            }
            if (isAllReady) {
                listener.onReady();
                it.remove();
            }
            if (hasError) {
                listener.onError();
                it.remove();
            }
        }
    }
}
