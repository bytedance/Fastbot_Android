/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.utils;

import android.content.Context;
import android.os.Looper;

import java.lang.reflect.Method;

/**
 * @author Dingchun Wang
 */

public class ContextUtils {
    private static Context mContext = null;

    /**
     * Get context through reflection
     * @return
     */
    public static synchronized Context getSystemContext() {

        if (mContext != null) {
            return mContext;
        }
        try {
            Looper.prepareMainLooper();
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getDeclaredMethod("systemMain");
            Method getSystemContextMethod = activityThreadClass.getDeclaredMethod("getSystemContext");

            Object object = systemMainMethod.invoke(null);
            Context context = (Context) getSystemContextMethod.invoke(object);
            mContext = context;
            return context;
        } catch (Exception e) {
        }
        return null;
    }
}
