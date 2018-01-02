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
package com.baidu.carlifevehicle.service;

import com.baidu.carlifevehicle.audioplayer.VehiclePCMPlayer;
import com.baidu.carlifevehicle.broadcast.BroadcastActionConstant;
import com.baidu.carlifevehicle.logic.voice.PcmRecorder;
import com.baidu.carlifevehicle.util.LogUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * record and send pcm data to mobile device
 *
 * @author ouyangnengjun
 */
public class RecordService extends Service {

    private static final String TAG = "CarLifeVoice";
    public static final int VR_STATUS_RECOGNITION = 1;
    public static final int VR_STATUS_WAKEUP = 2;
    private PcmRecorder mPcmRecorder = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mPcmRecorder = new PcmRecorder();
        mPcmRecorder.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordServiceBinder();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "--RecordService--onDestroy----");
        mPcmRecorder = null;
        super.onDestroy();
    }

    public class RecordServiceBinder extends Binder {

        public void requestAudioFocus() {
            LogUtil.e(TAG, "-----requestAudioFocus-----");
            VehiclePCMPlayer.getInstance().requestVRAudioFocus();
            sendBroadcast(new Intent(BroadcastActionConstant.CARLIFE_RECORD_SERVICE_START));
        }

        public void abandonAudioFocus() {
            LogUtil.e(TAG, "-----abandonAudioFocus-----");
            VehiclePCMPlayer.getInstance().abandonVRAudioFocus();
            sendBroadcast(new Intent(BroadcastActionConstant.CARLIFE_RECORD_SERVICE_STOP));
        }

        public void onRecordEnd() {
            LogUtil.e(TAG, "-----MSG_CMD_MIC_RECORD_END-----");
            if (mPcmRecorder != null) {
                mPcmRecorder.setRecording(false);
            }
        }

        public void onWakeUpStart() {
            LogUtil.e(TAG, "-----MSG_CMD_MIC_RECORD_WAKEUP_START-----");
            if (mPcmRecorder == null || !mPcmRecorder.isAlive()) {
                mPcmRecorder = new PcmRecorder();
                mPcmRecorder.start();
            }
            mPcmRecorder.setRecording(true);

            // close down sample
            mPcmRecorder.setDownSampleStatus(false);
        }

        public void onVRStart() {
            LogUtil.e(TAG, "-----MSG_CMD_MIC_RECORD_RECOG_START-----");
            if (mPcmRecorder == null || !mPcmRecorder.isAlive()) {
                mPcmRecorder = new PcmRecorder();
                mPcmRecorder.start();
            }
            mPcmRecorder.setRecording(true);

            // close down sample
            mPcmRecorder.setDownSampleStatus(false);
        }

        public void onUsbDisconnected() {
            VehiclePCMPlayer.getInstance().abandonVRAudioFocus();
            if (mPcmRecorder != null) {
                mPcmRecorder.setRecording(false);
            }
        }
    }

}
