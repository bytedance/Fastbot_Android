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
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.utils.Logger;

/**
 * @author Zhao Zhang
 */

/**
 * Events for running the shell command.
 */
public class MonkeyCommandEvent extends MonkeyEvent {

    private String mCmd;
    private long waitTime = 0;

    public MonkeyCommandEvent(String cmd) {
        super(EVENT_TYPE_PRO_ACTIVITY);
        mCmd = cmd;
    }

    public MonkeyCommandEvent(String cmd, long waitTime) {
        super(EVENT_TYPE_PRO_ACTIVITY);
        mCmd = cmd;
        this.waitTime = waitTime;
    }

    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (mCmd != null) {
            if ("".equals(mCmd)) return MonkeyEvent.INJECT_FAIL;
            // Execute the shell command
            String[] commands = mCmd.split("\\s+");
            try {
                java.lang.Process p = Runtime.getRuntime().exec(commands);
                int status = p.waitFor();

                Logger.println("Shell command " + mCmd + " status was " + status);
            } catch (Exception e) {
                Logger.warningPrintln("Exception from " + mCmd + ":");
                Logger.warningPrintln(e.toString());
            }
        }
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
            }
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
