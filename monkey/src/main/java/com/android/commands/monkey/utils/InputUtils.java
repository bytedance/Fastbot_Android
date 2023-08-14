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

package com.android.commands.monkey.utils;

/**
 * @author Dingchun Wang
 */

/**
 * Input method tool class, call adb to switch the specified input method
 */
public class InputUtils {
    public static String getDefaultIme() {
        return StoneUtils.executeShellCommand("settings get secure default_input_method");
    }

    public static void switchToIme(String ime) {
        StoneUtils.executeShellCommand("ime enable " + ime);
        StoneUtils.executeShellCommand("ime set " + ime);
    }

}
