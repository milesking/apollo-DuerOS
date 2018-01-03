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
package com.baidu.carlifevehicle.broadcast;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.connect.UsbMuxdConnect;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastActionReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(BroadcastActionConstant.CARLIFE_EXIT)) {
                LogUtil.e(TAG, "CarLife will exit ");
                if (CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC_DUAL_AUDIO)
                        || CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC)
                        || CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_YUANFENG)
                        || CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MSTAR786)
                        || CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_QUANZHI)) {
                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_ACTION_EXIT_APP);
                }
            } else if (intent.getAction().equals(BroadcastActionConstant.USB_DEVICE_ATTACHED)) {
                LogUtil.e(TAG, "Usb device attached ");
                if (!CommonParams.PULL_UP_VEHICLE) {
                    LogUtil.e(TAG, "do not pull up carlife vehicle ");
                    return;
                }
                if (CarlifeUtil.getInstance().isUsbStorageDevice(context)) {
                    return;
                }
                if (UsbMuxdConnect.getInstance().scanIphoneDevices()) {
                    LogUtil.e(TAG, "iPhone attached, connect vehicle ");
                    UsbMuxdConnect.getInstance().setUsbAttch(true);
                    UsbMuxdConnect.getInstance().startUsbMuxdConnectThread();
                }
                Intent mainActivityIntent = new Intent(context, com.baidu.carlifevehicle.CarlifeActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainActivityIntent);
            } else if (intent.getAction().equals(BroadcastActionConstant.USB_DEVICE_DETACHED)) {
                LogUtil.e(TAG, "Usb device detached ");
                if (!ConnectClient.getInstance().isCarlifeConnected()
                        && ConnectClient.getInstance().isCarlifeConnecting()) {
                    ConnectClient.getInstance().setIsConnecting(false);
                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL);
                }

                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_FRAGMENT_REFRESH);
                LogUtil.d(TAG, "#######  Usb device detached  [Change default Usb Connected type]");
            }
        } catch (Exception ex) {
            LogUtil.d(TAG, "BroadcastActionReceiver get exception");
            ex.printStackTrace();
        }
    }

}
