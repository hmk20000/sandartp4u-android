package org.kccc.sandartp4u;

import android.util.Log;

/**
 * Created by whylee on 2014. 5. 27..
 */
public class Logger {
    public static boolean isDebuggable;

    public static void d(String key, String msg, Throwable tr) {
        if (isDebuggable) Log.d(key, msg, tr);
    }

    public static void d(String key, String msg) {
        if (isDebuggable) Log.d(key, msg);
    }

    public static void w(String key, String msg, Throwable tr) {
        if (isDebuggable) Log.w(key, msg, tr);
    }

    public static void w(String key, String msg) {
        if (isDebuggable) Log.w(key, msg);
    }

    public static void i(String key, String msg, Throwable tr) {
        if (isDebuggable) Log.i(key, msg, tr);
    }

    public static void i(String key, String msg) {
        if (isDebuggable) Log.i(key, msg);
    }

    public static void e(String key, String msg, Throwable tr) {
        if (isDebuggable) Log.e(key, msg, tr);
    }

    public static void e(String key, String msg) {
        if (isDebuggable) Log.e(key, msg);
    }
}
