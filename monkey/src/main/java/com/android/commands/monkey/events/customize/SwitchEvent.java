/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
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
