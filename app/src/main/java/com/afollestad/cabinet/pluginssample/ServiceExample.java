package com.afollestad.cabinet.pluginssample;

import android.content.Intent;
import android.preference.PreferenceManager;

import com.afollestad.cabinet.plugins.PluginFile;
import com.afollestad.cabinet.plugins.PluginService;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ServiceExample extends PluginService {

    @Override
    protected void connect() throws Exception {

    }

    @Override
    protected List<PluginFile> listFiles(String path) throws Exception {
        return null;
    }

    @Override
    protected void newFile(String path) throws Exception {

    }

    @Override
    protected void mkDir(String path) throws Exception {

    }

    @Override
    protected void copyFile(String source, String dest) throws Exception {

    }

    @Override
    protected void copyDir(String source, String dest) throws Exception {

    }

    @Override
    protected void moveFile(String source, String dest) throws Exception {

    }

    @Override
    protected void moveDir(String source, String dest) throws Exception {

    }

    @Override
    protected void rmFile(String path) throws Exception {

    }

    @Override
    protected void rmDir(String path) throws Exception {

    }

    @Override
    protected void disconnect() throws Exception {

    }

    @Override
    protected boolean authenticationNeeded() {
        return !PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("is_authenticated", false);
    }

    @Override
    protected Intent authenticator() {
        return new Intent(this, AuthenticatorExample.class);
    }

    @Override
    protected PluginService getService() {
        return this;
    }

    @Override
    protected int getForegroundId() {
        return 4001;
    }
}
