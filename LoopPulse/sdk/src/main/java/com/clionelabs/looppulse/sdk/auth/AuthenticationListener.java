package com.clionelabs.looppulse.sdk.auth;

/**
 * Created by hiukim on 2014-10-16.
 */
public interface AuthenticationListener {
    public void onAuthenticationError(String msg);
    public void onAuthenticated();
}
