/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Modified - Copyright (c) 2020 Bytedance Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.commands.monkey.utils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Misc utilities.
 */
public abstract class MonkeyUtils {

    private static final java.util.Date DATE = new java.util.Date();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
    private static PackageFilter sFilter;
    private static ActivityFilter aFilter;

    private MonkeyUtils() {
    }

    /**
     * Return calendar time in pretty string.
     */
    public static synchronized String toCalendarTime(long time) {
        DATE.setTime(time);
        return DATE_FORMATTER.format(DATE);
    }

    public static PackageFilter getPackageFilter() {
        if (sFilter == null) {
            sFilter = new PackageFilter();
        }
        return sFilter;
    }

    public static ActivityFilter getActivityFilter() {
        if (aFilter == null) {
            aFilter = new ActivityFilter();
        }
        return aFilter;
    }

    public static class PackageFilter {
        private Set<String> mValidPackages = new HashSet<>();
        private Set<String> mInvalidPackages = new HashSet<>();

        private PackageFilter() {
        }

        public Set<String> getmValidPackages() {
            return mValidPackages;
        }

        public void addValidPackages(Set<String> validPackages) {
            mValidPackages.addAll(validPackages);
        }

        public void addInvalidPackages(Set<String> invalidPackages) {
            mInvalidPackages.addAll(invalidPackages);
        }

        public boolean hasValidPackages() {
            return mValidPackages.size() > 0;
        }

        public boolean isPackageValid(String pkg) {
            return mValidPackages.contains(pkg);
        }

        public boolean isPackageInvalid(String pkg) {
            return mInvalidPackages.contains(pkg);
        }

        /**
         * Check whether we should run against the given package.
         *
         * @param pkg The package name.
         * @return Returns true if we should run against pkg.
         */
        public boolean checkEnteringPackage(String pkg) {
            if (mInvalidPackages.size() > 0) {
                return !mInvalidPackages.contains(pkg);
            } else if (mValidPackages.size() > 0) {
                return mValidPackages.contains(pkg);
            }
            return true;
        }

        public void dump() {
            if (mValidPackages.size() > 0) {
                for (String mValidPackage : mValidPackages) {
                    Logger.println("// AllowPackage: " + mValidPackage);
                }
            }
            if (mInvalidPackages.size() > 0) {
                for (String mInvalidPackage : mInvalidPackages) {
                    Logger.println("// DisallowPackage: " + mInvalidPackage);
                }
            }
        }
    }

    public static class ActivityFilter {
        private final Set<String> mValidActivities = new HashSet<>();
        private final Set<String> mInvalidActivities = new HashSet<>();

        private ActivityFilter() {
        }

        public Set<String> getmValidActivities() {
            return mValidActivities;
        }

        public Set<String> getmInvalidActivities() {
            return mInvalidActivities;
        }

        public void addValidActivities(Set<String> validActivities) {
            mValidActivities.addAll(validActivities);
        }

        public void addInvalidActivities(Set<String> invalidActivities) {
            mInvalidActivities.addAll(invalidActivities);
        }

        public boolean hasValidActivities() {
            return mValidActivities.size() > 0;
        }

        public boolean hasInvalidActivities() {
            return mInvalidActivities.size() > 0;
        }

        public boolean isActivityValid(String pkg) {
            return mValidActivities.contains(pkg);
        }

        public boolean isActivityInvalid(String pkg) {
            return mInvalidActivities.contains(pkg);
        }

        public boolean checkEnteringActivity(String activity) {
            return !isActivityInvalid(activity) && (!hasValidActivities() || isActivityValid(activity));
        }

        public void dump() {
            if (mValidActivities.size() > 0) {
                for (String mValidActivity : mValidActivities) {
                    Logger.println("// AllowActivity: " + mValidActivity);
                }
            }
            if (mInvalidActivities.size() > 0) {
                for (String mInvalidActivity : mInvalidActivities) {
                    Logger.println("// DisallowActivity: " + mInvalidActivity);
                }
            }
        }
    }
}
