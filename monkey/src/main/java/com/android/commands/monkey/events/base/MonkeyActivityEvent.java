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

import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
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
public class MonkeyActivityEvent extends MonkeyEvent {
    long mAlarmTime = 0;
    private ComponentName mApp;
    private boolean startByHistory = false;

    public MonkeyActivityEvent(ComponentName app) {
        super(EVENT_TYPE_ACTIVITY);
        mApp = app;
    }

    public MonkeyActivityEvent(ComponentName app, boolean startByHistory) {
        super(EVENT_TYPE_ACTIVITY);
        mApp = app;
        this.startByHistory = startByHistory;

    }


    public MonkeyActivityEvent(ComponentName app, long arg) {
        super(EVENT_TYPE_ACTIVITY);
        mApp = app;
        mAlarmTime = arg;
    }

    /**
     * @return Intent for the new activity
     */
    /* private */ Intent getEvent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(mApp);
        if (startByHistory) {
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
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
