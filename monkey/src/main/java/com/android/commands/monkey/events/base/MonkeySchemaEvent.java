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

package com.android.commands.monkey.events.base;

import android.app.IActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.view.IWindowManager;

import com.android.commands.monkey.events.MonkeyEvent;
import com.android.commands.monkey.framework.AndroidDevice;
import com.android.commands.monkey.utils.Logger;

/**
 * @author Zhao Zhang
 */


public class MonkeySchemaEvent extends MonkeyEvent {
    private final String schema;

    public MonkeySchemaEvent(String schema) {
        super(EVENT_TYPE_SCHEMA);
        this.schema = schema;
    }

    private Intent getIntent() {
        Uri uri = Uri.parse(schema);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 0)
            Logger.println(":schema =" + schema);

        if ("".equals(schema))
            return MonkeyEvent.INJECT_FAIL;

        return AndroidDevice.startUri(getIntent());
    }
}
