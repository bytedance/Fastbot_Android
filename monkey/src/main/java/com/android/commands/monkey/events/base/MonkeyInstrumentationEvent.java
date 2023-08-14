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
import android.os.Bundle;
import android.os.RemoteException;
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.utils.Logger;

/**
 * monkey instrumentation event
 */
public class MonkeyInstrumentationEvent extends MonkeyEvent {
    String mRunnerName;
    String mTestCaseName;

    public MonkeyInstrumentationEvent(String testCaseName, String runnerName) {
        super(EVENT_TYPE_PRO_ACTIVITY);
        mTestCaseName = testCaseName;
        mRunnerName = runnerName;
    }

    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        ComponentName cn = ComponentName.unflattenFromString(mRunnerName);
        if (cn == null || mTestCaseName == null)
            throw new IllegalArgumentException("Bad component name");

        Bundle args = new Bundle();
        args.putString("class", mTestCaseName);
        try {
            iam.startInstrumentation(cn, null, 0, args, null, null, 0, null);
        } catch (RemoteException e) {
            Logger.warningPrintln("Failed talking with activity manager!");
            return MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
