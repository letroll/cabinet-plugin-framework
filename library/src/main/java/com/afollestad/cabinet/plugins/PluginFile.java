package com.afollestad.cabinet.plugins;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Used to store file objects that are sent from the plugin service to the main app.
 *
 * @author Aidan Follestad (afollestad)
 */
public class PluginFile implements Serializable {

    private static final String PACKAGE = "package";
    private static final String PATH = "path";
    private static final String THUMBNAIL = "thumbnail";

    private Bundle args;

    private PluginFile(Builder builder) {
        args = new Bundle();
        args.putString(PACKAGE, builder.packageName);
        args.putString(PATH, builder.path);
        args.putString(THUMBNAIL, builder.thumbnail);
    }

    public static class Builder {

        protected String packageName;
        protected String path;
        protected String thumbnail;

        public Builder(PluginService service) {
            packageName = service.getPackageName();
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public PluginFile build() {
            return new PluginFile(this);
        }
    }

    public Bundle getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return getArgs().toString();
    }
}
