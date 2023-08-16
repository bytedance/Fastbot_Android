/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events;

import com.android.commands.monkey.events.MonkeyEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * @author Zhao Zhang
 */

public interface CustomEvent extends Serializable {
    /**
     * According to specific customized fuzzing scenario, convert it to
     * corresponding monkey events that could be executed by origin monkey
     * @return Converted list of generated monkey events
     */
    List<MonkeyEvent> generateMonkeyEvents();

    JSONObject toJSONObject() throws JSONException;
}
