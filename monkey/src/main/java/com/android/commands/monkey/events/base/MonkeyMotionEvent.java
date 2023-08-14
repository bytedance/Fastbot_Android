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

package com.android.commands.monkey.events.base;

import android.app.IActivityManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.MotionEvent;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.utils.Logger;

import static com.android.commands.monkey.utils.Config.bytestStatusBarHeight;

/**
 * @author Zhao Zhang
 */

/**
 * monkey motion event
 */
public abstract class MonkeyMotionEvent extends MonkeyEvent {

    private static int statusBarHeight;
    private static int bottomBarHeight;

    static {
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);

        statusBarHeight = bytestStatusBarHeight;
        if (statusBarHeight == 0) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            int w = display.getWidth();
            int h = display.getHeight();
            if (w == 1080 && h > 2100) {
                statusBarHeight = (int) (40f * dm.density);
            } else if (w == 1200 && h == 1824) {
                statusBarHeight = (int) (30f * dm.density);
            } else if (w == 1440 && h == 2696) {
                statusBarHeight = (int) (30f * dm.density);
            } else {
                statusBarHeight = (int) (24f * dm.density);
            }
            statusBarHeight += 15;
        }

        bottomBarHeight = display.getHeight() - statusBarHeight;
    }

    private long mDownTime;
    private long mEventTime;
    private int mAction;
    private SparseArray<MotionEvent.PointerCoords> mPointers;
    private int mMetaState;
    private float mXPrecision;
    private float mYPrecision;
    private int mDeviceId;
    private int mSource;
    private int mFlags;
    private int mEdgeFlags;
    private int type = 0;

    // If true, this is an intermediate step (more verbose logging, only)
    private boolean mIntermediateNote;

    protected MonkeyMotionEvent(int type, int source, int action) {
        super(type);
        mSource = source;
        mDownTime = -1;
        mEventTime = -1;
        mAction = action;
        mPointers = new SparseArray<MotionEvent.PointerCoords>();
        mXPrecision = 1;
        mYPrecision = 1;
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y) {
        return addPointer(id, x, y, 0, 0);
    }

    public MonkeyMotionEvent addPointer(int id, float x, float y, float pressure, float size) {
        MotionEvent.PointerCoords c = new MotionEvent.PointerCoords();
        c.x = x;
        if (y <= statusBarHeight) {
            y = statusBarHeight + 1;
        } else if (y >= bottomBarHeight) {
            if (type == 1) y = bottomBarHeight - 1;
        }
        c.y = y;
        c.pressure = pressure;
        c.size = size;
        mPointers.append(id, c);
        return this;
    }

    public boolean getIntermediateNote() {
        return mIntermediateNote;
    }

    public MonkeyMotionEvent setIntermediateNote(boolean b) {
        mIntermediateNote = b;
        return this;
    }

    public int getAction() {
        return mAction;
    }

    public long getDownTime() {
        return mDownTime;
    }

    public MonkeyMotionEvent setDownTime(long downTime) {
        mDownTime = downTime;
        return this;
    }

    public long getEventTime() {
        return mEventTime;
    }

    public MonkeyMotionEvent setEventTime(long eventTime) {
        mEventTime = eventTime;
        return this;
    }

    public MonkeyMotionEvent setMetaState(int metaState) {
        mMetaState = metaState;
        return this;
    }

    public MonkeyMotionEvent setPrecision(float xPrecision, float yPrecision) {
        mXPrecision = xPrecision;
        mYPrecision = yPrecision;
        return this;
    }

    public MonkeyMotionEvent setDeviceId(int deviceId) {
        mDeviceId = deviceId;
        return this;
    }

    public MonkeyMotionEvent setEdgeFlags(int edgeFlags) {
        mEdgeFlags = edgeFlags;
        return this;
    }

    public MonkeyMotionEvent setType(int type) {
        this.type = type;
        return this;
    }

    /**
     * @return instance of a motion event
     */
    /* private */ MotionEvent getEvent() {
        int pointerCount = mPointers.size();
        int[] pointerIds = new int[pointerCount];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            pointerIds[i] = mPointers.keyAt(i);
            pointerCoords[i] = mPointers.valueAt(i);
        }

        MotionEvent ev = MotionEvent.obtain(mDownTime, mEventTime < 0 ? SystemClock.uptimeMillis() : mEventTime,
                mAction, pointerCount, pointerIds, pointerCoords, mMetaState, mXPrecision, mYPrecision, mDeviceId,
                mEdgeFlags, mSource, mFlags);
        return ev;
    }

    @Override
    public boolean isThrottlable() {
        return (getAction() == MotionEvent.ACTION_UP);
    }

    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        MotionEvent me = getEvent();
        if ((verbose > 0 && !mIntermediateNote) || verbose > 1) {
            StringBuilder msg = new StringBuilder(":Sending ");
            msg.append(getTypeLabel()).append(" (");
            switch (me.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    msg.append("ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    msg.append("ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    msg.append("ACTION_UP");
                    break;
                case MotionEvent.ACTION_CANCEL:
                    msg.append("ACTION_CANCEL");
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    msg.append("ACTION_POINTER_DOWN ").append(me.getPointerId(me.getActionIndex()));
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    msg.append("ACTION_POINTER_UP ").append(me.getPointerId(me.getActionIndex()));
                    break;
                default:
                    msg.append(me.getAction());
                    break;
            }
            msg.append("):");

            int pointerCount = me.getPointerCount();
            for (int i = 0; i < pointerCount; i++) {
                msg.append(" ").append(me.getPointerId(i));
                msg.append(":(").append(me.getX(i)).append(",").append(me.getY(i)).append(")");
            }
            Logger.println(msg.toString());
        }
        try {
            if (!InputManager.getInstance().injectInputEvent(me,
                    InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT)) {
                return MonkeyEvent.INJECT_FAIL;
            }
        } catch (SecurityException e) {
            return MonkeyEvent.INJECT_FAIL;
        } finally {
            me.recycle();
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }

    protected abstract String getTypeLabel();
}
