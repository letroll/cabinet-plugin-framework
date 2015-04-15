package com.afollestad.cabinet.plugins;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * An Authenticator is an Activity invoked by a PluginService when authentication is needed. The
 * Authenticator is responsible for displaying any necessary login UI and persisting values that are
 * used by the Service to verify authentication.
 * <p/>
 * For an example, a Google Drive plugin could using Google Play Services to authenticate the user
 * with a Google account. The account token would be saved in SharedPreferences for use in the plugin's
 * main service.
 *
 * @author Aidan Follestad (afollestad)
 */
public class PluginAuthenticator extends ActionBarActivity {

    private String targetComponent;

    public final static String TARGET_COMPONENT = "target_component";
    public final static String AUTHENTICATED_ACTION = "com.afollestad.cabinet.plugins.AUTHENTICATED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        targetComponent = getIntent().getStringExtra(TARGET_COMPONENT);
        if (targetComponent == null || targetComponent.trim().isEmpty())
            throw new IllegalStateException("No target component set for PluginAuthenticator.");
    }

    public final void notifyAuthenticated() {
        // Notifies target service that authentication is complete
        Intent serviceIntent = new Intent(AUTHENTICATED_ACTION)
                .setComponent(ComponentName.unflattenFromString(targetComponent));
        startService(serviceIntent);
    }
}
