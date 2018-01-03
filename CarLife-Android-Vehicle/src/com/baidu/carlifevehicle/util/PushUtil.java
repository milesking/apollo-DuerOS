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

import com.baidu.carlifevehicle.CarlifeActivity;
import com.baidu.carlifevehicle.CommonParams;

import android.app.Activity;
import android.content.SharedPreferences;

public class PushUtil {

    private static final String TAG = "PushUtil";
    private static PushUtil mInstance = null;
    private static String cerPath = null;

    private PushUtil() {

    }

    public static PushUtil getInstance() {
        if (mInstance == null) {
            mInstance = new PushUtil();
        }
        return mInstance;
    }

    public static void setIOSDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.length() == 0) {
            return;
        }
        SharedPreferences sh = CarlifeActivity.mContext.getSharedPreferences(
                CommonParams.USB_PREFRENECE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.putString(CommonParams.IOS_DEVICE_TOKEN, deviceToken);
        editor.commit();
    }

    public static String getIOSDeviceToken() {
        SharedPreferences sh = CarlifeActivity.mContext.getSharedPreferences(
                CommonParams.USB_PREFRENECE_NAME, Activity.MODE_PRIVATE);
        return sh.getString(CommonParams.IOS_DEVICE_TOKEN, "");
    }

}
