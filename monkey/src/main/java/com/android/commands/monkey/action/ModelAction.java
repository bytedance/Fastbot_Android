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

package com.android.commands.monkey.action;

import android.content.ComponentName;
import android.graphics.PointF;
import android.graphics.Rect;

import com.android.commands.monkey.fastbot.client.ActionType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhao Zhang
 */

/**
 * generation model action
 */
public class ModelAction extends Action {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static Pattern BOUNDS_RECT = Pattern.compile("\\[([0-9]+),([0-9]+)]\\[([0-9]+),([0-9]+)]");
    public final String packageName;
    public final String className;
    public final Rect boundingBox;
    private String inputText = null;
    private boolean useAdbInput = false;
    private boolean clearText = false;
    private boolean rawInput = false;
    private boolean isEditText = false;
    private long waitTime = 0;
    private int throttle = 0;
    private List<PointF> points = null;
    private String uri = "";
    private int keycode = 0;
    private String shellCommand = "";


    public ModelAction(ActionType type, ComponentName activity, Rect rect) {
        super(type);
        this.packageName = activity.getPackageName();
        this.className = activity.getClassName();
        this.boundingBox = rect;
    }

    public ModelAction(ActionType type, ComponentName activity, List<PointF> points, Rect rect) {
        super(type);
        this.packageName = activity.getPackageName();
        this.className = activity.getClassName();
        this.boundingBox = rect;
        this.points = points;
    }

    public ModelAction(ActionType type, String packageName, String className, Rect rect) {
        super(type);
        this.packageName = packageName;
        this.className = className;
        this.boundingBox = rect;
    }

    public static Action fromJSON(JSONObject jAction) throws JSONException {
        ActionType actionType = ActionType.valueOf(jAction.getString("actionType"));
        String packageName = jAction.getString("packageName");
        String className = jAction.getString("className");
        Rect bounds = parseRect(jAction.getString("bounds"));
        Action action = new ModelAction(actionType, packageName, className, bounds);
        int throttle = jAction.getInt("throttle");
        action.setThrottle(throttle);
        return action;
    }

    static Rect parseRect(String bounds) {
        Matcher m = BOUNDS_RECT.matcher(bounds);
        if (m.matches()) {
            return new Rect(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4))
            );
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + ((boundingBox == null) ? 0 : boundingBox.hashCode());
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
        ModelAction other = (ModelAction) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (packageName == null) {
            if (other.packageName != null)
                return false;
        } else if (!packageName.equals(other.packageName))
            return false;

        if (boundingBox == null) {
            return other.boundingBox == null;
        } else
            return boundingBox.equals(other.boundingBox);

    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jAction = super.toJSONObject();
        jAction.put("packageName", packageName);
        jAction.put("className", className);
        jAction.put("bounds", "[" + boundingBox.left + "," + boundingBox.top + "][" + boundingBox.right + "," + boundingBox.right + "]");
        return jAction;
    }
    public String toString() {
        return super.toString() + "@" + this.className;
    }

    public ComponentName getActivity() {
        return new ComponentName(packageName, className);
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String str) {
        this.inputText = str;
    }

    public boolean isUseAdbInput() {
        return useAdbInput;
    }

    public void setUseAdbInput(boolean useAdbInput) {
        this.useAdbInput = useAdbInput;
    }

    public boolean isClearText() {
        return clearText;
    }

    public void setClearText(boolean clearText) {
        this.clearText = clearText;
    }

    public boolean isRawInput() {
        return rawInput;
    }

    public void setRawInput(boolean rawInput) {
        this.rawInput = rawInput;
    }

    public boolean isEditText() {
        return isEditText;
    }

    public void setEditText(boolean editText) {
        isEditText = editText;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(long t) {
        this.waitTime = t;
    }

    public List<PointF> getPoints() {
        return points;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getKeycode() {
        return keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public String getShellCommand() {
        return shellCommand;
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand = shellCommand;
    }
}
