package com.danikula.videocache;

import android.util.Log;

public abstract class Logger {

    static public final String LOG_TAG = "ProxyCache";

    private Logger() { }

    public static void w(String message) {
        if (BuildConfig.DEBUG) {
            Log.w(LOG_TAG, message);
        }
    }

    public static void e(String message, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, message, e);
        }
    }

    public static void d(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, message);
        }
    }

    public static void i(String message) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, message);
        }
    }

    public static void e(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, message);
        }
    }

    public static void d(String message, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, message, e);
        }
    }
}
