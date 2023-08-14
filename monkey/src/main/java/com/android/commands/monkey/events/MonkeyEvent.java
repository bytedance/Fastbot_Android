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

package com.android.commands.monkey.events;

import android.app.IActivityManager;
import android.view.IWindowManager;

/**
 * abstract class for monkey event
 */
public abstract class MonkeyEvent {
    public static final int EVENT_TYPE_KEY = 0;
    public static final int EVENT_TYPE_TOUCH = 1;
    public static final int EVENT_TYPE_TRACKBALL = 2;
    public static final int EVENT_TYPE_ROTATION = 3; // Screen rotation
    public static final int EVENT_TYPE_ACTIVITY = 4;
    public static final int EVENT_TYPE_FLIP = 5; // Keyboard flip
    public static final int EVENT_TYPE_THROTTLE = 6;
    public static final int EVENT_TYPE_PERMISSION = 7;
    public static final int EVENT_TYPE_NOOP = 8;
    public static final int EVENT_TYPE_IME = 9;
    public static final int EVENT_TYPE_PRO_ACTIVITY = 10;
    public static final int EVENT_TYPE_SCHEMA = 11;
    public static final int EVENT_TYPE_DATA_ACTIVITY = 12;
    public static final int EVENT_TYPE_COMMON = 13;
    public static final int INJECT_SUCCESS = 1;
    public static final int INJECT_FAIL = 0;
    // error code for remote exception during injection
    public static final int INJECT_ERROR_REMOTE_EXCEPTION = -1;
    // error code for security exception during injection
    public static final int INJECT_ERROR_SECURITY_EXCEPTION = -2;
    protected int eventType;
    int eventId = -1;

    public MonkeyEvent(int type) {
        eventType = type;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int id) {
        if (this.eventId != -1) {
            throw new IllegalStateException();
        }
        this.eventId = id;
    }

    /**
     * @return event type
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * @return true if it is safe to throttle after this event, and false
     * otherwise.
     */
    public boolean isThrottlable() {
        return true;
    }

    /**
     * a method for injecting event, and this event will be executed right now.
     *
     * @param iwm     wires to current window manager
     * @param iam     wires to current activity manager
     * @param verbose a log switch
     * @return INJECT_SUCCESS if it goes through, and INJECT_FAIL if it fails in
     * the case of exceptions, return its corresponding error code
     */
    public abstract int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose);
}
