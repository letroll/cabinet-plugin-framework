package com.afollestad.cabinet.plugins;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A PluginService is the heart of a plugin. It's started when a user of Cabinet taps on your plugin
 * in the navigation drawer, and it's responsible for listing files and performing other file actions.
 * <p/>
 * If you return something greater than zero for getForegroundId(), the Service will run in foreground
 * mode with a persistent notification until the user explicitly taps exit or you stop the service from within.
 * Tapping the notification will bring the user to your plugin in Cabinet's main UI, also.
 *
 * @author Aidan Follestad (afollestad)
 */
public abstract class PluginService extends Service {

    private final static String EXIT_ACTION = "com.afollestad.cabinet.plugins.EXIT";
    private Messenger mMessenger;

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                PluginAction action = PluginAction.valueOf(msg.what);
                switch (action) {
                    case CONNECT: {
                        mMessenger = msg.replyTo;
                        tryConnect();
                        break;
                    }
                    case LIST: {
                        if (msg.getData() == null || !msg.getData().containsKey("path"))
                            throw new Exception("Expected data containing a path");
                        String path = msg.getData().get("path").toString();
                        ArrayList<PluginFile> results = listFiles(path);
                        msg.getData().putParcelableArrayList("results", results);
                        respond(msg.replyTo, PluginAction.LIST, msg.getData());
                        break;
                    }
                    case CREATE: {
                        if (msg.getData() == null || !msg.getData().containsKey("path"))
                            throw new Exception("Expected data containing a type and path");
                        String path = msg.getData().get("path").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            newFile(path);
                        else
                            mkDir(path);
                        respond(msg.replyTo, PluginAction.CREATE, msg.getData());
                        break;
                    }
                    case COPY: {
                        if (msg.getData() == null || !msg.getData().containsKey("source") || !msg.getData().containsKey("dest"))
                            throw new Exception("Expected data containing a source and dest");
                        String source = msg.getData().get("source").toString();
                        String dest = msg.getData().get("dest").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            copyFile(source, dest);
                        else
                            copyDir(source, dest);
                        respond(msg.replyTo, PluginAction.COPY, msg.getData());
                        break;
                    }
                    case MOVE: {
                        if (msg.getData() == null || !msg.getData().containsKey("source") || !msg.getData().containsKey("dest"))
                            throw new Exception("Expected data containing a source and dest");
                        String source = msg.getData().get("source").toString();
                        String dest = msg.getData().get("dest").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            moveFile(source, dest);
                        else
                            moveDir(source, dest);
                        respond(msg.replyTo, PluginAction.MOVE, msg.getData());
                        break;
                    }
                    case DELETE: {
                        if (msg.getData() == null || !msg.getData().containsKey("path"))
                            throw new Exception("Expected data containing a type and path");
                        String path = msg.getData().get("path").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            rmFile(path);
                        else
                            rmDir(path);
                        respond(msg.replyTo, PluginAction.DELETE, msg.getData());
                        break;
                    }
                    case DISCONNECT: {
                        mMessenger = msg.replyTo;
                        performDisconnect();
                        break;
                    }
                }
            } catch (Exception e) {
                log("Receive Error: " + e.getLocalizedMessage());
                if (msg.replyTo != null)
                    error(msg.replyTo, e.getLocalizedMessage());
            }
        }
    }

    private final Messenger mReceiver = new Messenger(new MessageHandler());
    private final static boolean DEBUG = true;

    private void log(String message) {
        if (DEBUG)
            Log.d("PluginService", message);
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return mReceiver.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            log("Received: " + intent.getAction());
            if (intent.getAction() != null) {
                if (PluginAuthenticator.AUTHENTICATED_ACTION.equals(intent.getAction())) {
                    // Authentication was finished, connect now
                    performConnect();
                } else if (EXIT_ACTION.equals(intent.getAction())) {
                    performDisconnect();
                }
            }
        } else {
            log("Received: null intent");
        }
        return START_STICKY;
    }

    private void tryConnect() {
        if (authenticationNeeded()) {
            // Authentication needed
            refreshNotification(getString(R.string.authenticating));
            startActivity(getAuthenticatorIntent());
        } else {
            // Authentication not needed, connect now
            performConnect();
        }
    }

    private void performConnect() {
        refreshNotification(getString(R.string.connecting));
        try {
            connect();
            respond(mMessenger, PluginAction.CONNECT, null);
            refreshNotification(getString(R.string.connected));
        } catch (Exception e) {
            log("Connect error: " + e.getLocalizedMessage());
            if (mMessenger != null)
                error(mMessenger, e.getLocalizedMessage());
            refreshNotification(getString(R.string.connect_error));
        }
    }

    private void performDisconnect() {
        try {
            disconnect();
            if (mMessenger != null)
                respond(mMessenger, PluginAction.DISCONNECT, null);
            stopForeground(true);
            stopSelf();
            sendBroadcast(new Intent(EXIT_ACTION));
        } catch (Exception e) {
            log("Disconnect error: " + e.getLocalizedMessage());
            if (mMessenger != null)
                error(mMessenger, e.getLocalizedMessage());
        }
    }

    private void refreshNotification(String status) {
        if (status == null)
            status = getString(R.string.disconnected);
        if (getForegroundId() > 0) {
            try {
                PackageManager pm = getPackageManager();
                ServiceInfo info = pm.getServiceInfo(getComponentName(), PackageManager.GET_SERVICES);
                PendingIntent mainIntent = PendingIntent.getActivity(this, 1001,
                        new Intent(Intent.ACTION_MAIN)
                                .setComponent(new ComponentName("com.afollestad.cabinet", "com.afollestad.cabinet.ui.MainActivity"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        PendingIntent.FLAG_CANCEL_CURRENT);
                PendingIntent exitIntent = PendingIntent.getService(this, 1002,
                        new Intent(EXIT_ACTION).setComponent(getComponentName()),
                        PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setContentTitle(info.loadLabel(pm))
                        .setContentText(status)
                        .setSmallIcon(info.getIconResource())
                        .setContentIntent(mainIntent)
                        .addAction(R.drawable.ic_stat_navigation_close,
                                getString(R.string.exit), exitIntent);
                startForeground(getForegroundId(), builder.build());
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected abstract void connect() throws Exception;

    protected abstract ArrayList<PluginFile> listFiles(String path) throws Exception;

    protected abstract void newFile(String path) throws Exception;

    protected abstract void mkDir(String path) throws Exception;

    protected abstract void copyFile(String source, String dest) throws Exception;

    protected abstract void copyDir(String source, String dest) throws Exception;

    protected abstract void moveFile(String source, String dest) throws Exception;

    protected abstract void moveDir(String source, String dest) throws Exception;

    protected abstract void rmFile(String path) throws Exception;

    protected abstract void rmDir(String path) throws Exception;

    protected abstract void disconnect() throws Exception;

    /**
     * If you're using a web service like Google Drive, you'll need to authenticate the user before
     * files can be accessed. Return true if the user is not already authenticated, the Intent returned
     * in authenticator() will be launched as an Activity.
     */
    protected abstract boolean authenticationNeeded();

    /**
     * Return an Intent that can be used to start your Authenticator Activity.
     */
    protected abstract Intent authenticator();

    /**
     * Generally, just return 'this'. Used to get the context, package name, etc. of your plugin
     * rather than the context if the plugin framework library.
     */
    protected abstract PluginService getService();

    /**
     * The ID used for the persistent foreground notification that keeps the service running
     * indefinitely until disconnection. If you have multiple services in your plugin, each need a
     * unique ID.
     * <p/>
     * If you <= 0, the service will not go into foreground mode. Without foreground mode, no
     * persistent notification is displayed, but the Service will be eventually killed by the system.
     */
    protected abstract int getForegroundId();

    private ComponentName getComponentName() {
        PluginService service = getService();
        return new ComponentName(service.getPackageName(), service.getClass().getName());
    }

    private Intent getAuthenticatorIntent() {
        return authenticator()
                .putExtra(PluginAuthenticator.TARGET_COMPONENT, getComponentName().flattenToString())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void error(Messenger responder, String err) {
        Bundle args = new Bundle();
        args.putString("error", err);
        respond(responder, PluginAction.ERROR, args);
    }

    private void respond(Messenger responder, PluginAction action, Bundle data) {
        if (responder == null)
            throw new IllegalStateException("Plugin service is not connected.");
        Message msg = Message.obtain(null, action.value());
        if (data != null)
            msg.setData(data);
        try {
            responder.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}