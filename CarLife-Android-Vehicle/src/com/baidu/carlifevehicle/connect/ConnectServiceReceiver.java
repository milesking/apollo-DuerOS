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
import android.os.Handler;
import android.os.Message;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.util.LogUtil;

public class ConnectServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectServiceReceiver";

    private static final String CARLIFE_CONNECT_SERVICE_START = "com.baidu.carlife.connect.ConnectServiceStart";
    private static final String CARLIFE_CONNECT_SERVICE_STOP = "com.baidu.carlife.connect.ConnectServiceStop";

    private Context mContext = null;
    private Handler mHandler = null;

    public ConnectServiceReceiver(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CARLIFE_CONNECT_SERVICE_START);
        filter.addAction(CARLIFE_CONNECT_SERVICE_STOP);
        mContext.registerReceiver(this, filter);
    }

    public void unregisterReceiver() {
        // LogUtils.d(TAG, "unregister UsbConnectServiceReceiver");
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == mHandler) {
            LogUtil.e(TAG, "mHandler is null");
            return;
        }

        String action = intent.getAction();
        Message msg = new Message();
        msg.what = CommonParams.MSG_CONNECT_SERVICE_MSG;

        if (action.equals(CARLIFE_CONNECT_SERVICE_START)) {
            LogUtil.d(TAG, "start connect service");
            msg.arg1 = CommonParams.MSG_CONNECT_SERVICE_MSG_START;
        } else if (action.equals(CARLIFE_CONNECT_SERVICE_STOP)) {
            LogUtil.d(TAG, "stop connect service");
            msg.arg1 = CommonParams.MSG_CONNECT_SERVICE_MSG_STOP;
        }

        mHandler.sendMessage(msg);
    }
}
