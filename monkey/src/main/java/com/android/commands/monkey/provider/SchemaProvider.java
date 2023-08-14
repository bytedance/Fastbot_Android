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
 * Read predefined schema from /sdcard/max.schema
 */
public class SchemaProvider {
    static final ArrayList<String> strings;

    static {
        File stringFiles = new File("/sdcard/max.schema");
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

    public static ArrayList<String> getStrings() {
        return strings;
    }
}
