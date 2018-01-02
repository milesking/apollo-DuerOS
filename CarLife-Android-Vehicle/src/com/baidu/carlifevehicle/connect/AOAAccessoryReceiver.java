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
package com.baidu.carlifevehicle.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import com.baidu.carlifevehicle.util.LogUtil;

public class AOAAccessoryReceiver extends BroadcastReceiver {

    private static final String TAG = "AOAAccessoryReceiver";

    public static final String ACTION_USB_PERMISSION = "com.baidu.carlifevehicle.connect.USB_PERMISSION";

    private Context mContext = null;
    private Handler mHandler = null;

    public AOAAccessoryReceiver() {

    }

    public AOAAccessoryReceiver(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        mContext.registerReceiver(this, filter);
    }

    public void unregisterReceiver() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        LogUtil.e(TAG, action);
        try {
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                LogUtil.e(TAG, "USB Device Attached");
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                LogUtil.e(TAG, "USB Device Detached");
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device == null) {
                        return;
                    }
                    LogUtil.d(TAG, device.toString());
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        LogUtil.d(TAG, "permission success");
                    } else {
                        LogUtil.d(TAG, "permission denied");
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "get unknow exception");
            e.printStackTrace();
        }
    }
}