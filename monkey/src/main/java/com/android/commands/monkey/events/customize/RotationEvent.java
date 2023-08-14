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
