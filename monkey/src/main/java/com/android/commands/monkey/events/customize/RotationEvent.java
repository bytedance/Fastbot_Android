/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events.customize;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.events.base.MonkeyRotationEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Zhao Zhang
 */

public class RotationEvent extends AbstractCustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public final int rotationDegree;
    public final boolean persist;

    public RotationEvent(int degree, boolean persist) {
        this.rotationDegree = degree;
        this.persist = persist;
    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        boolean persist = jEvent.getBoolean("persist");
        int degree = jEvent.getInt("degree");
        return new RotationEvent(degree, persist);
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        return Collections.<MonkeyEvent>singletonList(new MonkeyRotationEvent(rotationDegree, persist));
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "r");
        jEvent.put("degree", rotationDegree);
        jEvent.put("persist", persist);
        return jEvent;
    }
}
