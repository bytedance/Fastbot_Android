/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
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
