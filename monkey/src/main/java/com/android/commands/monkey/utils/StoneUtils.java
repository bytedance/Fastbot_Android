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

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Zhang Zhao
 */

public class StoneUtils {
    private static final String TAG = StoneUtils.class.getSimpleName();

    /**
     * Execute shell command
     * @param command shell command to execute
     * @return output of the execution result.
     */
    public static String executeShellCommand(String command) {
        String line = "";
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(command);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            Logger.errorPrintln("executeShellCommand() error! command: "+command);
            Logger.errorPrintln(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (proc != null) {
                proc.destroy();
            }
        }
        return sb.toString();
    }

    /**
     * Write string to file
     *
     * @param path path of the file to write to
     * @param data data to write
     * @return return true if succeed to write
     */
    public static boolean writeStringToFile(String path, String data, boolean isAppend) throws IOException {
        boolean result = false;
        FileWriter fw = null;
        try {
            File file = new File(path);
            fw = new FileWriter(file, isAppend);
            fw.write(data);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw!=null)
                fw.close();
        }
        return result;
    }

}
