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

package com.android.commands.monkey.fastbot.client;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jianqiang Guo
 */

/**
 * Communication interface
 */
public class Operate extends GsonIface {
    public ActionType act;
    public List<Short> pos;
    // Text information to be inputed
    public String text;
    // Do you need to clear the original text before input text?
    public boolean clear;
    // Whether to use the original adb shell to perform input,
    // and raw input(adbkeyborad) choose one, raw input speed is faster,
    // adb compatibility is better, in some scenarios such as security keyboard may only use adb
    public boolean adbinput;
    public boolean rawinput;
    public boolean allowFuzzing;
    public boolean editable;
    public String sid;
    public String aid;
    // Event duration, such as long press 5 seconds, wait time is 5000
    public long waitTime;
    // Event interval time
    public int throttle;
    public String target;
    public String jAction;

    public static Operate fromJson(String jsonStr) {
        return gson.fromJson(jsonStr, Operate.class);
    }

    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();
        if (pos == null)
            return points;
        for (int i = 0; i + 1 < pos.size(); i += 2) {
            Point p = new Point((Short) pos.get(i), (Short) pos.get(i + 1));
            points.add(p);
        }
        return points;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
