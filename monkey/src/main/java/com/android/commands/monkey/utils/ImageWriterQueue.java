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

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Zhao Zhang
 */

public class ImageWriterQueue implements Runnable {


    private LinkedList<Req> requestQueue = new LinkedList<Req>();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Req req = null;
            try {
                req = read();
            } catch (InterruptedException e) {
                continue;
            }
            if (req != null) {
                writePNG(req);
            }
        }
    }

    private void writePNG(Req req) {
        Bitmap map = req.map;
        File dst = req.dst;
        if (map == null) {
            Logger.format("No screen shot for %s", dst.getAbsolutePath());
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(dst)) {
            map.compress(Bitmap.CompressFormat.PNG, 85, fos);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.format("Fail to save screen shot to %s", dst.getAbsolutePath());
        } finally {
            map.recycle();
        }
    }

    public synchronized void add(Bitmap map, File dst) {
        requestQueue.add(new Req(map, dst));
        if (requestQueue.size() > Config.flushImagesThreshold) {
            Logger.format("ImageQueue is too full (%d)! Try to flush it.", requestQueue.size());
            flush();
        }
        notifyAll();
    }

    public synchronized void flush() {
        while (!requestQueue.isEmpty()) {
            Req req = requestQueue.removeFirst();
            writePNG(req);
        }
    }

    private synchronized Req read() throws InterruptedException {
        while (requestQueue.isEmpty()) {
            wait();
        }
        Req req = requestQueue.removeFirst();
        return req;
    }

    public synchronized void tearDown() {
        flush();
    }

    static class Req {
        final Bitmap map;
        final File dst;

        public Req(Bitmap map, File dst) {
            this.map = map;
            this.dst = dst;
        }
    }
}
