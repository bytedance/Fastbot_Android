/*
 * Copyright 2020 Advanced Software Technologies Lab at ETH Zurich, Switzerland
 *
 * Modified - Copyright (c) 2020 Bytedance Inc.
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

package com.android.commands.monkey.action;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.fastbot.client.ActionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Generation fuzzing action, action type is FUZZ
 * @author Zhao Zhang
 */
public class FuzzAction extends Action {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger fuzzIdGenerator = new AtomicInteger();
    private final int id; // simply make every fuzz action unique.
    private final List<CustomEvent> events;

    public FuzzAction(List<CustomEvent> events) {
        super(ActionType.FUZZ);
        this.events = events;
        this.id = fuzzIdGenerator.incrementAndGet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FuzzAction other = (FuzzAction) obj;
        return id == other.id;
    }

    public List<CustomEvent> getFuzzingEvents() {
        return events;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jAction = super.toJSONObject();
        JSONArray jEvents = new JSONArray();
        for (CustomEvent event : events) {
            jEvents.put(event.toJSONObject());
        }
        jAction.put("events", jEvents);
        return jAction;
    }
}
