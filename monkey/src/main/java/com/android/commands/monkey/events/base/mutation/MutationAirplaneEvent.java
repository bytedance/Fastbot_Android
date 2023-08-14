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

package com.android.commands.monkey.events.base.mutation;

import android.app.IActivityManager;
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.utils.Logger;
import com.android.commands.monkey.utils.StoneUtils;

/**
 * @author Dingchun Wang
 */

public class MutationAirplaneEvent extends MonkeyEvent {

    private boolean isOpen = false;

    public MutationAirplaneEvent() {
        super(EVENT_TYPE_COMMON);
        isOpen = isAirModeOpen();

    }

    private boolean isAirModeOpen() {
        String result = StoneUtils.executeShellCommand("settings get global airplane_mode_on").trim();
        return !result.contains("0");
    }


    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        String cmd = isOpen ? "settings put global airplane_mode_on 0" : "settings put global airplane_mode_on 1";

        // Execute the shell command
        String[] commands = cmd.split("\\s+");
        try {
            java.lang.Process p = Runtime.getRuntime().exec(commands);
            int status = p.waitFor();

            Logger.println("Shell command " + cmd + " status was " + status);
        } catch (Exception e) {
            Logger.warningPrintln("Exception from " + cmd + ":");
            Logger.warningPrintln(e.toString());
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }


    public void resetStatusAndExecute(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (isOpen) {
            injectEvent(iwm, iam, verbose);
        }
    }

}
