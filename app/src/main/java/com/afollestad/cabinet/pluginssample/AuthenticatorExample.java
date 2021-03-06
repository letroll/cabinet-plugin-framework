package com.afollestad.cabinet.pluginssample;

import android.os.Bundle;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.plugins.PluginAuthenticator;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AuthenticatorExample extends PluginAuthenticator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticatorexample);

        // Simulates a 7 second loading period
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Persist a value that ServiceExample uses to check for authentication
                        PreferenceManager.getDefaultSharedPreferences(AuthenticatorExample.this)
                                .edit().putBoolean("is_authenticated", true).commit();
                        // Notify the service that authentication is complete
                        notifyAuthenticated();
                        // Close the Activity
                        finish();
                    }
                });
            }
        }).start();
    }
}
