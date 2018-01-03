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
package com.baidu.carlifevehicle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baidu.carlifevehicle.logic.CarlifeDeviceInfoManager;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

/**
 * UncaughtException handler
 * 
 * @author zhaoke01
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    private Thread.UncaughtExceptionHandler mDefaultHandler = null;
    private static CrashHandler mInstance = null;
    private Context mContext;
    /**
     * store device info and exception messages
     */
    private Map<String, String> infos = new HashMap<String, String>();

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static final String CONTENT_SV = "sv";
    private static final String CONTENT_OS = "os";
    private static final String CONTENT_CUID = "cuid";
    private static final String CONTENT_PD = "pd";
    private static final String CONTENT_MB = "mb";
    private static final String CONTENT_OV = "ov";
    private static final String CONTENT_LT = "lt";
    private static final String CONTENT_CT = "ct";

    private static final String CRASH_LOG_DIR = "crash_log";
    private static final int CRASH_LOG_MAX_LEN = 16 * 10 * 1024;
    private static final int CRASH_LOG_MAX_LEN_FOR_EACH = 10 * 1024;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (null == mInstance) {
            synchronized (CrashHandler.class) {
                if (null == mInstance) {
                    mInstance = new CrashHandler();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        // default UncaughtException handler
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // set CrashHandler as the default UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(this);

        infos.put(CONTENT_SV, CarlifeUtil.getInstance().getVersionName());
        infos.put(CONTENT_OS, CommonParams.TYPE_OF_OS);
        infos.put(CONTENT_CUID, CarlifeUtil.getInstance().getCuid());
        infos.put(CONTENT_PD, "carlife");
        infos.put(CONTENT_MB, CarlifeUtil.getInstance().getChannel());
        infos.put(CONTENT_OV, CarlifeDeviceInfoManager.getInstance()
                .getDeviceInfo().getSdk());
        infos.put(CONTENT_LT, "1");
    }

    /**
     * called when UncaughtException occur
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            LogUtil.i(TAG, "system handles the Throwable");
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            LogUtil.i(TAG, "user handle the Throwable");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LogUtil.e(TAG, "error : ", e);
            }
            // exit
            System.exit(1);
        }
    }

    /**
     * error handle
     *
     * @param ex the Throwable caught
     * @return true for processed false for not
     */
    private boolean handleException(Throwable ex) {
        try {
            if (ex == null || mContext == null) {
                return false;
            }
            // toast the error
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(mContext,
                            mContext.getString(R.string.carlife_toast_crash),
                            Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }.start();
            saveCrashInfo2File(ex);
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * get device info
     *
     * @param ctx context
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            LogUtil.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                LogUtil.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                LogUtil.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * save error message to file
     *
     * @param ex the Throwable caught
     * @return the file name
     */
    private String saveCrashInfo2File(Throwable ex) {
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            String path = CommonParams.SD_DIR + "/" + CRASH_LOG_DIR + "/";

            infos.put(CONTENT_CT, time);

            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key + ":" + value + ";");
            }
            sb.append("\n");

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            FileOutputStream fos = null;
            try {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                return fileName;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, "an error occured while writing file...", e);
            } finally {
                CarlifeUtil.closeCloseable(fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readCrashInfo(String filename) {
        StringBuffer crashLog = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                crashLog.append(line + "\n");
                if (crashLog.length() > CRASH_LOG_MAX_LEN_FOR_EACH) {
                    return null;
                }
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "[ERROR]read crashLog " + filename);
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return crashLog.toString();
    }

    public String readCrashInfoFromDir() {
        StringBuffer crashLog = new StringBuffer();
        String line = null;
        String path = CommonParams.SD_DIR + "/" + CRASH_LOG_DIR + "/";

        try {
            File[] files = new File(path).listFiles();
            if (files == null || files.length < 1) {
                return null;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                String filename = file.getName();
                if (filename.contains("crash") && filename.contains("log")) {
                    line = readCrashInfo(path + filename);
                    if (line != null) {
                        crashLog.append(line);
                    }
                    if (crashLog.length() > CRASH_LOG_MAX_LEN) {
                        break;
                    }
                }
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return crashLog.toString();
    }
}
