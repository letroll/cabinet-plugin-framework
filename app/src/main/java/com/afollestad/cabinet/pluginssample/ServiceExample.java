package com.afollestad.cabinet.pluginssample;

import android.preference.PreferenceManager;

import com.afollestad.cabinet.plugins.PluginAuthenticator;
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
    protected void copyFile(String source, String dest) {

    }

    @Override
    protected void copyDir(String source, String dest) {

    }

    @Override
    protected void moveFile(String source, String dest) {

    }

    @Override
    protected void moveDir(String source, String dest) {

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
    protected Class<? extends PluginAuthenticator> authenticator() {
        return AuthenticatorExample.class;
    }
}
