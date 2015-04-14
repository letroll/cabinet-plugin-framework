package com.afollestad.cabinet.plugins;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Used to store file objects that are sent from the plugin service to the main app.
 *
 * @author Aidan Follestad (afollestad)
 */
public class PluginFile {

    public static final String PACKAGE = "package";
    public static final String PATH = "path";
    public static final String THUMBNAIL = "thumbnail";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    public static final String IS_DIR = "is_dir";

    private Bundle args;

    private PluginFile(Builder builder) {
        args = new Bundle();
        args.putString(PACKAGE, builder.packageName);
        args.putString(PATH, builder.path);
        args.putString(THUMBNAIL, builder.thumbnail);
        args.putLong(CREATED, builder.created);
        args.putLong(MODIFIED, builder.modified);
        args.putBoolean(IS_DIR, builder.isDir);
    }

    public PluginFile(Bundle args) {
        this.args = args;
    }

    public static ArrayList<Bundle> bundleArrayFromFileArray(ArrayList<PluginFile> files) {
        ArrayList<Bundle> results = new ArrayList<>();
        for (PluginFile fi : files)
            results.add(fi.getArgs());
        return results;
    }

    public static class Builder {

        protected String packageName;
        protected String path;
        protected String thumbnail;
        protected long created;
        protected long modified;
        protected boolean isDir;

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

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }

        public Builder isDir(boolean isDir) {
            this.isDir = isDir;
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
