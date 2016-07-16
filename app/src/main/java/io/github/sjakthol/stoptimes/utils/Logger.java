package io.github.sjakthol.stoptimes.utils;

import android.util.Log;

/**
 * A wrapper around the default logging class that allows messages to be more easily
 * formatted. Logging is also disabled whenever we are running unit tests.
 */
public class Logger {

    private static final boolean TESTING = Helpers.isInJUnitTest();

    public static void d(String TAG, String msg) {
        if (!TESTING) Log.d(TAG, msg);
    }
    public static void d(String TAG, String msg, Throwable tr) {
        if (!TESTING) Log.d(TAG, msg, tr);
    }
    public static void d(String TAG, String msg, Object ...params) {
        if (!TESTING) Log.d(TAG, String.format(msg, params));
    }

    public static void i(String TAG, String msg) {
        if (!TESTING) Log.i(TAG, msg);
    }
    public static void i(String TAG, String msg, Throwable tr) {
        if (!TESTING) Log.i(TAG, msg, tr);
    }
    public static void i(String TAG, String msg, Object ...params) {
        if (!TESTING) Log.i(TAG, String.format(msg, params));
    }

    public static void w(String TAG, String msg) {
        if (!TESTING) Log.w(TAG, msg);
    }
    public static void w(String TAG, String msg, Throwable tr) {
        if (!TESTING) Log.w(TAG, msg, tr);
    }
    public static void w(String TAG, String msg, Object ...params) {
        if (!TESTING) Log.w(TAG, String.format(msg, params));
    }

    public static void e(String TAG, String msg) {
        if (!TESTING) Log.e(TAG, msg);
    }
    public static void e(String TAG, String msg, Throwable tr) {
        if (!TESTING) Log.e(TAG, msg, tr);
    }
    public static void e(String TAG, String msg, Object ...params) {
        if (!TESTING) Log.e(TAG, String.format(msg, params));
    }

    public static void wtf(String TAG, String msg) {
        if (!TESTING) Log.wtf(TAG, msg);
    }
    public static void wtf(String TAG, String msg, Throwable tr) {
        if (!TESTING) Log.wtf(TAG, msg, tr);
    }
    public static void wtf(String TAG, String msg, Object ...params) {
        if (!TESTING) Log.wtf(TAG, String.format(msg, params));
    }
}
