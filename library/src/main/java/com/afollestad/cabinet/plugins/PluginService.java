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

import java.util.List;

/**
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
                        if (authenticationNeeded()) {
                            // Authentication needed
                            startActivity(getAuthenticatorIntent());
                        } else {
                            // Authentication not needed, connect now
                            performConnect();
                        }
                        break;
                    }
                    case LIST: {
                        if (msg.getData() == null || !msg.getData().containsKey("path"))
                            throw new Exception("Expected data containing a path");
                        String path = msg.getData().get("path").toString();
                        List<PluginFile> results = listFiles(path);
                        respond(msg.replyTo, PluginAction.LIST, results, msg.getData());
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
                        respond(msg.replyTo, PluginAction.CREATE, null, msg.getData());
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
                        respond(msg.replyTo, PluginAction.COPY, null, msg.getData());
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
                        respond(msg.replyTo, PluginAction.MOVE, null, msg.getData());
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
                        respond(msg.replyTo, PluginAction.DELETE, null, msg.getData());
                        break;
                    }
                    case DISCONNECT:
                        disconnect();
                        respond(msg.replyTo, PluginAction.DISCONNECT, null, null);
                        stopForeground(true);
                        stopSelf();
                        break;
                }
            } catch (Exception e) {
                log("Receive Error: " + e.getLocalizedMessage());
                if (msg.replyTo != null)
                    respond(msg.replyTo, PluginAction.ERROR, e.getLocalizedMessage(), null);
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
            if (intent.getAction() != null &&
                    PluginAuthenticator.AUTHENTICATED_ACTION.equals(intent.getAction())) {
                // Authentication was finished, connect now
                performConnect();
            }
        } else {
            log("Received: null intent");
        }
        return START_STICKY;
    }

    private void performConnect() {
        refreshNotification(getString(R.string.connecting));
        try {
            connect();
            respond(mMessenger, PluginAction.CONNECT, null, null);
            refreshNotification(getString(R.string.connected));
        } catch (Exception e) {
            respond(mMessenger, PluginAction.ERROR, e.getLocalizedMessage(), null);
        }
        mMessenger = null;
    }

    private void refreshNotification(String status) {
        if (status == null)
            status = getString(R.string.disconnected);
        try {
            PackageManager pm = getPackageManager();
            ServiceInfo info = pm.getServiceInfo(getComponentName(), PackageManager.GET_SERVICES);
            PendingIntent mainIntent = PendingIntent.getActivity(this, 1001,
                    new Intent(Intent.ACTION_MAIN)
                            .setComponent(new ComponentName("com.afollestad.cabinet", "ui.MainActivity")),
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

    protected abstract void connect() throws Exception;

    protected abstract List<PluginFile> listFiles(String path) throws Exception;

    protected abstract void newFile(String path) throws Exception;

    protected abstract void mkDir(String path) throws Exception;

    protected abstract void copyFile(String source, String dest) throws Exception;

    protected abstract void copyDir(String source, String dest) throws Exception;

    protected abstract void moveFile(String source, String dest) throws Exception;

    protected abstract void moveDir(String source, String dest) throws Exception;

    protected abstract void rmFile(String path) throws Exception;

    protected abstract void rmDir(String path) throws Exception;

    protected abstract void disconnect() throws Exception;

    protected abstract boolean authenticationNeeded();

    protected abstract Intent authenticator();

    protected abstract PluginService getService();

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

    private void respond(Messenger responder, PluginAction action, Object obj, Bundle data) {
        if (mMessenger == null)
            throw new IllegalStateException("Plugin service is not connected.");
        Message msg = Message.obtain(null, action.value(), obj);
        if (data != null)
            msg.setData(data);
        try {
            responder.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}