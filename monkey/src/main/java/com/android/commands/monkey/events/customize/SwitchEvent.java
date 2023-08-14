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

package com.android.commands.monkey.events.customize;

import android.view.KeyEvent;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.events.base.MonkeyKeyEvent;
import com.android.commands.monkey.events.base.MonkeyThrottleEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * @author Zhao Zhang
 */

public class SwitchEvent extends AbstractCustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    boolean home;

    public SwitchEvent(boolean home) {
        this.home = home;
    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        boolean home = jEvent.getBoolean("home");
        return new SwitchEvent(home);
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        MonkeyKeyEvent appSwitchDown = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_APP_SWITCH);
        MonkeyKeyEvent appSwitchUp = new MonkeyKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_APP_SWITCH);
        MonkeyKeyEvent postDown, postUp;
        MonkeyThrottleEvent throttle = new MonkeyThrottleEvent(500);
        if (home) {
            postDown = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME);
            postUp = new MonkeyKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME);
        } else {
            postDown = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
            postUp = new MonkeyKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
        }
        return Arrays.asList(appSwitchDown, appSwitchUp, throttle, postDown, postUp);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "a");
        jEvent.put("home", home);
        return jEvent;
    }
}
