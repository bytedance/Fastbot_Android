/*
 * Copyright 2020 Advanced Software Technologies Lab at ETH Zurich, Switzerland
 *
 * Modified - Copyright (c) 2020 Bytedance Inc.
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

import android.annotation.ColorInt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zhao Zhang
 */

/**
 * general utils
 */
public class Utils {
    public static String getProcessOutput(String[] cmd) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder processOutput = new StringBuilder();
        try (BufferedReader processOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));) {
            String readLine;
            while ((readLine = processOutputReader.readLine()) != null) {
                processOutput.append(readLine + System.lineSeparator());
            }
            process.waitFor();
        }
        return processOutput.toString().trim();
    }

    public static void samplingActivityCoverage(Set<String> tested, HashSet<String> total, List<Map<String, String>> list) {
        long time = System.currentTimeMillis();
        int i = 0;
        for (String activity : tested) {
            if (!total.contains(activity)) continue;
            i++;
        }

        float c = 0.0f;
        if (total.size() > 0) {
            c = 1.0f * i / total.size() * 100;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("" + time, "" + c);
        list.add(map);
    }

    public static void activityStatistics(File outputdir, String[] tested, String[] total, List<Map<String, String>> set, float coverage, Map<String, Integer> activityTimes) {
        BufferedWriter bufferedWriter = null;
        JSONObject jsondata = new JSONObject();
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(new File(outputdir, "/max.activity.statistics.log").getAbsolutePath(), false));
            JSONArray array = new JSONArray();

            for (Map<String, String> map : set) {
                JSONObject jsonmap = new JSONObject();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    jsonmap.put(entry.getKey(), entry.getValue());
                }
                array.put(jsonmap);
            }
            jsondata.put("Sampling", array);
            array = new JSONArray();
            for (String it : tested) {
                array.put(it);
            }
            jsondata.put("TestedActivity", array);
            array = new JSONArray();
            for (String it : total) {
                array.put(it);
            }
            jsondata.put("TotalActivity", array);

            array = new JSONArray();
            JSONObject jsonmap = new JSONObject();
            for (Map.Entry<String, Integer> entry : activityTimes.entrySet()) {
                jsonmap.put(entry.getKey(), entry.getValue());
            }
            array.put(jsonmap);
            jsondata.put("ActivityTimes", array);
            jsondata.put("Coverage", coverage);

            bufferedWriter.write(String.format("%s\n", jsondata));
        } catch (IOException e) {
            Logger.println(" cannot write acitivity-statistis msg to " + outputdir + "/max.activity.statistics.log");
        } catch (JSONException e) {
            Logger.println(" cannot write json");
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
