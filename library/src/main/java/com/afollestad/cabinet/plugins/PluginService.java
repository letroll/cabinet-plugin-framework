package com.afollestad.cabinet.plugins;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class PluginService extends Service {

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mSender = msg.replyTo;
            try {
                PluginAction action = PluginAction.valueOf(msg.what);
                switch (action) {
                    case CONNECT:
                        if (authenticationNeeded()) {
                            // Authentication needed
                            startActivity(getAuthenticatorIntent());
                        } else {
                            // Authentication not needed, connect now
                            connect();
                        }
                        break;
                    case LIST: {
                        String path = msg.getData().get("path").toString();
                        List<PluginFile> results = listFiles(path);
                        respond(PluginAction.LIST, results, msg.getData());
                        break;
                    }
                    case CREATE: {
                        String path = msg.getData().get("path").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            newFile(path);
                        else
                            mkDir(path);
                        respond(PluginAction.CREATE, null, msg.getData());
                        break;
                    }
                    case COPY: {
                        String source = msg.getData().get("source").toString();
                        String dest = msg.getData().get("dest").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            copyFile(source, dest);
                        else
                            copyDir(source, dest);
                        respond(PluginAction.COPY, null, msg.getData());
                        break;
                    }
                    case MOVE: {
                        String source = msg.getData().get("source").toString();
                        String dest = msg.getData().get("dest").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            moveFile(source, dest);
                        else
                            moveDir(source, dest);
                        respond(PluginAction.MOVE, null, msg.getData());
                        break;
                    }
                    case DELETE: {
                        String path = msg.getData().get("path").toString();
                        if (msg.getData().get("type").toString().equals("file"))
                            rmFile(path);
                        else
                            rmDir(path);
                        respond(PluginAction.DELETE, null, msg.getData());
                        break;
                    }
                    case DISCONNECT:
                        disconnect();
                        respond(PluginAction.DISCONNECT, null, null);
                        break;
                }
            } catch (Exception e) {
                respond(PluginAction.ERROR, e.getLocalizedMessage(), null);
            }
        }
    }

    private Messenger mSender;
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
        log("Received: " + intent.getAction());
        if (intent.getAction() != null &&
                PluginAuthenticator.AUTHENTICATED_ACTION.equals(intent.getAction())) {
            // Authentication was finished, connect now
            try {
                connect();
            } catch (Exception e) {
                respond(PluginAction.ERROR, e.getLocalizedMessage(), null);
            }
        }
        return START_STICKY;
    }

    protected abstract void connect() throws Exception;

    protected abstract List<PluginFile> listFiles(String path) throws Exception;

    protected abstract void newFile(String path) throws Exception;

    protected abstract void mkDir(String path) throws Exception;

    protected abstract void copyFile(String source, String dest);

    protected abstract void copyDir(String source, String dest);

    protected abstract void moveFile(String source, String dest);

    protected abstract void moveDir(String source, String dest);

    protected abstract void rmFile(String path) throws Exception;

    protected abstract void rmDir(String path) throws Exception;

    protected abstract void disconnect() throws Exception;

    protected abstract boolean authenticationNeeded();

    protected abstract Intent authenticator();

    protected abstract PluginService getService();

    private Intent getAuthenticatorIntent() {
        PluginService service = getService();
        return authenticator()
                .putExtra(PluginAuthenticator.TARGET_COMPONENT,
                        new ComponentName(service.getPackageName(), service.getClass().getName()).flattenToString())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void respond(PluginAction action, Object obj, Bundle data) {
        if (mSender == null)
            throw new IllegalStateException("Plugin service is not connected.");
        Message msg = Message.obtain(null, action.value(), obj);
        if (data != null)
            msg.setData(data);
        try {
            mSender.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}