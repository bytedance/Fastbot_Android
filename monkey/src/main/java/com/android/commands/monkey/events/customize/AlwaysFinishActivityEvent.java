/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events.customize;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.events.base.mutation.MutationAlwaysFinishActivityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class AlwaysFinishActivityEvent extends AbstractCustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AlwaysFinishActivityEvent() {

    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        return new AlwaysFinishActivityEvent();
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        return Collections.<MonkeyEvent>singletonList(new MutationAlwaysFinishActivityEvent());
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "alwaysfinishActivitys");
        return jEvent;
    }
}
