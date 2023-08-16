/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.fastbot.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jianqiang Guo
 */

public abstract class GsonIface {
    protected static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

}
