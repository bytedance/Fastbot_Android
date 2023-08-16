/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.android.commands.monkey.utils.Config.debug;

/**
 * @author Zhao Zhang, Tianxiao Gu
 */

public class Logger {

    public static final String TAG = "[Fastbot]";

    public static void logo() {
        System.out.format
                ("▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄   ▄▄▄▄▄▄▄▄▄▄▄  ▄▄▄▄▄▄▄▄▄▄▄ \n" +
                "▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░▌ ▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌\n" +
                "▐░█▀▀▀▀▀▀▀▀▀ ▐░█▀▀▀▀▀▀▀█░▌▐░█▀▀▀▀▀▀▀▀▀  ▀▀▀▀█░█▀▀▀▀ ▐░█▀▀▀▀▀▀▀█░▌▐░█▀▀▀▀▀▀▀█░▌ ▀▀▀▀█░█▀▀▀▀ \n" +
                "▐░▌          ▐░▌       ▐░▌▐░▌               ▐░▌     ▐░▌       ▐░▌▐░▌       ▐░▌     ▐░▌     \n" +
                "▐░█▄▄▄▄▄▄▄▄▄ ▐░█▄▄▄▄▄▄▄█░▌▐░█▄▄▄▄▄▄▄▄▄      ▐░▌     ▐░█▄▄▄▄▄▄▄█░▌▐░▌       ▐░▌     ▐░▌     \n" +
                "▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌▐░░░░░░░░░░░▌     ▐░▌     ▐░░░░░░░░░░▌ ▐░▌       ▐░▌     ▐░▌     \n" +
                "▐░█▀▀▀▀▀▀▀▀▀ ▐░█▀▀▀▀▀▀▀█░▌ ▀▀▀▀▀▀▀▀▀█░▌     ▐░▌     ▐░█▀▀▀▀▀▀▀█░▌▐░▌       ▐░▌     ▐░▌     \n" +
                "▐░▌          ▐░▌       ▐░▌          ▐░▌     ▐░▌     ▐░▌       ▐░▌▐░▌       ▐░▌     ▐░▌     \n" +
                "▐░▌          ▐░▌       ▐░▌ ▄▄▄▄▄▄▄▄▄█░▌     ▐░▌     ▐░█▄▄▄▄▄▄▄█░▌▐░█▄▄▄▄▄▄▄█░▌     ▐░▌     \n" +
                "▐░▌          ▐░▌       ▐░▌▐░░░░░░░░░░░▌     ▐░▌     ▐░░░░░░░░░░▌ ▐░░░░░░░░░░░▌     ▐░▌     \n" +
                " ▀            ▀         ▀  ▀▀▀▀▀▀▀▀▀▀▀       ▀       ▀▀▀▀▀▀▀▀▀▀   ▀▀▀▀▀▀▀▀▀▀▀       ▀\n");
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }

    public static void println(Object message) {
        try {
            System.out.format(TAG + "[" + getCurrentTimeStamp() + "] %s\n", message);
            Log.i(TAG, message.toString());
        } catch (java.lang.NumberFormatException e) {} catch (Exception e) {}
    }

    public static void format(String format, Object... args) {
        if (debug) System.out.format(TAG + format + "\n", args);
    }

    public static void debugFormat(String format, Object... args) {
        if (debug) System.out.format(TAG + "*** DEBUG *** " + format + "\n", args);
    }

    public static void warningFormat(String format, Object... args) {
        System.out.format(TAG + "*** WARNING *** " + format + "\n", args);
    }

    public static void infoFormat(String format, Object... args) {
        if (debug) System.out.format(TAG + "*** INFO *** " + format + "\n", args);
    }

    public static void warningPrintln(Object message) {
        System.out.format(TAG + "*** WARNING *** %s\n", message);
        Log.w(TAG, "*** WARNING *** %s" + message);
    }

    public static void infoPrintln(Object message) {
        if (debug) {
            System.out.format(TAG + "*** INFO *** %s\n", message);
            Log.i(TAG, "*** INFO *** "+ message);
        }
    }

    public static void errorPrintln(Object message) {
        System.err.format(TAG + "*** ERROR *** %s\n", message);
        Log.e(TAG, "*** ERROR *** "+ message);
    }
}
