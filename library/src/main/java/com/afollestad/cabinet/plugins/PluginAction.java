package com.afollestad.cabinet.plugins;

/**
 * @author Aidan Follestad (afollestad)
 */
public enum PluginAction {

    CONNECT(0),
    LIST(1),
    CREATE(2),
    COPY(3),
    MOVE(4),
    DELETE(5),
    DISCONNECT(6),
    ERROR(7);

    private int mValue;

    PluginAction(int value) {
        mValue = value;
    }

    public static PluginAction valueOf(int value) {
        switch (value) {
            case 1:
                return LIST;
            case 2:
                return CREATE;
            case 3:
                return DELETE;
            case 4:
                return DISCONNECT;
            case 5:
                return ERROR;
            default:
                return CONNECT;
        }
    }

    public int value() {
        return mValue;
    }
}
