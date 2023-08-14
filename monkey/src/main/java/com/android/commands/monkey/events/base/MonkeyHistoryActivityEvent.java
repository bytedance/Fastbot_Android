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

package com.android.commands.monkey.events.base;

import static com.android.commands.monkey.utils.Config.quickappStartActivityIntentPutExtra;

import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.framework.AndroidDevice;
import com.android.commands.monkey.utils.Logger;

/**
 * @author Zhao Zhang
 */

/**
 * monkey activity event
 */
public class MonkeyHistoryActivityEvent extends MonkeyEvent {
    long mAlarmTime = 0;
    private ComponentName mApp;
    private String mIntentaction;
    private String mIntentdata;
    private String mQuickactivity;

    public MonkeyHistoryActivityEvent(ComponentName app, String intentaction, String intentdata, String quickactivity) {
        super(EVENT_TYPE_DATA_ACTIVITY);
        mApp = app;
        mIntentaction = intentaction;
        mIntentdata = intentdata;
        mQuickactivity = quickactivity;
    }

    public MonkeyHistoryActivityEvent(ComponentName app, long arg, String intentaction, String intentdata, String quickactivity) {
        super(EVENT_TYPE_DATA_ACTIVITY);
        mApp = app;
        mAlarmTime = arg;
        mIntentaction = intentaction;
        mIntentdata = intentdata;
        mQuickactivity = quickactivity;
    }

    /**
     * @return Intent for the new activity
     */
    /* private */ Intent getEvent() {
        Uri uri = Uri.parse(mIntentdata);
        Intent intent = new Intent(mIntentaction);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra(quickappStartActivityIntentPutExtra, mIntentdata);
        return intent;
    }

    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Intent intent = getEvent();
        if (verbose > 0) {
            Logger.println("Switch: " + intent.toUri(0));
        }

        if (mAlarmTime != 0) {
            Bundle args = new Bundle();
            args.putLong("alarmTime", mAlarmTime);
            intent.putExtras(args);
        }

        AndroidDevice.startActivity(intent);
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
