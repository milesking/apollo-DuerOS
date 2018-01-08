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
package com.baidu.che.codriverlauncher.receiver;

import com.baidu.che.codriverlauncher.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * receive date changed broadcast
 */
public class DateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "DateChangeReceiver";
    private static OnDateChangeListener sOnDateChangeListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
            LogUtil.d(TAG, Intent.ACTION_TIME_CHANGED);
        } else if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
            LogUtil.d(TAG, Intent.ACTION_DATE_CHANGED);
            if (sOnDateChangeListener != null) {
                sOnDateChangeListener.onDateChange();
            }
        }
    }

    public static void setDateChangeListener(OnDateChangeListener onDateChangeListener) {
        sOnDateChangeListener = onDateChangeListener;
    }
}
