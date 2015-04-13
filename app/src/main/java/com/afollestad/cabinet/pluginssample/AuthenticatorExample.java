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
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Simulates a 3 second loading period
                try {
                    Thread.sleep(3000);
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
