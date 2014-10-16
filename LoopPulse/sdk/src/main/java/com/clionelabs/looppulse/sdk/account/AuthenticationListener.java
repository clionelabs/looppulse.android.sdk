package com.clionelabs.looppulse.sdk.account;

/**
 * Created by hiukim on 2014-10-16.
 */
public interface AuthenticationListener {
    public void onAuthenticationError(String msg);
    public void onAuthenticated();
}
