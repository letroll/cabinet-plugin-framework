package com.afollestad.cabinet.plugins;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to store file objects that are sent from the plugin service to the main app.
 *
 * @author Aidan Follestad (afollestad)
 */
public class PluginFile implements Parcelable {

    public static final String PACKAGE = "package";
    public static final String PATH = "path";
    public static final String THUMBNAIL = "thumbnail";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    public static final String IS_DIR = "is_dir";
    public static final String LENGTH = "length";
    public static final String HIDDEN = "hidden";

    private Bundle args;

    public PluginFile() {
    }

    private PluginFile(Builder builder) {
        args = new Bundle();
        args.putString(PACKAGE, builder.packageName);
        args.putString(PATH, builder.path);
        args.putString(THUMBNAIL, builder.thumbnail);
        args.putLong(CREATED, builder.created);
        args.putLong(MODIFIED, builder.modified);
        args.putBoolean(IS_DIR, builder.isDir);
        args.putLong(LENGTH, builder.length);
        args.putBoolean(HIDDEN, builder.hidden);
    }

    public PluginFile(Parcel in) {
        args = in.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(args);
    }

    public static final Parcelable.Creator<PluginFile> CREATOR = new
            Parcelable.Creator<PluginFile>() {
                public PluginFile createFromParcel(Parcel in) {
                    return new PluginFile(in);
                }

                public PluginFile[] newArray(int size) {
                    return new PluginFile[size];
                }
            };


    public static class Builder {

        protected String packageName;
        protected String path;
        protected String thumbnail;
        protected long created;
        protected long modified;
        protected boolean isDir;
        protected long length;
        protected boolean hidden;

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

        public Builder length(long length) {
            this.length = length;
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public PluginFile build() {
            return new PluginFile(this);
        }
    }

    public String getString(String key) {
        return getArgs().getString(key);
    }

    public long getLong(String key) {
        return getArgs().getLong(key);
    }

    public boolean getBoolean(String key) {
        return getArgs().getBoolean(key);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return getArgs().getBoolean(key, fallback);
    }

    public Bundle getArgs() {
        return args;
    }

    @Override
    public String toString() {
        if (getArgs() != null)
            return args.getString(PATH) + " (is_dir=" + args.getBoolean(IS_DIR) + ")";
        return "(null)";
    }
}
