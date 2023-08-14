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

public class KeyEvent extends AbstractCustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = -672478591540471589L;
    final int keyCode;
    final long waitTime;

    public KeyEvent(int keyCode) {
        this.keyCode = keyCode;
        this.waitTime = 0;
    }

    public KeyEvent(int keyCode, long waitTime) {
        this.keyCode = keyCode;
        this.waitTime = waitTime;
    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        int key = jEvent.getInt("key");
        long waitTime = 0;
        if (jEvent.has("wait")) waitTime = jEvent.getLong("wait");
        return new KeyEvent(key, waitTime);
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        MonkeyEvent down = new MonkeyKeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode);
        MonkeyEvent up = new MonkeyKeyEvent(android.view.KeyEvent.ACTION_UP, keyCode);
        MonkeyEvent throttle = new MonkeyThrottleEvent(waitTime);

        return Arrays.asList(down, up, throttle);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "k");
        jEvent.put("key", keyCode);
        jEvent.put("wait", waitTime);
        return jEvent;
    }
}
