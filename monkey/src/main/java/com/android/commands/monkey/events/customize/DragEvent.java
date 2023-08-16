/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events.customize;

import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.android.commands.monkey.events.CustomEvent;
import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.events.base.MonkeyTouchEvent;
import com.android.commands.monkey.events.base.MonkeyWaitEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.android.commands.monkey.utils.Config.swipeDuration;

/**
 * @author Zhao Zhang
 */

public class DragEvent extends AbstractCustomEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    float[] values;

    public DragEvent(PointF[] points) {
        this.values = fromPointsArray(points);
        if (points.length < 2) {
            throw new IllegalArgumentException();
        }
    }

    private DragEvent(float[] values) {
        this.values = values;
    }

    public static CustomEvent fromJSONObject(JSONObject jEvent) throws JSONException {
        JSONArray jValues = jEvent.getJSONArray("values");
        float[] values = new float[jValues.length()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (float) jValues.getDouble(i);
        }
        return new DragEvent(values);
    }

    @Override
    public List<MonkeyEvent> generateMonkeyEvents() {
        int index = 0;
        PointF[] points = toPointsArray(values);
        final int size = points.length;
        List<MonkeyEvent> events = new ArrayList<MonkeyEvent>(size);
        long downAt = SystemClock.uptimeMillis();
        PointF p = points[index++];
        events.add(new MonkeyTouchEvent(MotionEvent.ACTION_DOWN).setDownTime(downAt).addPointer(0, p.x, p.y)
                .setIntermediateNote(false));

        long waitTime = swipeDuration / size;
        for (; index < size - 1; index++) {
            p = points[index];
            events.add(new MonkeyTouchEvent(MotionEvent.ACTION_MOVE).setDownTime(downAt).addPointer(0, p.x, p.y)
                    .setIntermediateNote(true));
            events.add(new MonkeyWaitEvent(waitTime));
        }
        p = points[index];
        events.add(new MonkeyTouchEvent(MotionEvent.ACTION_UP).setDownTime(downAt).addPointer(0, p.x, p.y)
                .setIntermediateNote(false));
        return events;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jEvent = new JSONObject();
        jEvent.put("type", "d");
        JSONArray jPoint = new JSONArray();
        for (float x : values) {
            jPoint.put(x);
        }
        jEvent.put("values", jPoint);
        return jEvent;
    }
}
