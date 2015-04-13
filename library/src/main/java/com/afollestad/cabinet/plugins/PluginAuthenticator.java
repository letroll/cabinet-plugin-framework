package com.afollestad.cabinet.plugins;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
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
