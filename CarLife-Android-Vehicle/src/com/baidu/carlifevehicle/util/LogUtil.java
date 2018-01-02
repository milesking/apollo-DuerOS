/******************************************************************************
 * Copyright 2017 The Baidu Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/
package com.baidu.carlifevehicle.util;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.baidu.carlifevehicle.CommonParams;

import android.util.Log;

public class LogUtil {
    public static final boolean IS_WRITE_TO_FILE = false;
    public static final String LOG_FILE = "_Carlife.log";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

    public static void dumpException(Throwable t) {
        if (CommonParams.LOG_LEVEL <= Log.WARN) {
            final int innerBufLen = 256;
            StringBuilder err = new StringBuilder(innerBufLen);

            err.append("Got exception: ");
            err.append(t.toString());
            err.append("\n");

            System.out.println(err.toString());
            t.printStackTrace(System.out);
        }
    }

    public static void v(String tag, String format, Object...args) {
        outMsg(Log.VERBOSE, tag, format, args);
    }

    public static void v(String tag, String format) {
        if (CommonParams.LOG_LEVEL <= Log.VERBOSE) {
            Log.v(tag, format);
        }
    }

    public static void d(String tag, String format, Object...args) {
        outMsg(Log.DEBUG, tag, format, args);
    }

    public static void d(String tag, String format) {
        if (CommonParams.LOG_LEVEL <= Log.DEBUG) {
            Log.d(tag, format);
        }
    }

    public static void i(String tag, String format, Object...args) {
        outMsg(Log.INFO, tag, format, args);
    }

    public static void i(String tag, String format) {
        if (CommonParams.LOG_LEVEL <= Log.INFO) {
            Log.i(tag, format);
        }
    }

    public static void w(String tag, String format, Object...args) {
        outMsg(Log.WARN, tag, format, args);
    }

    public static void w(String tag, String format) {
        if (CommonParams.LOG_LEVEL <= Log.WARN) {
            Log.w(tag, format);
        }
    }

    public static void e(String tag, String format, Object...args) {
        outMsg(Log.ERROR, tag, format, args);
    }

    public static void e(String tag, String format) {
        if (CommonParams.LOG_LEVEL <= Log.ERROR) {
            Log.e(tag, format);
        }
    }

    private static void outMsg(int level, String tag, String format, Object...args) {
        if (CommonParams.LOG_LEVEL <= level && tag != null && format != null) {
            Log.println(level, tag, String.format(format, args));
        }
    }

    public static void f(String tag, String format) {
        FileWriter fw = null;
        try {
            if (tag == null || format == null) {
                return;
            }
            int len = format.length();
            int max = CommonParams.MAX_DATA_DISPLAY_LENGTH;
            String tformat = format.substring(0, len > max ? max : len);

            String date = DATE_FORMAT.format(new Date());
            String log = "[" + date + "]" + tformat + "\r\n";
            String logFile;
            if (date != null && date.length() >= 10) {
                logFile = CommonParams.SD_DIR + "/" + date.substring(0, 10) + LOG_FILE;
            } else {
                return;
            }
            fw = new FileWriter(logFile, true);
            fw.write(log);
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CarlifeUtil.closeCloseable(fw);
        }
    }
}
