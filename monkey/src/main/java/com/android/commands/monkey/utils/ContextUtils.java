/*
 * Copyright (c) 2020 Bytedance Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
