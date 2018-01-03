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
package com.baidu.carlifevehicle.encryption;

import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by liucaiquan on 2017/2/23.
 */

public class DebugLogUtil {
    private static final boolean LOG_SWITCH = false;
    private static DebugLogUtil mInstance;
    private static final int MSG_ID_DEBUG = 2222222;
    private static final String MSG_BUNDLE_KEY = "key";
    private Context mContext;
    private DebugLogHandler mDebugLogHandler = new DebugLogHandler();

    private DebugLogUtil() {
        MsgHandlerCenter.registerMessageHandler(mDebugLogHandler);
    }

    public static DebugLogUtil getInstance() {
        if (mInstance == null) {
            mInstance = new DebugLogUtil();
        }

        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void println(String str) {
        if (!LOG_SWITCH) {
            return;
        }

        Message msg = new Message();
        msg.what = MSG_ID_DEBUG;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_BUNDLE_KEY, str);
        msg.setData(bundle);

        mDebugLogHandler.sendMessage(msg);
    }

    private class DebugLogHandler extends MsgBaseHandler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ID_DEBUG:
                    Toast.makeText(mContext, msg.getData().getString(MSG_BUNDLE_KEY), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void careAbout() {
            addMsg(MSG_ID_DEBUG);
        }
    }
}
