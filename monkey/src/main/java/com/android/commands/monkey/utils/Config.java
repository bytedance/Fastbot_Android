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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Zhao Zhang
 */

public class Config {
    static private final Properties configurations;

    static {
        configurations = new Properties(System.getProperties());
        loadConfiguration("/data/local/tmp/max.config");
        loadConfiguration("/sdcard/max.config");
    }


    /**
     * fastbot version
     */
    public static final String fastbotversion = "2.3.23.0810.1705-git";
    /**
     * enable debug log, disable by default
     */
    public static final boolean debug = Config.getBoolean("max.debug", false);
    /**
     * fastbot logger level
     */
    public static final int verbose = Config.getInteger("max.verbose", 2);
    /**
     * enable takescreenshot
     */
    public static final boolean takeScreenshotForEveryStep = Config.getBoolean("max.takeScreenshotForEveryStep", false);
    /**
     * enable save guitree to xml file
     */
    public static final boolean saveGUITreeToXmlEveryStep = Config.getBoolean("max.saveGUITreeToXmlEveryStep", false);
    /**
     * image writer queue settings, flush threshold & queue count
     */
    public static final int flushImagesThreshold = Config.getInteger("max.flushImagesThreshold", 10);
    public static final int imageWriterCount = Config.getInteger("max.imageWriterCount", 3);
    /**
     * input fuzzing, default 50% probability fuzz input to textview
     */
    public static final boolean doinputtextFuzzing = Config.getBoolean("max.doinputtextFuzzing", true);
    public static final double inputRate = Config.getDouble("max.inputRate", 0.5D);
    public static final double randomFormattedStringProp = Config.getDouble("max.randomFormattedStringProp", 0.5);
    /**
     * input fuzzing, randomly get fuzz input from a predefined list
     */
    public static final boolean randomPickFromStringList = Config.getBoolean("max.randomPickFromStringList", false);
    public static final double stringFromListRate = Config.getDouble("max.stringFromListRate", 1D);
    /**
     * default throttle, maybe never use
     */
    public static final long defaultGUIThrottle = Config.getLong("max.defaultGUIThrottle", 200L);
    /**
     * the time interval between each swipe atomic event
     */
    public static final long swipeDuration = Config.getLong("max.swipeDuration", 200);
    /**
     * get retry settings for guitree
     */
    public static final long refectchInfoWaitingInterval = Config.getLong("max.refectchInfoWaitingInterval", 50);
    public static final int refectchInfoCount = Config.getInteger("max.refectchInfoCount", 4);
    /**
     * generator fuzzing event
     */
    public static final double fuzzingRate = Config.getDouble("max.fuzzingRate", 0.01D);
    /**
     * fuzzing setting, enable generate system key events
     */
    public static final double doRotationFuzzing = Config.getDouble("max.doRotateFuzzing", 0.15);
    public static final double doAppSwitchFuzzing = Config.getDouble("max.doAppSwitchFuzzing", 0.15);
    public static final double doTrackballFuzzing = Config.getDouble("max.doTrackballFuzzing", 0.15);
    public static final double doNavKeyFuzzing = Config.getDouble("max.doNavKeyFuzzing", 0.15);
    public static final double doKeyCodeFuzzing = Config.getDouble("max.doKeyCodeFuzzing", 0.15);
    public static final double doSystemKeyFuzzing = Config.getDouble("max.doSystemKeyFuzzing", 0.15);
    public static final double doDragFuzzing = Config.getDouble("max.doDragFuzzing", 0.5);
    public static final double doPinchZoomFuzzing = Config.getDouble("max.doPinchZoomFuzzing", 0.15);
    public static final double doClickFuzzing = Config.getDouble("max.doClickFuzzing", 0.7);
    /**
     * mutationFuzzing
     */
    public static final double doMutationAirplaneFuzzing = Config.getDouble("max.doMutationAirplaneFuzzing", 0.001);
    public static final double doMutationMutationAlwaysFinishActivitysFuzzing = Config.getDouble("max.doMutationMutationAlwaysFinishActivitysFuzzing", 0.1);
    public static final double doMutationWifiFuzzing = Config.getDouble("max.doMutationWifiFuzzing", 0.001);


    /**
     * enable clear package, adb shell pm clear
     */
    public static final boolean clearPackage = Config.getBoolean("max.clearPackage", false);
    /**
     * enable dump fastbot memory, never use
     */
    public static final boolean enableDumpMemory = Config.getBoolean("max.enableDumpMemory", false);
    /**
     * grant all permissions required, enabled by default
     */
    public static final boolean grantAllPermission = Config.getBoolean("max.grantAllPermission", true);
    /**
     * start mutation at startup
     */
    public static final double startMutaion = Config.getDouble("max.startMutation", 0.3D);

    /**
     * enable exec shell event, default disable execution shell per startup
     */
    public static final boolean execPreShell = Config.getBoolean("max.execPreShell", true);
    public static final boolean execPreShellEveryStartup = Config.getBoolean("max.execPreShellEveryStartup", false);
    public static final int throttleForExecPreShell = Config.getInteger("max.throttleForExecPreShell", 3000);
    /**
     * enable exec schema event
     */
    public static final boolean execSchema = Config.getBoolean("max.execSchema", true);
    public static final boolean execSchemaEveryStartup = Config.getBoolean("max.execSchemaEveryStartup", true);
    public static final int throttleForExecPreSchema = Config.getInteger("max.throttleForExecPreSchema", 3000);
    /**
     * schema traversal mode, disable by default
     */
    public static final boolean schemaTraversalMode = Config.getBoolean("max.schemaTraversalMode", false);
    /**
     * loging logcat when crashing
     */
    public static final boolean requestLogcat = Config.getBoolean("max.requestLogcat", true);
    public static final int LogcatLineNums = Config.getInteger("max.logcatLineNums", 30000);
    /**
     * use 'dumpsys window' get plugin activity
     */
    public static final boolean totalActivityComplexCalc = Config.getBoolean("max.totalActivityComplexCalc", false);
    /**
     * monitor app performance, disable by default
     */
    public static final boolean collectPerformance = Config.getBoolean("max.collectPerformance", false);
    /**
     * startup and sleep n seconds
     */
    public static final int startAfterNSecondsofsleep = Config.getInteger("max.startAfterNSecondsofsleep", 2000);
    /**
     * startup and scroll top to down n times, disable by default
     */
    public static final boolean startAfterDoScrollAction = Config.getBoolean("max.startAfterDoScrollAction", false);
    public static final int startAfterDoScrollActionTimes = Config.getInteger("max.startAfterDoScrollActionTimes", 1);
    /**
     * startup and scroll down to top n times, disable by default
     */
    public static final boolean startAfterDoScrollBottomAction = Config.getBoolean("max.startAfterDoScrollBottomAction", false);
    public static final int startAfterDoScrollBottomActionTimes = Config.getInteger("max.startAfterDoScrollBottomActionTimes", 1);
    /**
     * scroll andr sleep n seconds
     */
    public static final int scrollAfterNSecondsofsleep = Config.getInteger("max.scrollAfterNSecondsofsleep", 3000);
    /**
     * enable force stop app
     */
    public static final boolean enableStopPackage = Config.getBoolean("max.enableStopPackage", true);
    /**
     * customize the height of the top tarbar of the device, this area needs to be cropped out
     */
    public static final int bytestStatusBarHeight = Config.getInteger("max.bytestStatusBarHeight", 0);
    /**
     * click on the coordinates in the randomly selected area
     */
    public static final boolean useRandomClick = Config.getBoolean("max.useRandomClick", true);
    /**
     * string cache size, never use
     */
    public static final int maxStringListSize = Config.getInteger("max.maxStringListSize", 2000);
    /**
     * startup activity use intent extra data
     */
    public static final String quickappStartActivityIntentPutExtra = Config.get("max.quickappStartActivityIntentPutExtra", "open_url");
    /**
     * allow jumping out of the app under test
     */
    public static final String allowStartActivityEscapePackageName = Config.get("max.allowStartActivityEscapePackageName", "android"); //"android"  ,only stone use
    public static final boolean allowStartActivityEscapeAny = Config.getBoolean("max.allowStartActivityEscapeAny", false);
    /**
     * allow press home and restart app
     */
    public static final boolean doHoming = Config.getBoolean("max.doHoming", true);
    public static final double homingRate = Config.getDouble("max.homingRate", 0.3D);

    /**
     * history mission restart
     */
    public static final boolean doHistoryRestart = Config.getBoolean("max.doHistoryRestart", true);
    public static final double historyRestartRate = Config.getDouble("max.historyRestartRate", 0.3D);

    public static final int homeAfterNSecondsofsleep = Config.getInteger("max.homeAfterNSecondsofsleep", 5000);


    private static void loadConfiguration(String fileName) {
        File configFile = new File(fileName);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                configurations.load(fis);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Fail to load the configuration file at " + configFile);
            }
        }
    }

    public static Object set(String key, String value) {
        return configurations.setProperty(key, value);
    }

    public static Object setBoolean(String key, boolean value) {
        return configurations.setProperty(key, Boolean.toString(value));
    }

    public static Object setDouble(String key, double value) {
        return configurations.setProperty(key, Double.toString(value));
    }

    public static Object setIntger(String key, int value) {
        return configurations.setProperty(key, Integer.toString(value));
    }

    public static String get(String key) {
        return configurations.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        if (value != null) {
            return value;
        }
        configurations.put(key, defaultValue);
        return defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        configurations.put(key, defaultValue);
        return defaultValue;
    }

    public static int getInteger(String key, int defaultValue) {
        String value = get(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
            }
        }
        configurations.put(key, defaultValue);
        return defaultValue;
    }

    public static long getLong(String key, long defaultValue) {
        String value = get(key);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
            }
        }
        configurations.put(key, defaultValue);
        return defaultValue;
    }

    public static double getDouble(String key, double defaultValue) {
        String value = get(key);
        if (value != null) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException e) {
            }
        }
        configurations.put(key, defaultValue);
        return defaultValue;
    }

    public static void printConfigurations() {
        Logger.println("Configurations:");
        int maxLength = Integer.MIN_VALUE;
        List<String> keys = new ArrayList<>(configurations.size());
        for (Object key : configurations.keySet()) {
            int length = key.toString().length();
            if (length > maxLength) {
                maxLength = length;
            }
            keys.add(key.toString());
        }
        Collections.sort(keys);
        String formatter = String.format(" %%%ds: %%s", maxLength);
        for (String key : keys) {
            Logger.format(formatter, key, configurations.get(key));
        }
    }
}
