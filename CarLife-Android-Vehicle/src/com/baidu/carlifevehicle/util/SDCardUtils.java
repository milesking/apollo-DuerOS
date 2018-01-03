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

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class SDCardUtils {
    private static final double MIN_FREE_SPACE = 10;
    
    public static final String TAG = "StorageCheck";

    public static final int SDCARD_NORMAL = 0;
    public static final int SDCARD_FULL = 1;
    public static final int SDCARD_ERROR = 2;
    public static final int SDCARD_NOTFOUND = 3;

    public static final int MIN_FREE_SIZE = 1024 * 1024 * 15;

    public static final int MIN_CACHE_FREE_SIZE = 1024 * 1024 * 20;


    public static boolean writeTestFileToSdcard(String path) {
        boolean flag = false;
        try {
            File testFile = new File(path + "/test.0");
            if (testFile.exists()) {
                testFile.delete();
            }
            flag = testFile.createNewFile();
            if (testFile.exists()) {
                testFile.delete();
            }
        } catch (IOException e) {
            LogUtil.e("", e.toString());
        }
        return flag;
    }

    public static int getSdcardState() {
        String status = Environment.getExternalStorageState();

        if (status == null || Environment.MEDIA_BAD_REMOVAL.equals(status)) {
            return SDCARD_ERROR;
        } else if (Environment.MEDIA_CHECKING.equals(status)) {
            LogUtil.v(TAG, "MEDIA_CHECKING");
        } else if (Environment.MEDIA_MOUNTED.equals(status)) {
            return SDCARD_NORMAL;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
            return SDCARD_ERROR;
        } else if (Environment.MEDIA_NOFS.equals(status)) {
            return SDCARD_ERROR;
        } else if (Environment.MEDIA_REMOVED.equals(status)) {
            return SDCARD_NOTFOUND;
        } else if (Environment.MEDIA_SHARED.equals(status)) {
            // USB Mass Storage
            return SDCARD_NOTFOUND;
        } else if (Environment.MEDIA_UNMOUNTABLE.equals(status)) {
            return SDCARD_ERROR;
        } else if (Environment.MEDIA_UNMOUNTED.equals(status)) {
            return SDCARD_NOTFOUND;
        }
        return SDCARD_NORMAL;
    }
}
