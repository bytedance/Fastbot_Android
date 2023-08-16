/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.bytedance.fastbot;

import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;

import com.android.commands.monkey.fastbot.client.Operate;
import com.android.commands.monkey.utils.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jianqiang Guo, Zhao Zhang
 */

public class AiClient {

    private static final AiClient singleton;

    static {
        boolean success;
        long begin = SystemClock.elapsedRealtimeNanos();
        success = tryToLoadNativeLib(false);
        if (!success){
            success = tryToLoadNativeLib(true);
        }
        long end = SystemClock.elapsedRealtimeNanos();
        Logger.infoFormat("load fastbot_native takes %d ms.", TimeUnit.NANOSECONDS.toMillis(end - begin));
        singleton = new AiClient(success);
    }

    public enum AlgorithmType {
        Random(0),
        SataRL(1),
        SataNStep(2),
        NStepQ(3),
        Reuse(4);

        private final int _value;

        AlgorithmType(int value) {
            this._value = value;
        }

        public int value() {
            return this._value;
        }
    }

    public static void InitAgent(AlgorithmType agentType, String packagename) {
        singleton.fgdsaf5d(agentType.value(), packagename, 0);
    }

    private boolean loaded = false;

    protected AiClient(boolean success) {
        loaded = success;
    }

    private static boolean tryToLoadNativeLib(boolean fromAPK){
        String path = "";
        try {
            path = getAiPath(fromAPK);
            System.load(path);
            Logger.println("fastbot native : library load!");
            Logger.println("fastbot native path is : "+path);
        } catch (UnsatisfiedLinkError e) {
            Logger.errorPrintln("Error: Could not load library!");
            Logger.errorPrintln(path);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getAiPathFromAPK(){
        String[] abis = Build.SUPPORTED_ABIS;
        List<String> abisList = Arrays.asList(abis);
        if (abisList.contains("x86_64")) {
            return "/data/local/tmp/monkey.apk!/lib/x86_64/libfastbot_native.so";
        } else if (abisList.contains("x86")) {
            return "/data/local/tmp/monkey.apk!/lib/x86/libfastbot_native.so";
        } else if (abisList.contains("arm64-v8a")) {
            return "/data/local/tmp/monkey.apk!/lib/arm64-v8a/libfastbot_native.so";
        } else {
            return "/data/local/tmp/monkey.apk!/lib/armeabi-v7a/libfastbot_native.so";
        }
    }

    private static String getAiPathLocally(){
        String[] abis = Build.SUPPORTED_ABIS;
        List<String> abisList = Arrays.asList(abis);
        if (abisList.contains("x86_64")) {
            return "/data/local/tmp/x86_64/libfastbot_native.so";
        } else if (abisList.contains("x86")) {
            return "/data/local/tmp/x86/libfastbot_native.so";
        } else if (abisList.contains("arm64-v8a")) {
            return "/data/local/tmp/arm64-v8a/libfastbot_native.so";
        } else {
            return "/data/local/tmp/armeabi-v7a/libfastbot_native.so";
        }
    }

    private static String getAiPath(boolean fromAPK) {
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.LOLLIPOP_MR1) {
            return getAiPathLocally();
        }else {
            if (fromAPK) {
                return getAiPathFromAPK();
            }else {
                return getAiPathLocally();
            }
        }
    }

    public static void loadResMapping(String resmapping) {
        if (null == singleton) {
            Logger.println("// Error: AiCore not initted!");
            return;
        }
        if (!singleton.loaded) {
            Logger.println("// Error: Could not load native library!");
            Logger.println("Please report this bug issue to github");
            System.exit(1);
        }
        singleton.jdasdbil(resmapping);
    }

    public static Operate getAction(String acvitty, String pageDesc) {
        return singleton.b1bhkadf(acvitty, pageDesc);
    }

    private native void jdasdbil(String b9);

    private native String b0bhkadf(String a0, String a1);
    private native void fgdsaf5d(int b7, String b2, int t);
    private native boolean nkksdhdk(String a0, float p1, float p2);

    public static native String getNativeVersion();

    public static boolean checkPointIsShield(String activity, PointF point)
    {
        return singleton.nkksdhdk(activity, point.x, point.y);
    }

    public Operate b1bhkadf(String activity, String pageDesc) {
        if (!loaded) {
            Logger.println("// Error: Could not load native library!");
            Logger.println("Please report this bug issue to github");
            System.exit(1);
        }
        String operateStr = b0bhkadf(activity, pageDesc);

        if (operateStr.length() < 1) {
            Logger.errorPrintln("native get operate failed " + operateStr);
            return null;
        }
        return Operate.fromJson(operateStr);
    }

}
