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

import android.graphics.PointF;

import com.android.commands.monkey.events.CustomEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zhao Zhang
 */

public abstract class AbstractCustomEvent implements CustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static float[] fromPointsArray(PointF[] points) {
        float[] results = new float[points.length << 1];
        int index = 0;
        for (PointF p : points) {
            results[index++] = p.x;
            results[index++] = p.y;
        }
        return results;
    }

    static PointF[] toPointsArray(float[] a) {
        PointF[] results = new PointF[a.length >> 1];
        int index = 0;
        for (int i = 0; i < results.length; i++) {
            float x = a[index++];
            float y = a[index++];
            results[i] = new PointF(x, y);
        }
        return results;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", getClass().getSimpleName());
        return jEvent;
    }
}
