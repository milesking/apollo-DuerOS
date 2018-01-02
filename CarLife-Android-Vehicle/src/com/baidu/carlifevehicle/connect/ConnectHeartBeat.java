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

import java.util.Timer;
import java.util.TimerTask;

import com.baidu.carlifevehicle.util.LogUtil;

public class ConnectHeartBeat {
    private static final String TAG = "ConnectHeartBeat";
    public static final int HEART_BEAT_CHECK_MS = 1000;

    private static ConnectHeartBeat mInstance = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private byte[] packageHead = new byte[12];

    public static ConnectHeartBeat getInstance() {
        if (null == mInstance) {
            synchronized (ConnectHeartBeat.class) {
                if (null == mInstance) {
                    mInstance = new ConnectHeartBeat();
                }
            }
        }
        return mInstance;
    }

    /**
     * An empty packet is sent from the video channel as a heartbeat,
     * containing a timestamp, while return -1 indicates that the channel has been disconnected
     */
    public int sendEmptyPacket() {
        long timeStamp = System.currentTimeMillis();
        packageHead[0] = 0;
        packageHead[1] = 0;
        packageHead[2] = 0;
        packageHead[3] = 0;
        packageHead[4] = (byte) ((timeStamp & 0x7f000000) >> 24);
        packageHead[5] = (byte) ((timeStamp & 0xff0000) >> 16);
        packageHead[6] = (byte) ((timeStamp & 0xff00) >> 8);
        packageHead[7] = (byte) ((timeStamp & 0xff) >> 0);
        packageHead[8] = 0;
        packageHead[9] = 2;
        packageHead[10] = 0;
        packageHead[11] = 2;
        return ConnectManager.getInstance().writeVideoData(packageHead, 12);
    }

    public void startConnectHeartBeatTimer() {
        try {
            LogUtil.d(TAG, "start Connect heart beat timer");
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (sendEmptyPacket() < 0) {
                        LogUtil.d(TAG, "ean connect fail,send hear beat packet fail");
                    }
                }
            };
            mTimer.schedule(mTimerTask, HEART_BEAT_CHECK_MS, HEART_BEAT_CHECK_MS);
        } catch (Exception ex) {
            LogUtil.d(TAG, "startTimer get exception");
            ex.printStackTrace();
        }
    }

    public void stopConnectHeartBeatTimer() {
        LogUtil.d(TAG, "stop Connect heart beat timer");

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

}
