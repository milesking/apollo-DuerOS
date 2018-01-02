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

import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.content.Intent;

public class BroadcastManager {
    private static final String TAG = "BroadcastManager";
    private static BroadcastManager mInstance = null;
    private Context mContext = null;

    public static final int TYPE_CONNECTED = 0x01;
    public static final int TYPE_DISCONNECTED = 0x02;
    public static final int TYPE_NAVI_START = 0x03;
    public static final int TYPE_NAVI_STOP = 0x04;

    public static BroadcastManager getInstance() {
        if (null == mInstance) {
            synchronized (BroadcastManager.class) {
                if (null == mInstance) {
                    mInstance = new BroadcastManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mContext = context;
    }

    public void sendConnectedBoadcast() {
        LogUtil.d(TAG, "send connected broadcast");
        Intent intent = new Intent();
        intent.setAction(BroadcastActionConstant.CARLIFE_CONNECTED);
        intent.putExtra("connect", "connected");
        mContext.sendBroadcast(intent);
    }

    public void sendDisconnectedBoadcast() {
        LogUtil.d(TAG, "send disconnected broadcast");
        Intent intent = new Intent();
        intent.setAction(BroadcastActionConstant.CARLIFE_DISCONNECTED);
        intent.putExtra("connect", "disconnect");
        mContext.sendBroadcast(intent);
    }
    
    public void sendStartNaviBoadcast() {
        LogUtil.d(TAG, "send start navi broadcast");
        Intent intent = new Intent();
        intent.setAction(BroadcastActionConstant.CARLIFE_NAVI_START);
        intent.putExtra("navi", "start");
        mContext.sendBroadcast(intent);
    }
    
    public void sendStopNaviBoadcast() {
        LogUtil.d(TAG, "send stop navi broadcast");
        Intent intent = new Intent();
        intent.setAction(BroadcastActionConstant.CARLIFE_NAVI_STOP);
        intent.putExtra("navi", "stop");
        mContext.sendBroadcast(intent);
    }
    
    public void sendBoadcastToVehicle(int type) {
        LogUtil.d(TAG, "send broadcast" + type);
        switch (type) {
            case TYPE_CONNECTED:
                sendConnectedBoadcast();
                break;
            case TYPE_DISCONNECTED:
                sendDisconnectedBoadcast();
                break;
            case TYPE_NAVI_START:
                sendStartNaviBoadcast();
                break;
            case TYPE_NAVI_STOP:
                sendStopNaviBoadcast();
                break;
            default:
                LogUtil.d(TAG, "connect not support" + type);

        }
    }

}
