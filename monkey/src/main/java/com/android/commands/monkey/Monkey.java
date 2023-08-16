/*
 * Copyright 2007, The Android Open Source Project
 *
 * Modified - Copyright (c) 2020 Bytedance Inc.
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

package com.android.commands.monkey;

import android.app.IActivityController;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.IWindowManager;
import android.view.Surface;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.events.MonkeyEventSource;
import com.android.commands.monkey.events.base.MonkeyFlipEvent;
import com.android.commands.monkey.events.base.MonkeyKeyEvent;
import com.android.commands.monkey.events.base.MonkeyMotionEvent;
import com.android.commands.monkey.events.base.MonkeyRotationEvent;
import com.android.commands.monkey.events.base.MonkeyThrottleEvent;
import com.android.commands.monkey.framework.APIAdapter;
import com.android.commands.monkey.framework.AndroidDevice;
import com.android.commands.monkey.events.base.mutation.MutationAirplaneEvent;
import com.android.commands.monkey.events.base.mutation.MutationWifiEvent;
import com.android.commands.monkey.source.MonkeySourceApeNative;
import com.android.commands.monkey.source.MonkeySourceRandom;
import com.android.commands.monkey.source.MonkeySourceRandomScript;
import com.android.commands.monkey.source.MonkeySourceScript;
import com.android.commands.monkey.utils.Config;
import com.android.commands.monkey.utils.Logger;
import com.android.commands.monkey.utils.MonkeyUtils;
import com.android.commands.monkey.utils.RandomHelper;
import com.android.commands.monkey.utils.ContextUtils;
import com.bytedance.fastbot.AiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static com.android.commands.monkey.utils.Config.LogcatLineNums;
import static com.android.commands.monkey.utils.Config.allowStartActivityEscapeAny;
import static com.android.commands.monkey.utils.Config.allowStartActivityEscapePackageName;
import static com.android.commands.monkey.utils.Config.fastbotversion;
import static com.android.commands.monkey.utils.Config.grantAllPermission;
import static com.android.commands.monkey.utils.Config.requestLogcat;
import static com.android.commands.monkey.utils.Config.startMutaion;


/**
 * @author Zhao Zhang, Tianxiao Gu
 */

/**
 * Application that injects random key events and other actions into the system.
 */
public class Monkey {

    /**
     * Monkey Debugging/Dev Support
     * <p>
     * All values should be zero when checking in.
     */
    private static final int DEBUG_ALLOW_ANY_STARTS = 0;
    private static final long ONE_MINUTE_IN_MILLISECOND = 1000 * 60;
    private static final File TOMBSTONES_PATH = new File("/data/tombstones");
    /**
     * The monkey event source
     */
    MonkeyEventSource mEventSource;
    /**
     * The delay between event inputs
     **/
    private long mThrottle = 100;
    /**
     * Whether to randomize each throttle (0-mThrottle ms) inserted between
     * events.
     */
    private boolean mRandomizeThrottle = false;
    /**
     * The number of iterations
     **/
    private int mCount = 1000;
    /**
     * The random number seed
     **/
    private long mSeed = 0;
    /**
     * The random number generator
     **/
    private Random mRandom = null;
    /**
     * Dropped-event statistics
     **/
    private long mDroppedKeyEvents = 0;
    private long mDroppedPointerEvents = 0;
    private long mDroppedTrackballEvents = 0;
    private long mDroppedFlipEvents = 0;
    private long mDroppedRotationEvents = 0;
    /**
     * The delay between user actions. This is for the scripted monkey.
     **/
    private long mProfileWaitTime = 5000;
    /**
     * Device idle time. This is for the scripted monkey.
     **/
    private long mDeviceSleepTime = 30000;
    /**
     * The random select user actions. This is for the scripted monkey.
     */
    private boolean mRandomizeScript = false;
    /**
     * Abandoned
     */
    private boolean mScriptLog = false;
    /**
     * The monkey event factors. This is for the random monkey
     */
    private float[] mFactors = new float[MonkeySourceRandom.FACTORZ_COUNT];
    /**
     * framework Android server manager
     */
    private IActivityManager mAm;
    private IWindowManager mWm;
    private IPackageManager mPm;

    /**
     * Command line arguments
     */
    private String[] mArgs;

    /**
     * Current argument being parsed
     */
    private int mNextArg;

    /**
     * Data of current argument
     */
    private String mCurArgData;

    /**
     * Running in verbose output mode? 1= verbose, 2=very verbose
     */
    private int mVerbose;

    /**
     * Ignore any application crashes while running?
     */
    private boolean mIgnoreCrashes;

    /**
     * Ignore any not responding timeouts while running?
     */
    private boolean mIgnoreTimeouts;

    /**
     * (The activity launch still fails, but we keep pluggin' away)
     */
    private boolean mIgnoreSecurityExceptions;

    /**
     * Monitor /data/tombstones and stop the monkey if new files appear.
     */
    private boolean mMonitorNativeCrashes;

    /**
     * Ignore any native crashes while running?
     */
    private boolean mIgnoreNativeCrashes;

    /**
     * Send no events. Use with long throttle-time to watch user operations
     */
    private boolean mSendNoEvents;

    /**
     * This is set when we would like to abort the running of the monkey.
     */
    private boolean mAbort;

    /**
     * Count each event as a cycle. Set to false for scripts so that each time
     * through the script increments the count.
     */
    private boolean mCountEvents = true;

    /**
     * This is set by the ActivityController thread to request collection of ANR
     * trace files
     */
    private boolean mRequestAnrTraces = false;

    /**
     * This is set by the ActivityController thread to request a "dumpsys
     * meminfo"
     */
    private boolean mRequestDumpsysMemInfo = false;

    /**
     * This is set by the ActivityController thread to request a bugreport after
     * ANR
     */
    private boolean mRequestAnrBugreport = false;

    /**
     * This is set by the ActivityController thread to request a bugreport after
     * a system watchdog report
     */
    private boolean mRequestWatchdogBugreport = false;

    /**
     * Synchronization for the ActivityController callback to block until we are
     * done handling the reporting of the watchdog error.
     */
    private boolean mWatchdogWaiting = false;

    /**
     * This is set by the ActivityController thread to request a bugreport after
     * java application crash
     */
    private boolean mRequestAppCrashBugreport = false;

    /**
     * Request the bugreport based on the mBugreportFrequency.
     */
    private boolean mGetPeriodicBugreport = false;

    /**
     * Request the bugreport based on the mBugreportFrequency.
     */
    private boolean mRequestPeriodicBugreport = false;

    /**
     * Bugreport frequency.
     */
    private long mBugreportFrequency = 10;

    /**
     * Failure process name
     */
    private String mReportProcessName;

    /**
     * This is set by the ActivityController thread to request a "procrank"
     */
    private boolean mRequestProcRank = false;

    /**
     * Kill the process after a timeout or crash.
     */
    private boolean mKillProcessAfterError;

    /**
     * Generate hprof reports before/after monkey runs
     */
    private boolean mGenerateHprof;

    /**
     * Package blacklist file.
     */
    private String mPkgBlacklistFile = null;

    /**
     * Package whitelist file.
     */
    private String mPkgWhitelistFile = null;

    /**
     * Activity blackList file
     */
    private String mActBlacklistFile = null;

    /**
     * Activity whiteList file
     */
    private String mActWhitelistFile = null;

    /**
     * Categories we are allowed to launch
     **/
    private final ArrayList<String> mMainCategories = new ArrayList<String>();

    /**
     * Applications we can switch to.
     */
    private final ArrayList<ComponentName> mMainApps = new ArrayList<ComponentName>();

    /**
     * Capture bugreprot whenever there is a crash.
     **/
    private boolean mRequestBugreport = false;

    /**
     * a filename to the setup script (if any)
     */
    private String mSetupFileName = null;

    /**
     * a filenames of the script (if any)
     */
    private final ArrayList<String> mScriptFileNames = new ArrayList<String>();

    /**
     * a TCP port to listen on for remote commands.
     */
    private int mServerPort = -1;

    /**
     * tombstone file
     */
    private HashSet<String> mTombstones = null;

    /**
     * use fastbot-native nstep q algorithmic decision
     */
    private boolean mUseApeNative;

    /**
     * use fastbot-native reuse nq algorithmic decision
     */
    private boolean mUseApeNativeReuse;

    /**
     * outputdir for test result
     */
    private File mOutputDirectory = null;

    /**
     * Monkey test run times (minutes)
     */
    private long mRunningMillis = -1;

    /**
     * Monkey test end time
     */
    private long mEndTime;

    /**
     * Monkey test start time
     */
    private final long mStartTime = SystemClock.elapsedRealtimeNanos();

    /**
     * if we should target system packages regardless if they are listed
     */
    private boolean mPermissionTargetSystem = false;

    /**
     * crash count
     */
    private int crashCount = 0;

    /**
     * oom count
     */
    private int oomCount = 0;

    /**
     * application build mapping file
     */
    private String mMappingFilePath = "";



    /**
     * application version
     */
    private String appVersionCode = "";

    /**
     * Record the last crashed process id to prevent continuous crashes and repeated records
     */
    private int lastCrashPid = 0;

    /**
     * handle application not responding
     */
    private boolean mRequestHookAppNotResponding = false;
    private boolean shouldHookAppNotResponding = false;
    private ANRInfo anrInfo = new ANRInfo();

    /**
     * handle application crash
     */
    private boolean mRequestHookAppCrashed = false;
    private boolean shouldHookAppCrashed = false;
    private CrashInfo crashInfo = new CrashInfo();


    /**
     * Custom Input-Ime Method
     */
    private String ime = "com.android.adbkeyboard/.AdbIME";

    /**
     * Custom application launch intent
     */
    private String mMainIntentAction = null;
    private String mMainIntentData = null;

    /**
     * Custom quick-application launch intent
     */
    private String mMainQuickAppActivity = null;


    /**
     * Command-line entry point.
     *
     * @param args The command-line arguments
     */
    public static void main(String[] args) {
        Logger.logo();
        String version = fastbotversion;
        // Set the process name showing in "ps" or "top"
        Process.setArgV0("com.android.commands.monkey");
        Logger.println(" @Version: " + version);
        try {
            int resultCode = (new Monkey()).run(args, version);
            System.exit(resultCode);
        } catch (Throwable e) {
            Logger.println("Internal error");
            e.printStackTrace();
            Logger.println("Please report this bug issue to github");
            System.exit(1);
        }
    }

    /**
     * Load a list of package names from a file.
     *
     * @param fileName The file name, with package names separated by new line.
     * @param list     The destination list.
     * @return Returns false if any error occurs.
     */
    private static boolean loadPackageListFromFile(String fileName, Set<String> list) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String s;
            while ((s = reader.readLine()) != null) {
                s = s.trim();
                if ((s.length() > 0) && (!s.startsWith("#"))) {
                    list.add(s);
                }
            }
        } catch (IOException ioe) {
            Logger.warningPrintln(ioe);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    Logger.warningPrintln(ioe);
                }
            }
        }
        return true;
    }

    /**
     * Run "cat /data/anr/traces.txt". Wait about 5 seconds first, to let the
     * asynchronous report writing complete.
     */
    private void reportAnrTraces() {
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
        }
        commandLineReport("anr traces", "cat /data/anr/traces.txt");
    }

    /**
     * Record logcat at the same time when crashing
     *
     * @param time
     */
    private void reportLogcat(String time) {
        commandLineReport("Logcat_" + time + ".log", "logcat -t " + LogcatLineNums);
    }

    /**
     * Run "dumpsys meminfo"
     * <p>
     * NOTE: You cannot perform a dumpsys call from the ActivityController
     * callback, as it will deadlock. This should only be called from the main
     * loop of the monkey.
     */
    private void reportDumpsysMemInfo() {
        commandLineReport("meminfo", "dumpsys meminfo");
    }

    /**
     * Print report from a single command line.
     * <p>
     * TODO: Use ProcessBuilder & redirectErrorStream(true) to capture both
     * streams (might be important for some command lines)
     *
     * @param reportName Simple tag that will print before the report and in various
     *                   annotations.
     * @param command    Command line to execute.
     */
    private void commandLineReport(String reportName, String command) {
        System.err.println(reportName + ":");
        Writer logOutput = null;
        Writer tracesOutput = null;

        try {
            // Process must be fully qualified here because android.os.Process
            // is used elsewhere
            java.lang.Process p = Runtime.getRuntime().exec(command);

            if (mRequestBugreport) {
                logOutput = new BufferedWriter(new FileWriter(new File(mOutputDirectory, reportName).getAbsolutePath(), true));
            }

            if ("anr traces".equals(reportName)) {
                tracesOutput = new BufferedWriter(new FileWriter(new File(mOutputDirectory, "oom-traces.log").getAbsolutePath(), true));
            }

            if ("dropbox".equals(reportName)) {
                tracesOutput = new BufferedWriter(new FileWriter(new File(mOutputDirectory, "dumpsys-dropbox.log").getAbsolutePath(), true));
            }

            if (reportName.contains("Logcat")) {
                tracesOutput = new BufferedWriter(new FileWriter(new File(mOutputDirectory, reportName).getAbsolutePath(), true));
            }


            // pipe everything from process stdout -> System.err
            InputStream inStream = p.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            while ((s = inBuffer.readLine()) != null) {

                if (mRequestBugreport && logOutput != null) {
                    logOutput.write(s);
                    logOutput.write("\n");
                } else {
                    Logger.warningPrintln(s);
                }

                if ("anr traces".equals(reportName) || "dropbox".equals(reportName) || reportName.contains("Logcat")) {
                    if (tracesOutput != null) {
                        tracesOutput.write(s);
                        tracesOutput.write("\n");
                    }
                }
            }

            int status = p.waitFor();
            Logger.warningPrintln("// " + reportName + " status was " + status);

            if (logOutput != null) {
                logOutput.close();
            }

            if (tracesOutput != null) {
                tracesOutput.close();
            }
        } catch (Exception e) {
            Logger.warningPrintln("// Exception from " + reportName + ":");
            Logger.warningPrintln(e.toString());
        }
    }

    /**
     * Write the number of iteration to the log
     *
     * @param count TODO: Add the script file name to the log.
     */
    private void writeScriptLog(int count) {
        try {
            Writer output = new BufferedWriter(
                    new FileWriter(new File(Environment.getLegacyExternalStorageDirectory(), "scriptlog.txt"), true));
            output.write(
                    "iteration: " + count + " time: " + MonkeyUtils.toCalendarTime(System.currentTimeMillis()) + "\n");
            output.close();
        } catch (IOException e) {
            Logger.warningPrintln(e.toString());
        }
    }

    /**
     * Write the bugreport to the sdcard when crashing (option)
     *
     * @param reportName Name of the bug report
     */
    private void getBugreport(String reportName) {
        reportName += MonkeyUtils.toCalendarTime(System.currentTimeMillis());
        String bugreportName = reportName.replaceAll("[ ,:]", "_");
        commandLineReport(bugreportName + ".txt", "bugreport");
    }

    /**
     * Run the command!
     *
     * @param args The command-line arguments
     * @return Returns a posix-style result code. 0 for no error.
     */
    private int run(String[] args, String version) {
        // Super-early debugger wait
        for (String s : args) {
            if ("--wait-dbg".equals(s)) {
                Debug.waitForDebugger();
            }
        }

        // Default values for some command-line options
        mVerbose = 0;
        mCount = 1000;
        mSeed = 0;
        mThrottle = 100;
        mRunningMillis = -1;

        // prepare for command-line processing
        mArgs = args;
        mNextArg = 0;

        // set a positive value, indicating none of the factors is provided yet
        for (int i = 0; i < MonkeySourceRandom.FACTORZ_COUNT; i++) {
            mFactors[i] = 1.0f;
        }

        // set user-define options
        if (!processOptions()) {
            return -1;
        }

        // Load package blacklist or whitelist (if specified).
        if (!loadPackageLists()) {
            return -1;
        }

        // Load activity blacklist or whitelist (if specified).
        if (!loadActivityLists()) {
            return -1;
        }

        // now set up additional data in preparation for launch
        if (mMainCategories.size() == 0 && mMainIntentAction == null) {
            mMainCategories.add(Intent.CATEGORY_LAUNCHER);
            mMainCategories.add(Intent.CATEGORY_MONKEY);
        }

        // set seed
        if (mSeed == 0) {
            mSeed = System.currentTimeMillis() + System.identityHashCode(this);
        }

        // print important parameters
        if (mVerbose > 0) {
            Logger.println("// Monkey: seed=" + mSeed + " count=" + mCount + "\n\n");

            MonkeyUtils.getPackageFilter().dump();
            MonkeyUtils.getActivityFilter().dump();
            if (mMainCategories.size() != 0) {
                for (String category : mMainCategories) {
                    Logger.println("// IncludeCategory: " + category);
                }
            }
            if (mMainIntentAction != null) {
                Logger.println("// IncludeAction: " + mMainIntentAction);
                Logger.println("// IncludeData: " + mMainIntentData);
            }
            Config.setIntger("max.verbose", mVerbose);
        }

        // never use
        if (!checkInternalConfiguration()) {
            return -2;
        }

        // attach to the required system interfaces
        if (!getSystemInterfaces()) {
            return -3;
        }

        // Using the restrictions provided (categories & packages), generate a list of
        // activities that we can actually switch to.
        if (!getMainApps()) {
            return -4;
        }

        // adding valid activity
        if (!addLauncherToValidActivity()) {
            return -5;
        }


        mRandom = new Random(mSeed);
        String name = mMainApps.get(0).getPackageName();
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
        }

        //todo maybe set out failed
        System.setOut(System.out);

        Logger.println("// phone info： " + android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL + "_" + Build.VERSION.RELEASE);

        // script monkey
        if (mScriptFileNames != null && mScriptFileNames.size() == 1) {
            // script mode, ignore other options
            mEventSource = new MonkeySourceScript(mRandom, mScriptFileNames.get(0), mThrottle, mRandomizeThrottle,
                    mProfileWaitTime, mDeviceSleepTime);
            mEventSource.setVerbose(mVerbose);

            mCountEvents = false;
        } else if (mScriptFileNames != null && mScriptFileNames.size() > 1) {
            if (mSetupFileName != null) {
                mEventSource = new MonkeySourceRandomScript(mSetupFileName, mScriptFileNames, mThrottle,
                        mRandomizeThrottle, mRandom, mProfileWaitTime, mDeviceSleepTime, mRandomizeScript);
                mCount++;
            } else {
                mEventSource = new MonkeySourceRandomScript(mScriptFileNames, mThrottle, mRandomizeThrottle, mRandom,
                        mProfileWaitTime, mDeviceSleepTime, mRandomizeScript);
            }
            mEventSource.setVerbose(mVerbose);
            mCountEvents = false;
        } else if (mUseApeNative) {
            // fastbot monkey
            Logger.println("// runing fastbot");

            // init framework android device
            AndroidDevice.initializeAndroidDevice(mAm, mWm, mPm, ime);
            AndroidDevice.checkInteractive();

            if (!"".equals(mMappingFilePath) && !"max.mapping".equals(mMappingFilePath)) {
                AiClient.loadResMapping(mMappingFilePath);
            }

            mEventSource = new MonkeySourceApeNative(mRandom, mMainApps, mThrottle, mRandomizeThrottle, mPermissionTargetSystem, mOutputDirectory);
            mEventSource.setVerbose(mVerbose);

            // grant all permissions required, enabled by default
            if (grantAllPermission) {
                ((MonkeySourceApeNative) mEventSource).grantRuntimePermissions("GrantPermissionsActivity");
            }
            if (RandomHelper.toss(startMutaion)) {
                ((MonkeySourceApeNative) mEventSource).startMutation(mWm, mAm, mVerbose);
            }
            ((MonkeySourceApeNative) mEventSource).setAttribute(mMainApps.get(0).getPackageName(), appVersionCode, mMainIntentAction, mMainIntentData, mMainQuickAppActivity);
            if (mUseApeNativeReuse) {
                Logger.println("// init with reuse agent");
                ((MonkeySourceApeNative) mEventSource).initReuseAgent();
            }
        } else {
            // random monkey by default
            Logger.println("// runing google monkey mode");
            if (mVerbose >= 2) { // check seeding performance
                Logger.println("// Seeded: " + mSeed);
            }
            AndroidDevice.initializeAndroidDevice(mAm, mWm, mPm, ime);
            AndroidDevice.checkInteractive();

            mEventSource = new MonkeySourceRandom(mRandom, mMainApps, mThrottle, mRandomizeThrottle,
                    mPermissionTargetSystem, mOutputDirectory);
            mEventSource.setVerbose(mVerbose);
            ((MonkeySourceRandom) mEventSource).setAttr(mMainApps.get(0).getPackageName(), appVersionCode);
            // set any of the factors that has been set
            for (int i = 0; i < MonkeySourceRandom.FACTORZ_COUNT; i++) {
                if (mFactors[i] <= 0.0f) {
                    ((MonkeySourceRandom) mEventSource).setFactors(i, mFactors[i]);
                }
            }

            // in random mode, we start with a random activity
            ((MonkeySourceRandom) mEventSource).generateActivity();
        }

        // validate source generator
        if (!mEventSource.validate()) {
            return -5;
        }

        // If we're profiling, do it immediately before/after the main monkey
        if (mGenerateHprof) {
            signalPersistentProcesses();
        }

        int crashedAtCycle = 0;
        Logger.println("\n");

        try {
            // run looper, generate event
            crashedAtCycle = runMonkeyCycles();
        } finally {
            // Release the rotation lock if it's still held and restore the
            // original orientation.
            Logger.println("// Monkey is over!");
            new MonkeyRotationEvent(Surface.ROTATION_0, false).injectEvent(mWm, mAm, mVerbose);
        }

        if (this.mEventSource instanceof MonkeySourceRandom) {
            ((MonkeySourceRandom) this.mEventSource).tearDown();
        }

        if (this.mEventSource instanceof MonkeySourceApeNative) {
            new MutationAirplaneEvent().resetStatusAndExecute(mWm, mAm, mVerbose);
            new MutationWifiEvent().resetStatusAndExecute(mWm,mAm,mVerbose);
            ((MonkeySourceApeNative) this.mEventSource).tearDown();
        }

        // sync handle error information
        synchronized (this) {
            if (mRequestAnrTraces) {
                reportAnrTraces();
                mRequestAnrTraces = false;
            }
            if (mRequestAnrBugreport) {
                Logger.warningPrintln("Print the anr report");
                getBugreport("anr_" + mReportProcessName + "_");
                mRequestAnrBugreport = false;
            }
            if (mRequestWatchdogBugreport) {
                Logger.warningPrintln("Print the watchdog report");
                getBugreport("anr_watchdog_");
                mRequestWatchdogBugreport = false;
            }
            if (mRequestAppCrashBugreport) {
                getBugreport("app_crash" + mReportProcessName + "_");
                mRequestAppCrashBugreport = false;
            }
            if (mRequestDumpsysMemInfo) {
                reportDumpsysMemInfo();
                mRequestDumpsysMemInfo = false;
            }
            if (mRequestPeriodicBugreport) {
                getBugreport("Bugreport_");
                mRequestPeriodicBugreport = false;
            }
            if (mWatchdogWaiting) {
                mWatchdogWaiting = false;
                notifyAll();
            }
        }

        // unregister
        if (mGenerateHprof) {
            signalPersistentProcesses();
            if (mVerbose > 0) {
                Logger.println("// Generated profiling reports in /data/misc");
            }
        }

        APIAdapter.setActivityController(mAm, null);

        // report dropped event stats
        if (mVerbose > 0) {
            System.out.print(":Dropped: keys=");
            System.out.print(mDroppedKeyEvents);
            System.out.print(" pointers=");
            System.out.print(mDroppedPointerEvents);
            System.out.print(" trackballs=");
            System.out.print(mDroppedTrackballEvents);
            System.out.print(" flips=");
            System.out.print(mDroppedFlipEvents);
            System.out.print(" rotations=");
            System.out.println(mDroppedRotationEvents);
        }

        if (crashCount > 0 || oomCount > 0) {
            Logger.println("// App appears " + crashCount + " crash, " + oomCount + " anr, monkey using seed: " + mSeed);
        }

        if (crashedAtCycle < mCount - 1) {
            return crashedAtCycle;
        } else {
            if (mVerbose > 0) {
                Logger.println("// Monkey finished");
            }
            return 0;
        }
    }

    /**
     * Process the command-line options
     *
     * @return Returns true if options were parsed with no apparent errors.
     */
    private boolean processOptions() {
        // quick (throwaway) check for unadorned command
        if (mArgs.length < 1) {
            showUsage();
            return false;
        }
        Set<String> validPackages = new HashSet<>();

        try {
            String opt;
            while ((opt = nextOption()) != null) {
                switch (opt) {
                    case "-s":
                        mSeed = nextOptionLong("Seed");
                        break;
                    case "-p":
                        validPackages.add(nextOptionData());
                        break;
                    case "-c":
                        mMainCategories.add(nextOptionData());
                        break;
                    case "--intentaction":
                        mMainIntentAction = nextOptionData();
                        break;
                    case "--intentdata":
                        mMainIntentData = nextOptionData();
                        break;
                    case "--quickactivity":
                        mMainQuickAppActivity = nextOptionData();
                        break;
                    case "-v":
                        mVerbose += 1;
                        break;
                    case "--ignore-crashes":
                        mIgnoreCrashes = true;
                        break;
                    case "--ignore-timeouts":
                        mIgnoreTimeouts = true;
                        break;
                    case "--ignore-security-exceptions":
                        mIgnoreSecurityExceptions = true;
                        break;
                    case "--monitor-native-crashes":
                        mMonitorNativeCrashes = true;
                        break;
                    case "--ignore-native-crashes":
                        mIgnoreNativeCrashes = true;
                        break;
                    case "--kill-process-after-error":
                        mKillProcessAfterError = true;
                        break;
                    case "--hprof":
                        mGenerateHprof = true;
                        break;
                    case "--agent":
                        mUseApeNative = true;
                        String agentType = nextOptionData();
                        if ("reuseq".equals(agentType)) {
                            mUseApeNativeReuse = true;
                        }
                        break;
                    case "--replay-log":
                        String logFile = nextOptionData();
                        Config.set("max.replayLog", logFile);
                        break;
                    case "--running-minutes":
                        mRunningMillis = nextOptionLong("Running Minutes") * ONE_MINUTE_IN_MILLISECOND;
                        break;
                    case "--pct-touch": {
                        int i = MonkeySourceRandom.FACTOR_TOUCH;
                        mFactors[i] = -nextOptionLong("touch events percentage");
                        break;
                    }
                    case "--pct-motion": {
                        int i = MonkeySourceRandom.FACTOR_MOTION;
                        mFactors[i] = -nextOptionLong("motion events percentage");
                        break;
                    }
                    case "--pct-trackball": {
                        int i = MonkeySourceRandom.FACTOR_TRACKBALL;
                        mFactors[i] = -nextOptionLong("trackball events percentage");
                        break;
                    }
                    case "--pct-rotation": {
                        int i = MonkeySourceRandom.FACTOR_ROTATION;
                        mFactors[i] = -nextOptionLong("screen rotation events percentage");
                        break;
                    }
                    case "--pct-syskeys": {
                        int i = MonkeySourceRandom.FACTOR_SYSOPS;
                        mFactors[i] = -nextOptionLong("system (key) operations percentage");
                        break;
                    }
                    case "--pct-nav": {
                        int i = MonkeySourceRandom.FACTOR_NAV;
                        mFactors[i] = -nextOptionLong("nav events percentage");
                        break;
                    }
                    case "--pct-majornav": {
                        int i = MonkeySourceRandom.FACTOR_MAJORNAV;
                        mFactors[i] = -nextOptionLong("major nav events percentage");
                        break;
                    }
                    case "--pct-appswitch": {
                        int i = MonkeySourceRandom.FACTOR_APPSWITCH;
                        mFactors[i] = -nextOptionLong("app switch events percentage");
                        break;
                    }
                    case "--pct-flip": {
                        int i = MonkeySourceRandom.FACTOR_FLIP;
                        mFactors[i] = -nextOptionLong("keyboard flip percentage");
                        break;
                    }
                    case "--pct-anyevent": {
                        int i = MonkeySourceRandom.FACTOR_ANYTHING;
                        mFactors[i] = -nextOptionLong("any events percentage");
                        break;
                    }
                    case "--pct-pinchzoom": {
                        int i = MonkeySourceRandom.FACTOR_PINCHZOOM;
                        mFactors[i] = -nextOptionLong("pinch zoom events percentage");
                        break;
                    }
                    case "--pct-permission": {
                        int i = MonkeySourceRandom.FACTOR_PERMISSION;
                        mFactors[i] = -nextOptionLong("runtime permission toggle events percentage");
                        break;
                    }
                    case "--pkg-blacklist-file":
                        mPkgBlacklistFile = nextOptionData();
                        break;
                    case "--pkg-whitelist-file":
                        mPkgWhitelistFile = nextOptionData();
                        break;
                    case "--act-blacklist-file":
                        mActBlacklistFile = nextOptionData();
                        break;
                    case "--act-whitelist-file":
                        mActWhitelistFile = nextOptionData();
                        break;
                    case "--throttle":
                        mThrottle = nextOptionLong("delay (in milliseconds) to wait between events");
                        if (mThrottle == 0) {
                            mThrottle = 100;
                        }
                        break;
                    case "--randomize-throttle":
                        mRandomizeThrottle = true;
                        break;
                    case "--wait-dbg":
                        // do nothing - it's caught at the very start of run()
                        break;
                    case "--dbg-no-events":
                        mSendNoEvents = true;
                        break;
                    case "--port":
                        mServerPort = (int) nextOptionLong("Server port to listen on for commands");
                        break;
                    case "--setup":
                        mSetupFileName = nextOptionData();
                        break;
                    case "-f":
                        mScriptFileNames.add(nextOptionData());
                        break;
                    case "--profile-wait":
                        mProfileWaitTime = nextOptionLong(
                                "Profile delay" + " (in milliseconds) to wait between user action");
                        break;
                    case "--device-sleep-time":
                        mDeviceSleepTime = nextOptionLong("Device sleep time" + "(in milliseconds)");
                        break;
                    case "--randomize-script":
                        mRandomizeScript = true;
                        break;
                    case "--script-log":
                        mScriptLog = true;
                        break;
                    case "--bugreport":
                        mRequestBugreport = true;
                        break;
                    case "--top-activity":
                        getSystemInterfaces();
                        AndroidDevice.initializeAndroidDevice(mAm, mWm, mPm, ime);
                        ComponentName componentName = AndroidDevice.getTopActivityComponentName();
                        if (componentName!=null){
                            Logger.println("Top activity name is:"+componentName.getClassName());
                            Logger.println("Top package name is:"+componentName.getPackageName());
                            Logger.println("Top short activity name is:"+componentName.getShortClassName());
                        }
                        return false;
                    case "--periodic-bugreport":
                        mGetPeriodicBugreport = true;
                        mBugreportFrequency = nextOptionLong("Number of iterations");
                        break;
                    case "--permission-target-system":
                        mPermissionTargetSystem = true;
                        break;
                    case "--output-directory":
                        String outputdir = nextOptionData();
                        mOutputDirectory = createOutputDirectory(outputdir);
                        break;
                    case "--resMapping-file":
                        mMappingFilePath = nextOptionData();
                        break;
                    case "--app-version":
                        appVersionCode = nextOptionData();
                        break;
                    case "--ime":
                        ime = nextOptionData();
                        break;
                    case "-h":
                        showUsage();
                        return false;
                    default:
                        System.err.println("** Error: Unknown option: " + opt);
                        showUsage();
                        return false;
                }
            }
            MonkeyUtils.getPackageFilter().addValidPackages(validPackages);
        } catch (RuntimeException ex) {
            System.err.println("** Error: " + ex.toString());
            showUsage();
            return false;
        }

        // If a server port hasn't been specified, we need to specify
        // a count
        if (mServerPort == -1) {
            if (mRunningMillis < 0) { // Ignore count if running time i
                String countStr = nextArg();
                if (countStr == null) {
                    System.err.println("** Error: Count not specified");
                    showUsage();
                    return false;
                }

                try {
                    mCount = Integer.parseInt(countStr);
                } catch (NumberFormatException e) {
                    System.err.println("** Error: Count is not a number");
                    showUsage();
                    return false;
                }
            }
        }

        // If no parameter defines output dir, automatically create "/sdcard/fastbot-app-running-mines-xx."
        if (mOutputDirectory == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("/sdcard/fastbot-");
            for (String pkg : validPackages) {
                sb.append(pkg);
                sb.append('-');
            }
            if (mRunningMillis > 0) {
                sb.append("-running-minutes-");
                sb.append(mRunningMillis / ONE_MINUTE_IN_MILLISECOND);
            } else {
                sb.append("-count-");
                sb.append(mCount);
            }
            mOutputDirectory = createOutputDirectory(sb.toString());
        }

        return true;
    }

    /**
     * create output directory
     *
     * @param string directory
     * @return
     */
    private File createOutputDirectory(String string) {
        File file = new File(string);
        if (file.exists()) {
            int count = 1;
            File newFile = new File(file.getAbsolutePath() + "." + count);
            while (newFile.exists()) {
                count++;
                newFile = new File(file.getAbsolutePath() + "." + count);
            }
            Logger.format("Rename %s to %s", file, newFile);
            if(file.renameTo(newFile)){
                Logger.format("Rename %s to %s succeed", file, newFile);
            }else{
                Logger.format("Rename %s to %s failed", file, newFile);
            }
        }
        if (file.exists()) {
            throw new IllegalStateException("Cannot rename file " + file);
        }
        if (!file.mkdirs()) {
            throw new IllegalStateException("Cannot mkdirs at file " + file);
        }
        return file;
    }


    /**
     * Load package blacklist or whitelist (if specified).
     *
     * @return Returns false if any error occurs.
     */
    private boolean loadPackageLists() {
        if (((mPkgWhitelistFile != null) || (MonkeyUtils.getPackageFilter().hasValidPackages()))
                && (mPkgBlacklistFile != null)) {
            Logger.errorPrintln("Error: you can not specify a package blacklist "
                    + "together with a whitelist or individual packages (via -p).");
            return false;
        }
        Set<String> validPackages = new HashSet<>();
        if ((mPkgWhitelistFile != null) && (!loadPackageListFromFile(mPkgWhitelistFile, validPackages))) {
            return false;
        }
        MonkeyUtils.getPackageFilter().addValidPackages(validPackages);
        Set<String> invalidPackages = new HashSet<>();
        if ((mPkgBlacklistFile != null) && (!loadPackageListFromFile(mPkgBlacklistFile, invalidPackages))) {
            return false;
        }
        MonkeyUtils.getPackageFilter().addInvalidPackages(invalidPackages);
        return true;
    }

    /**
     * Check for any internal configuration (primarily build-time) errors.
     *
     * @return Returns true if ready to rock.
     */
    private boolean checkInternalConfiguration() {
        return true;
    }

    /**
     * Attach to the required system interfaces.
     *
     * @return Returns true if all system interfaces were available.
     */
    private boolean getSystemInterfaces() {
        mAm = APIAdapter.getActivityManager();
        if (mAm == null) {
            Logger.errorPrintln("Error: Unable to connect to activity manager; is the system " + "running?");
            return false;
        }

        mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        if (mWm == null) {
            Logger.errorPrintln("Error: Unable to connect to window manager; is the system " + "running?");
            return false;
        }

        mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (mPm == null) {
            Logger.errorPrintln("Error: Unable to connect to package manager; is the system " + "running?");
            return false;
        }
        APIAdapter.setActivityController(mAm, new ActivityController());
        return true;
    }


    /**
     * Using the restrictions provided (categories & packages), generate a list
     * of activities that we can actually switch to.
     *
     * @return Returns true if it could successfully build a list of target
     * activities
     */
    private boolean getMainApps() {
        Context systemContext = ContextUtils.getSystemContext();
        if (systemContext == null){
            return false;
        }
        PackageManager packageManager = systemContext.getPackageManager();
        if (mMainIntentAction != null && mMainIntentData != null) {
            Intent intent = new Intent(mMainIntentAction);
            Uri uri = Uri.parse(mMainIntentData);
            intent.setData(uri);
            List<ResolveInfo> mainApps = APIAdapter.queryIntentActivities(packageManager, intent);

            final int NA = mainApps.size();
            for (int a = 0; a < NA; a++) {
                ResolveInfo r = mainApps.get(a);
                String packageName = r.activityInfo.applicationInfo.packageName;

                if (MonkeyUtils.getPackageFilter().checkEnteringPackage(packageName)) {
                    if (mVerbose >= 2) { // very verbose
                        Logger.println("//   + Using main activity " + r.activityInfo.name + " (from package "
                                + packageName + ")");
                    }
                    mMainApps.add(new ComponentName(packageName, r.activityInfo.name));
                } else {
                    if (mVerbose >= 2) { // very very verbose
                        Logger.println("//   - NOT USING main activity " + r.activityInfo.name
                                + " (from package " + packageName + ")");
                    }
                }
            }
        } else {
            final int N = mMainCategories.size();
            for (int i = 0; i < N; i++) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                String category = mMainCategories.get(i);
                if (category.length() > 0) {
                    intent.addCategory(category);
                }
                List<ResolveInfo> mainApps = APIAdapter.queryIntentActivities(packageManager, intent);

                if (mainApps == null || mainApps.size() == 0) {
                    Logger.warningPrintln("// Warning: no activities found for category " + category);
                    continue;
                }
                if (mVerbose >= 2) { // very verbose
                    Logger.println("// Selecting main activities from category " + category);
                }
                final int NA = mainApps.size();
                for (int a = 0; a < NA; a++) {
                    ResolveInfo r = mainApps.get(a);
                    String packageName = r.activityInfo.applicationInfo.packageName;
                    if (MonkeyUtils.getPackageFilter().checkEnteringPackage(packageName)) {
                        if (mVerbose >= 2) { // very verbose
                            Logger.println("//   + Using main activity " + r.activityInfo.name + " (from package "
                                    + packageName + ")");
                        }
                        mMainApps.add(new ComponentName(packageName, r.activityInfo.name));
                    } else {
                        if (mVerbose >= 2) { // very very verbose
                            Logger.println("//   - NOT USING main activity " + r.activityInfo.name
                                    + " (from package " + packageName + ")");
                        }
                    }
                }
            }
        }

        if (mMainApps.size() == 0) {
            Logger.warningPrintln("** No activities found to run, monkey aborted.");
            return false;
        }

        return true;
    }

    /**
     * Run mCount cycles and see if we hit any crashers.
     *
     * @return Returns the last cycle which executed. If the value == mCount, no
     * errors detected.
     */
    private int runMonkeyCycles() {
        int eventCounter = 0;
        int cycleCounter = 0;

        boolean shouldReportAnrTraces = false;
        boolean shouldReportDumpsysMemInfo = false;
        boolean shouldAbort = false;
        boolean systemCrashed = false;

        long currentTime = SystemClock.elapsedRealtime();
        if (mRunningMillis > 0) {
            mEndTime = currentTime + mRunningMillis;

            // We report everything
            mIgnoreCrashes = false;
            mIgnoreTimeouts = false;
            mKillProcessAfterError = true;
        }

        // TO DO : The count should apply to each of the script file.
        while (!systemCrashed) {
            // Check user specified stopping condition
            if (mRunningMillis < 0) {
                if (cycleCounter >= mCount) {
                    break;
                }
            } else {
                currentTime = SystemClock.elapsedRealtime();
                if (currentTime > mEndTime) {
                    break;
                }
            }

            synchronized (this) {
                if (mRequestHookAppCrashed) {
                    mRequestHookAppCrashed = false;
                    shouldHookAppCrashed = true;
                }
                if (mRequestHookAppNotResponding) {
                    mRequestHookAppNotResponding = false;
                    shouldHookAppNotResponding = true;
                }
                if (mRequestAnrTraces) {
                    mRequestAnrTraces = false;
                    shouldReportAnrTraces = true;
                }
                if (mRequestAnrBugreport) {
                    getBugreport("anr_" + mReportProcessName + "_");
                    mRequestAnrBugreport = false;
                }
                if (mRequestWatchdogBugreport) {
                    Logger.println("Print the watchdog report");
                    getBugreport("anr_watchdog_");
                    mRequestWatchdogBugreport = false;
                }
                if (mRequestAppCrashBugreport) {
                    getBugreport("app_crash" + mReportProcessName + "_");
                    mRequestAppCrashBugreport = false;
                }
                if (mRequestPeriodicBugreport) {
                    getBugreport("Bugreport_");
                    mRequestPeriodicBugreport = false;
                }
                if (mRequestDumpsysMemInfo) {
                    mRequestDumpsysMemInfo = false;
                    shouldReportDumpsysMemInfo = true;
                }
                if (mMonitorNativeCrashes) {
                    mMonitorNativeCrashes = false;
                    // first time through, when eventCounter == 0, just set up
                    // the watcher (ignore the error)
                    if (checkNativeCrashes() && (eventCounter > 0)) {
                        Logger.println("** New native crash detected.");
                        if (mRequestBugreport) {
                            getBugreport("native_crash_");
                        }
                        mAbort = mAbort || !mIgnoreNativeCrashes || mKillProcessAfterError;
                    }
                }
                if (mAbort) {
                    shouldAbort = true;
                }
                if (mWatchdogWaiting) {
                    mWatchdogWaiting = false;
                    notifyAll();
                }
            }

            // Report ANR, dumpsys after releasing lock on this.
            // This ensures the availability of the lock to Activity
            // controller's appNotResponding
            if (shouldReportAnrTraces) {
                shouldReportAnrTraces = false;
                reportAnrTraces();
            }

            if (shouldReportDumpsysMemInfo) {
                shouldReportDumpsysMemInfo = false;
                reportDumpsysMemInfo();
            }

            if (shouldHookAppCrashed) {
                shouldHookAppCrashed = false;
                processCrashed();
            }

            if (shouldHookAppNotResponding) {
                shouldHookAppNotResponding = false;
                processANR();
            }

            if (shouldAbort) {
                shouldAbort = false;
                if (mRunningMillis > 0) {
                    mAbort = false;
                    Logger.println("Encounter abort but we are in continuous mode.");
                } else {
                    Logger.println("** Monkey aborted due to error.");
                    Logger.println("Events injected: " + eventCounter);
                    return eventCounter;
                }
            }

            // In this debugging mode, we never send any events. This is
            // primarily here so you can manually test the package or category
            // limits, while manually exercising the system.
            if (mSendNoEvents) {
                eventCounter++;
                cycleCounter++;
                continue;
            }

            if ((mVerbose > 0) && (eventCounter % 100) == 0 && eventCounter != 0) {
                String calendarTime = MonkeyUtils.toCalendarTime(System.currentTimeMillis());
                long systemUpTime = SystemClock.elapsedRealtime();
                Logger.println("    //[calendar_time:" + calendarTime + " system_uptime:" + systemUpTime + "]");
                Logger.println("    // Sending event #" + eventCounter);
            }

            // generate next event and inject it
            MonkeyEvent ev = mEventSource.getNextEvent();
            if (ev != null) {
                int injectCode = ev.injectEvent(mWm, mAm, mVerbose);
                if (injectCode == MonkeyEvent.INJECT_FAIL) {
                    Logger.println("    // Injection Failed " + ev);
                    if (ev instanceof MonkeyKeyEvent) {
                        mDroppedKeyEvents++;
                    } else if (ev instanceof MonkeyMotionEvent) {
                        mDroppedPointerEvents++;
                    } else if (ev instanceof MonkeyFlipEvent) {
                        mDroppedFlipEvents++;
                    } else if (ev instanceof MonkeyRotationEvent) {
                        mDroppedRotationEvents++;
                    }
                } else if (injectCode == MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION) {
                    systemCrashed = true;
                    Logger.warningPrintln("** Error: RemoteException while injecting event.");
                } else if (injectCode == MonkeyEvent.INJECT_ERROR_SECURITY_EXCEPTION) {
                    systemCrashed = !mIgnoreSecurityExceptions;
                    if (systemCrashed) {
                        Logger.warningPrintln("** Error: SecurityException while injecting event.");
                    }
                }

                // Don't count throttling as an event.
                if (!(ev instanceof MonkeyThrottleEvent)) {
                    eventCounter++;
                    if (mCountEvents) {
                        cycleCounter++;
                    }
                }
            } else {
                if (!mCountEvents) {
                    cycleCounter++;
                    writeScriptLog(cycleCounter);
                    // Capture the bugreport after n iteration
                    if (mGetPeriodicBugreport) {
                        if ((cycleCounter % mBugreportFrequency) == 0) {
                            mRequestPeriodicBugreport = true;
                        }
                    }
                } else {
                    // Event Source has signaled that we have no more events to
                    // process
                    break;
                }
            }
        }
        Logger.println("Events injected: " + eventCounter);
        return eventCounter;
    }

    /**
     * Send SIGNAL_USR1 to all processes. This will generate large (5mb)
     * profiling reports in data/misc, so use with care.
     */
    public void signalPersistentProcesses() {
        try {
            mAm.signalPersistentProcesses(Process.SIGNAL_USR1);

            synchronized (this) {
                wait(20000);
            }
        } catch (RemoteException e) {
            Logger.warningPrintln("** Failed talking with activity manager!");
        } catch (InterruptedException e) {
            Logger.warningPrintln("** InterruptedException happened. Failed talking with activity manager!");
        }
    }

    /**
     * Watch for appearance of new tombstone files, which indicate native
     * crashes.
     *
     * @return Returns true if new files have appeared in the list
     */
    private boolean checkNativeCrashes() {
        String[] tombstones = TOMBSTONES_PATH.list();

        // shortcut path for usually empty directory, so we don't waste even
        // more objects
        if ((tombstones == null) || (tombstones.length == 0)) {
            mTombstones = null;
            return false;
        }

        // use set logic to look for new files
        HashSet<String> newStones = new HashSet<String>(Arrays.asList(tombstones));

        boolean result = (mTombstones == null) || !mTombstones.containsAll(newStones);

        // keep the new list for the next time
        mTombstones = newStones;

        return result;
    }

    /**
     * Return the next command line option. This has a number of special cases
     * which closely, but not exactly, follow the POSIX command line options
     * patterns:
     *
     * <pre>
     * -- means to stop processing additional options
     * -z means option z
     * -z ARGS means option z with (non-optional) arguments ARGS
     * -zARGS means option z with (optional) arguments ARGS
     * --zz means option zz
     * --zz ARGS means option zz with (non-optional) arguments ARGS
     * </pre>
     * <p>
     * Note that you cannot combine single letter options; -abc != -a -b -c
     *
     * @return Returns the option string, or null if there are no more options.
     */
    private String nextOption() {
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[mNextArg];
        if (!arg.startsWith("-")) {
            return null;
        }
        mNextArg++;
        if (arg.equals("--")) {
            return null;
        }
        if (arg.length() > 1 && arg.charAt(1) != '-') {
            if (arg.length() > 2) {
                mCurArgData = arg.substring(2);
                return arg.substring(0, 2);
            } else {
                mCurArgData = null;
                return arg;
            }
        }
        mCurArgData = null;
        return arg;
    }

    /**
     * Return the next data associated with the current option.
     *
     * @return Returns the data string, or null of there are no more arguments.
     */
    private String nextOptionData() {
        if (mCurArgData != null) {
            return mCurArgData;
        }
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String data = mArgs[mNextArg];
        mNextArg++;
        return data;
    }

    /**
     * Returns a long converted from the next data argument, with error handling
     * if not available.
     *
     * @param opt The name of the option.
     * @return Returns a long converted from the argument.
     */
    private long nextOptionLong(final String opt) {
        long result;
        try {
            result = Long.parseLong(nextOptionData());
        } catch (NumberFormatException e) {
            Logger.println(opt + " is not a number");
            throw e;
        }
        return result;
    }

    /**
     * Return the next argument on the command line.
     *
     * @return Returns the argument string, or null if we have reached the end.
     */
    private String nextArg() {
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[mNextArg];
        mNextArg++;
        return arg;
    }

    /**
     * Dump log/crash log
     *
     * @param logTimeStamp the timestamp of the log
     * @param msg       some information，etc crash stack
     */
    private void writeDumpLog(String logTimeStamp, String msg) {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(new File(mOutputDirectory + "/" + logTimeStamp + "/", logTimeStamp + ".log").getAbsolutePath(), true);
            fileWriter.write(String.format("%s\n", msg));
        } catch (IOException e) {
            Logger.println("cannot write dump msg to " + mOutputDirectory + "/" + logTimeStamp + "/" + logTimeStamp + ".log");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.println("cannot close dump filewriter");
                }
            }
        }

        try {
            fileWriter = new FileWriter(new File("/sdcard/", "crash-dump.log").getAbsolutePath(), true);
            fileWriter.write(String.format("%s\n", msg));
        } catch (IOException e) {
            Logger.println("cannot write dump msg to " + "/sdcard/crash-dump.log");
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Logger.println("cannot close dump filewriter");
                }
            }
        }
    }

    /**
     * Load activity blacklist or whitelist (if specified).
     *
     * @return Returns false if any error occurs.
     */
    private Boolean loadActivityLists() {
        if ((mActWhitelistFile != null || MonkeyUtils.getActivityFilter().hasValidActivities()) && mActBlacklistFile != null) {
            Logger.warningPrintln("// : you can not specify a activity blacklist together with a whitelist or individual activitys (via -p).");
            return false;
        }

        Set<String> valid = new HashSet<>();
        if (mActWhitelistFile != null && !loadPackageListFromFile(mActWhitelistFile, valid)) {
            return false;
        }
        MonkeyUtils.getActivityFilter().addValidActivities(valid);

        Set<String> invalid = new HashSet<>();
        if (mActBlacklistFile != null && !loadPackageListFromFile(mActBlacklistFile, invalid)) {
            return false;
        }
        MonkeyUtils.getActivityFilter().addInvalidActivities(invalid);

        return true;
    }

    /**
     * Adding user-defined valid activity
     *
     * @return Returns false if any error occurs.
     */
    private boolean addLauncherToValidActivity() {
        if (mActWhitelistFile != null) {
            Set<String> valid = new HashSet<>();
            for (ComponentName cn : mMainApps) {
                String className = cn.getClassName();
                valid.add(className);
                Logger.println(" // Add Launcher " + className + " To ActivityWhiteList");
            }
            MonkeyUtils.getActivityFilter().addValidActivities(valid);
        }
        return true;
    }

    /**
     * Crash handling process, crash reporting, crash logging, logcat logging
     */
    private void processCrashed() {
        String tag = "_Crash_";
        String symbolStr = ("" + crashInfo.process).replace(":", "_") + "_" + mStartTime + tag + crashInfo.time;
        String output = mOutputDirectory + "/" + symbolStr + "/";
        File file = new File(output);
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalStateException("Cannot mkdirs at file " + output);
        }
        writeDumpLog(symbolStr, crashInfo.stack);

        if (requestLogcat) {
            reportLogcat(crashInfo.time);
        }
        crashInfo = new CrashInfo();
    }

    /**
     * Anr handling process, anr reporting, anr logging, logcat logging
     */
    private void processANR() {
        String tag = "_Anr_";
        String symbolStr = ("" + anrInfo.process).replace(":", "_") + "_" + mStartTime + tag + anrInfo.time;
        String output = mOutputDirectory + "/" + symbolStr + "/";
        File file = new File(output);
        if (!file.exists() && !file.mkdirs()) {
            throw new IllegalStateException("Cannot mkdirs at file " + output);
        }
        writeDumpLog(symbolStr, anrInfo.stack);

        if (requestLogcat) {
            reportLogcat(anrInfo.time);
        }
        anrInfo = new ANRInfo();
    }

    /**
     * Print how to use this command.
     */
    private void showUsage() {
        String usage = "usage: monkey [-p ALLOWED_PACKAGE [-p ALLOWED_PACKAGE] ...]\n" +
                "              [-c MAIN_CATEGORY [-c MAIN_CATEGORY] ...]\n" +
                "              [--ignore-crashes] [--ignore-timeouts]\n" +
                "              [--ignore-security-exceptions]\n" +
                "              [--agent [AGENT_TYPE(walk,stat)]]\n" +
                "              [--running-minutes MINUTES\n" +
                "              [--monitor-native-crashes] [--ignore-native-crashes]\n" +
                "              [--kill-process-after-error] [--hprof]\n" +
                "              [--pct-touch PERCENT] [--pct-motion PERCENT]\n" +
                "              [--pct-trackball PERCENT] [--pct-syskeys PERCENT]\n" +
                "              [--pct-nav PERCENT] [--pct-majornav PERCENT]\n" +
                "              [--pct-appswitch PERCENT] [--pct-flip PERCENT]\n" +
                "              [--pct-anyevent PERCENT] [--pct-pinchzoom PERCENT]\n" +
                "              [--pct-permission PERCENT]\n" +
                "              [--pkg-blacklist-file PACKAGE_BLACKLIST_FILE]\n" +
                "              [--pkg-whitelist-file PACKAGE_WHITELIST_FILE]\n" +
                "              [--wait-dbg] [--dbg-no-events]\n" +
                "              [--setup scriptfile] [-f scriptfile [-f scriptfile] ...]\n" +
                "              [--port port]\n" +
                "              [-s SEED] [-v [-v] ...]\n" +
                "              [--throttle MILLISEC] [--randomize-throttle]\n" +
                "              [--profile-wait MILLISEC]\n" +
                "              [--device-sleep-time MILLISEC]\n" +
                "              [--randomize-script]\n" +
                "              [--top-activity]\n" +
                "              [--script-log]\n" +
                "              [--bugreport]\n" +
                "              [--periodic-bugreport]\n" +
                "              [--permission-target-system]\n" +
                "              COUNT\n";
        System.err.println(usage);
    }

    /**
     * Crash info
     */
    public static class CrashInfo {
        public String process = "";
        public String stack = "";
        public String time = "";

        public CrashInfo() {
        }
    }

    /**
     * Anr info
     */
    public static class ANRInfo {
        public String process = "";
        public String stack = "";
        public String time = "";

        public ANRInfo() {
        }
    }

    /**
     * Monitor operations happening in the system.
     */
    private class ActivityController extends IActivityController.Stub {

        public boolean activityStarting(Intent intent, String pkg) {

            if (allowStartActivityEscapeAny && ("".equals(allowStartActivityEscapePackageName) || pkg.equals(allowStartActivityEscapePackageName))) {
                return true;
            }

            if (intent.getComponent() != null) {
                if (pkg.equals("com.android.systemui")) {
                    if (intent.getComponent().getClassName().equals("com.android.systemui.recents.RecentsActivity")) {
                        if (mVerbose > 2)
                            Logger.infoPrintln("activityStarting allowing RecentsActivity");
                        return true;
                    }
                }
            }

            if (intent.getComponent() != null) {
                String className = intent.getComponent().getClassName();
                if ("com.android.packageinstaller.permission.ui.GrantPermissionsActivity".equals(className)
                        || "com.android.permissioncontroller.permission.ui.GrantPermissionsActivity".equals(className)) {
                    if (mVerbose > 2)
                        Logger.infoPrintln("activityStarting allowing GrantPermissionsActivity");
                    return true;
                }
            }

            if (intent.getComponent() != null) {
                String className = intent.getComponent().getClassName();
                if (AndroidDevice.isAtPhoneLauncher(className)) {
                    if (mVerbose > 2)
                        Logger.infoPrintln("activityStarting allowing PhoneLanucherActivity: " + className);
                    return true;
                }
                if (AndroidDevice.isAtPhoneCapture(className)) {
                    if (mVerbose > 2)
                        Logger.infoPrintln("activityStarting allowing PhoneCaptureActivity: " + className);
                    return true;
                }
            }

            boolean allowActivity = true;

            if (MonkeyUtils.getActivityFilter().hasValidActivities() || MonkeyUtils.getActivityFilter().hasInvalidActivities()) {
                String currentActivity = null;
                try {
                    if (intent != null) {
                        ComponentName cn = intent.getComponent();
                        if (cn != null) {
                            currentActivity = cn.getClassName();
                        }

                    }
                } catch (Exception e) {
                }
                if (currentActivity != null) {
                    allowActivity = MonkeyUtils.getActivityFilter().checkEnteringActivity(currentActivity);
                    if (mVerbose > 2)
                        Logger.println("// Activity: " + currentActivity + " in Intent");
                }
            }

            boolean allow = MonkeyUtils.getPackageFilter().checkEnteringPackage(pkg) && allowActivity || (DEBUG_ALLOW_ANY_STARTS != 0);


            if (mVerbose > 0) {
                // StrictMode's disk checks end up catching this on
                // userdebug/eng builds due to PrintStream going to a
                // FileOutputStream in the end (perhaps only when
                // redirected to a file?) So we allow disk writes
                // around this region for the monkey to minimize
                // harmless dropbox uploads from monkeys.
                StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
                Logger.println(
                        "    // " + (allow ? "Allowing" : "Rejecting") + " start of " + intent + " in package " + pkg);
                StrictMode.setThreadPolicy(savedPolicy);
            }
            return allow;
        }

        public boolean activityResuming(String pkg) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger.println("    // activityResuming(" + pkg + ")");
            boolean allow = MonkeyUtils.getPackageFilter().checkEnteringPackage(pkg) || (DEBUG_ALLOW_ANY_STARTS != 0);

            if (!allow) {
                if (mVerbose > 0) {
                    Logger.println("    // " + (allow ? "Allowing" : "Rejecting") + " resume of package " + pkg);
                }
            }
            StrictMode.setThreadPolicy(savedPolicy);
            return allow;
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis,
                                  String stackTrace) {
            if (!AndroidDevice.isAppCrash(processName, mMainApps)) {
                Logger.println("// crash processName: " + processName + ", is not testing app");
                return false;
            }

            if (pid == lastCrashPid) {
                Logger.println("// continuous crash! ");
                return false;
            }

            if (longMsg != null && longMsg.contains("[TRUNCATED")) {
                Logger.println("// this crash have [truncated, slardar can't parse! ");
                return false;
            }

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger.errorPrintln("// CRASH: " + processName + " (pid " + pid + ") (elapsed nanos: "
                    + SystemClock.elapsedRealtimeNanos() + ")");
            Logger.errorPrintln("// Version: " + appVersionCode);
            Logger.errorPrintln("// Short Msg: " + shortMsg);
            Logger.errorPrintln("// Long Msg: " + longMsg);
            Logger.errorPrintln("// " + stackTrace.replace("\n", "\n" + Logger.TAG + "// "));

            StrictMode.setThreadPolicy(savedPolicy);

            if (!mIgnoreCrashes || mRequestBugreport) {
                synchronized (Monkey.this) {
                    crashCount++;
                    lastCrashPid = pid;

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String dateStr = dateFormat.format(timeMillis);
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                    String timeStr = df.format(timeMillis);

                    sb.append("\n").append(timeStr);
                    sb.append("\ncrash:");
                    sb.append("\n// CRASH: ").append(processName).append(" (pid ").append(pid).append(") (dump time: ").append(dateStr);
                    sb.append("\n// Version: ").append(appVersionCode);
                    sb.append("\n// Long Msg: ").append(stackTrace.replace("\n", "\n// "));
                    sb.append("crash end\n");

                    crashInfo.process = processName;
                    crashInfo.stack = sb.toString();
                    crashInfo.time = timeStr;
                    mRequestHookAppCrashed = true;

                    if (!mIgnoreCrashes) {
                        mAbort = true;
                    }
                    if (mRequestBugreport) {
                        mRequestAppCrashBugreport = true;
                        mReportProcessName = processName;
                    }
                }
                return !mKillProcessAfterError;
            }
            return false;
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            return 0;
        }

        public int appNotResponding(String processName, int pid, String processStats) {
            if (!AndroidDevice.isAppCrash(processName, mMainApps)) {
                Logger.println("// crash processName: " + processName + ", is not testing app");
                return (mKillProcessAfterError) ? -1 : 1;
            }

            if (pid == lastCrashPid) {
                Logger.println("// continuous crash! ");
                return (mKillProcessAfterError) ? -1 : 1;
            }

            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger.errorPrintln("NOT RESPONDING: " + processName + " (pid " + pid + ")");
            Logger.errorPrintln(processStats);
            StrictMode.setThreadPolicy(savedPolicy);

            synchronized (Monkey.this) {
                oomCount++;
                lastCrashPid = pid;
                long timeMillis = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                String dateStr = dateFormat.format(timeMillis);

                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                String timeStr = df.format(timeMillis);

                sb.append("\n").append(timeStr);
                sb.append("\nanr:");
                sb.append("\n// ANR: ").append(processName).append(" (pid ").append(pid).append(") (dump time: ").append(dateStr);
                sb.append("\n// Version: ").append(appVersionCode);
                sb.append("\n// NOT RESPONDING: ").append(processName).append(" (pid ").append(pid).append(") (stat ").append(processStats);
                sb.append("anr end\n");

                anrInfo.process = processName;
                anrInfo.stack = sb.toString();
                anrInfo.time = timeStr;

                mRequestHookAppNotResponding = true;
                mRequestAnrTraces = true;
                mRequestDumpsysMemInfo = true;
                if (mRequestBugreport) {
                    mRequestAnrBugreport = true;
                    mReportProcessName = processName;
                }
            }
            if (!mIgnoreTimeouts) {
                synchronized (Monkey.this) {
                    mAbort = true;
                }
            }
            return (mKillProcessAfterError) ? -1 : 1;
        }

        public int systemNotResponding(String message) {
            StrictMode.ThreadPolicy savedPolicy = StrictMode.allowThreadDiskWrites();
            Logger.errorPrintln("WATCHDOG: " + message);
            StrictMode.setThreadPolicy(savedPolicy);

            synchronized (Monkey.this) {
                if (!mIgnoreCrashes) {
                    mAbort = true;
                }
                if (mRequestBugreport) {
                    mRequestWatchdogBugreport = true;
                }
                mWatchdogWaiting = true;
            }
            synchronized (Monkey.this) {
                while (mWatchdogWaiting) {
                    try {
                        Monkey.this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return (mKillProcessAfterError) ? -1 : 1;
        }
    }

}

//may the force be with you
