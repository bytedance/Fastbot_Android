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

import com.android.commands.monkey.framework.APIAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * @author Zhao Zhang, Tianxiao Gu
 */

/**
 * uuid utils
 */
public class UUIDHelper {
    static String uuid = null;
    static String serial = APIAdapter.getSerial();

    static {
        File stringFiles = new File("/sdcard/max.uuid");
        if (stringFiles.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(stringFiles))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    uuid = line;
                }
            } catch (Exception e) {
            }
        }
    }

    public static String read() {
        if (uuid == null || "".equals(uuid) || "0".equals(uuid) || "null".equals(uuid)) {
            uuid = UUID.randomUUID().toString();
            write();
            return uuid;
        }
        return uuid;
    }

    public static void write() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File("/sdcard/max.uuid")))) {
            pw.print(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.warningFormat("Fail to save uuid");
        }
    }

    public static String getSerial() {
        return serial;
    }
}
