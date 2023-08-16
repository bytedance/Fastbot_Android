/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events.customize;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.events.base.MonkeyCommandEvent;
import com.android.commands.monkey.events.MonkeyEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Zhao Zhang
 */

public class ShellEvent extends AbstractCustomEvent {
    private static final long serialVersionUID = 1L;
    private String command = "";
    private long waitTime = 0;

    public ShellEvent(){}

    public ShellEvent(String command) {
        this.command = command;
    }

    public ShellEvent(String command, long waitTime) {
        this.command = command;
        this.waitTime = waitTime;
    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        String com = jEvent.getString("command");
        long waitTime = 0;
        if (jEvent.has("wait")) waitTime = jEvent.getLong("wait");
        return new ShellEvent(com, waitTime);
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        MonkeyEvent ime = new MonkeyCommandEvent(command, waitTime);
        return Collections.singletonList(ime);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "s");
        jEvent.put("com", command);
        jEvent.put("wait", waitTime);
        return jEvent;
    }
}