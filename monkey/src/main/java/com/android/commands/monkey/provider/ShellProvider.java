/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Zhao Zhang
 */

/**
 * Read predefined shell from/sdcard/max.shell
 */
public class ShellProvider {
    static final ArrayList<String> strings;

    static {
        File stringFiles = new File("/sdcard/max.shell");
        strings = new ArrayList<String>();
        if (stringFiles.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(stringFiles))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    strings.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Fail to load the strings file at " + stringFiles);
            }
        }
    }

    public static String randomNext() {
        if (strings.isEmpty()) {
            return "";
        }
        if (strings.size() == 1) {
            return strings.get(0);
        }
        int i = ThreadLocalRandom.current().nextInt(strings.size());
        return strings.get(i);
    }
}
