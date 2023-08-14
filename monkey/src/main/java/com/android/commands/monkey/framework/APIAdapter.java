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

package com.android.commands.monkey.framework;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.inputmethod.InputMethodInfo;

import com.android.commands.monkey.utils.Logger;
import com.android.internal.view.IInputMethodManager;
import com.android.commands.monkey.utils.ContextUtils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Zhao Zhang, Tianxiao Gu
 */

/**
 * Reflect utils
 */
public class APIAdapter {


    private static Method getTasksMethod = null;

    private static Method findMethod(Class<?> clazz, String name, Class<?>... types) {
        Method method = null;
        try {
            method = clazz.getMethod(name, types);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Logger.errorPrintln("findMethod() error, NoSuchMethodException happened, there is no such method: "+name);
        } catch (java.lang.NoSuchMethodError e) {
            Logger.errorPrintln("findMethod() error, NoSuchMethodError happened,, there is no such method: "+name);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return method;
    }

    public static PermissionInfo getPermissionInfo(IPackageManager ipm, String perm, int flags) {
        Class<?> clazz = ipm.getClass();
        String name = "getPermissionInfo";
        Method method = findMethod(clazz, name, String.class, int.class);
        if (method != null) {
            return (PermissionInfo) invoke(method, ipm, perm, flags);
        }
        method = findMethod(clazz, name, String.class, String.class, int.class);
        if (method != null) {
            return (PermissionInfo) invoke(method, ipm, perm, "shell", flags);
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
        return null;
    }

    public static void registerReceiver(IActivityManager am, IIntentReceiver receiver, IntentFilter filter, int userId) {
        Class<?> clazz = am.getClass();
        String name = "registerReceiver";
        Method method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId);
            return;
        }
        method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class, boolean.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId, false);
            return;
        }
        method = findMethod(clazz, name, IApplicationThread.class, String.class, IIntentReceiver.class,
                IntentFilter.class, String.class, int.class, int.class);
        if (method != null) {
            invoke(method, am, null, null, receiver, filter, null, userId, 0);
            return;
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
    }

    public static IActivityManager getActivityManager() {
        {
            Class<?> clazz = ActivityManagerNative.class;
            String name = "getDefault";
            Method method = findMethod(clazz, name);
            if (method != null) {
                return (IActivityManager) invoke(method, null);
            }
        }
        {
            Class<?> clazz = ActivityManager.class;
            String name = "getService";
            Method method = findMethod(clazz, name);
            if (method != null) {
                return (IActivityManager) invoke(method, null);
            }
        }
        Logger.println("Cannot getActivityManager");
        System.exit(1);
        return null;
    }

    private static Object invoke(Method method, Object reciver, Object... args) {
        try {
            return method.invoke(reciver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private static Object invokej(Method method, Object reciver, Object... args) {
        try {
            return method.invoke(reciver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object invokek(Method method, Object reciver, Object... args) {
        try {
            return method.invoke(reciver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            return null;
        }
    }

    public static List<ResolveInfo> queryIntentActivities(PackageManager mPm, Intent intent) {
        return mPm.queryIntentActivities(intent, 0);
    }

    @SuppressWarnings("unchecked")
    public static List<RunningTaskInfo> getTasks(IActivityManager iAm, int maxNum) {
        Method method = getTasksMethod;
        if (method == null) {
            Class<?> clazz = iAm.getClass();
            String name = "getTasks";
            method = findMethod(clazz, name, int.class, int.class);
            if (method == null) {
                method = findMethod(clazz, name, int.class);
            }
            if (method == null) {
                Logger.println("Cannot resolve method: " + name);
                System.exit(1);
            }
            getTasksMethod = method;
        }
        int parameterCount = method.getParameterTypes().length;
        if (parameterCount == 2) {
            return (List<RunningTaskInfo>) invokej(method, iAm, maxNum, 0 /* flags */);
        } else { // 1
            return (List<RunningTaskInfo>) invokej(method, iAm, maxNum);
        }
    }


    public static void setActivityController(IActivityManager mAm, Object controller) {
        Class<?> clazz = mAm.getClass();
        String name = "setActivityController";
        Method method = findMethod(clazz, name, android.app.IActivityController.class);
        if (method != null) {
            invoke(method, mAm, controller);
            return;
        }
        method = findMethod(clazz, name, android.app.IActivityController.class, boolean.class);
        if (method != null) {
            invoke(method, mAm, controller, true);
            return;
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
    }

    public static void broadcastIntent(IActivityManager mAm, Intent paramIntent) {
        Class<?> c0 = mAm.getClass();
        String c1 = "broadcastIntent";
        Method m0 = findMethod(c0, c1, IApplicationThread.class,
                Intent.class, String.class, IIntentReceiver.class,
                int.class, String.class, Bundle.class,
                String[].class, int.class, Bundle.class,
                boolean.class, boolean.class, int.class);
        if (m0 != null) {
            invoke(m0, mAm, null, paramIntent, null, null, 0, null, null, null, 0, null, false, false, 0);
            return;
        }
        m0 = findMethod(c0, c1, IApplicationThread.class,
                Intent.class, String.class, IIntentReceiver.class,
                int.class, String.class, Bundle.class,
                String.class, int.class,
                boolean.class, boolean.class, int.class);
        if (m0 != null) {
            invoke(m0, mAm, null, paramIntent, null, null, 0, null, null, null, 0, false, false, 0);
            return;
        }
        System.out.format("Cannot resolve m0: " + c1);
        System.exit(1);
    }


    public static Object startActivity(IActivityManager mAm, Intent paramIntent) {
        Class<?> c0 = mAm.getClass();
        String c1 = "startActivity";
        Method m0 = findMethod(c0, c1, IApplicationThread.class,
                String.class, Intent.class, String.class, IBinder.class,
                String.class, int.class, int.class, ProfilerInfo.class, Bundle.class);

        if (m0 != null) {
            return invokek(m0, mAm, null, null, paramIntent, null, null, null, 0, 0, null, null);
        }
        System.out.format("Cannot resolve m0: " + c1);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<InputMethodInfo> getEnabledInputMethodList(IInputMethodManager iIMM) {
        Class<?> clazz = iIMM.getClass();
        String name = "getEnabledInputMethodList";
        Method method = findMethod(clazz, name);
        if (method != null) {
            return (List<InputMethodInfo>) invoke(method, iIMM);
        }
        method = findMethod(clazz, name, int.class);
        if (method != null) {
            return (List<InputMethodInfo>) invoke(method, iIMM, 0);
        }
        Logger.println("Cannot resolve method: " + name);
        System.exit(1);
        return null;
    }

    public static boolean setInputMethod(IInputMethodManager iIMM, String ime) {
        Class<?> clazz = iIMM.getClass();
        String name = "setInputMethod";
        Method method = findMethod(clazz, name, IBinder.class, String.class);
        if (method != null) {
            if (invokej(method, iIMM, null, ime) != null) {
                return true;
            }
        }
        return false;
    }

    public static String getSerial() {
        Class<?> classType = null;
        String serial = "unknown";
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            serial = (String) getMethod.invoke(classType, new Object[]{"ro.serialno"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.println("// device serial number is " + serial);
        return serial;
    }
}
