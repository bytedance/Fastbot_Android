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
import android.content.Context;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.utils.Logger;

/**
 * @author Dingchun Wang
 */

public class MutationWifiEvent extends MonkeyEvent {
    private IWifiManager wifiManager;

    public MutationWifiEvent() {
        super(EVENT_TYPE_COMMON);
        wifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_SERVICE));
    }


    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (wifiManager == null) {
            return MonkeyEvent.INJECT_FAIL;
        }
        try {

            int wifiState = wifiManager.getWifiEnabledState();
            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
            String cmd = isWifiEnabled ? "svc wifi disable" : "svc wifi enable";

            // Execute the shell command
            String[] commands = cmd.split("\\s+");
            try {
                java.lang.Process p = Runtime.getRuntime().exec(commands);
                int status = p.waitFor();
                Logger.println("Shell command " + cmd + " status was " + status);
            } catch (Exception e) {
                Logger.warningPrintln("Exception from " + cmd + ":");
                Logger.warningPrintln(e.toString());
                return MonkeyEvent.INJECT_FAIL;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            return MonkeyEvent.INJECT_SUCCESS;
        } catch (
                RemoteException e) {
            e.printStackTrace();
            return MonkeyEvent.INJECT_FAIL;
        }

    }
    public void resetStatusAndExecute(IWindowManager iwm, IActivityManager iam, int verbose) {
        try {
            int wifiState = wifiManager.getWifiEnabledState();
            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
            if (!isWifiEnabled) {
                injectEvent(iwm, iam, verbose);
            }
        } catch (RemoteException e) {
            e.printStackTrace();

        }

    }

}
