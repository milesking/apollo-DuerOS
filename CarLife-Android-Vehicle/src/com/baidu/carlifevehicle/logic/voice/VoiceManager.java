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
package com.baidu.carlifevehicle.logic.voice;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.model.ModuleStatusModel;
import com.baidu.carlifevehicle.service.RecordService;
import com.baidu.carlifevehicle.service.RecordService.RecordServiceBinder;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

public class VoiceManager {

    private static final String TAG = "CarLifeVoice";
    private static final Object LOCK = new Object();
    private static VoiceManager mInstance;
    private Context mContext;

    private RecordServiceBinder mRecordServiceBinder;

    private static final int VR_STATUS_IDLE = 0;
    private static final int VR_STATUS_WAKEUP = 1;
    private static final int VR_STATUS_RECOG = 2;
    private int mVoiceStatus = VR_STATUS_IDLE;
    private boolean isPaused;

    private VoiceManager() {
    }

    public static VoiceManager getInstance() {
        if (null == mInstance) {
            synchronized (LOCK) {
                if (null == mInstance) {
                    mInstance = new VoiceManager();
                }
            }
        }

        return mInstance;
    }

    private ServiceConnection mRecordServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "--recordservice-onServiceConnected----");
            mRecordServiceBinder = (RecordServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "--recordservice-onServiceDisconnected----");
        }

    };

    private MsgBaseHandler mHandler = new MsgBaseHandler() {

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_MIC_RECORD_END);
            addMsg(CommonParams.MSG_CMD_MIC_RECORD_WAKEUP_START);
            addMsg(CommonParams.MSG_CMD_MIC_RECORD_RECOG_START);
            addMsg(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
        }

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CommonParams.MSG_CMD_MIC_RECORD_END:
                    if (0 != CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_VOICE_MIC)) {
                        break;
                    }

                    mRecordServiceBinder.onRecordEnd();
                    break;
                case CommonParams.MSG_CMD_MIC_RECORD_WAKEUP_START:
                    if (0 != CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_VOICE_MIC)) {
                        break;
                    }

                    mVoiceStatus = VR_STATUS_WAKEUP;
                    if (!isPaused) {
                        mRecordServiceBinder.onWakeUpStart();
                    }
                    break;
                case CommonParams.MSG_CMD_MIC_RECORD_RECOG_START:
                    // Only use the hu mic to continue
                    if (0 != CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_VOICE_MIC)) {
                        break;
                    }

                    if (!isPaused) {
                        mRecordServiceBinder.onVRStart();
                    }
                    break;
                case CommonParams.MSG_CONNECT_STATUS_DISCONNECTED:
                    mVoiceStatus = VR_STATUS_IDLE;
                    mRecordServiceBinder.onUsbDisconnected();
                    break;
                default:
                    break;
            }
        }
    };

    public void init(Context context) {
        mContext = context;

        // start record service
        Intent intent = new Intent(mContext, RecordService.class);
        mContext.bindService(intent, mRecordServiceConnection, Context.BIND_AUTO_CREATE);

        MsgHandlerCenter.registerMessageHandler(mHandler);
    }

    public void uninit() {
        MsgHandlerCenter.unRegisterMessageHandler(mHandler);
        if (mContext != null) {
            mContext.unbindService(mRecordServiceConnection);
        }
    }

    public void onVoiceRecogRunning() {
        mVoiceStatus = VR_STATUS_RECOG;
        if (mRecordServiceBinder != null) {
            mRecordServiceBinder.requestAudioFocus();
        }
    }

    public void onVoiceRecogIDLE() {
        if (mVoiceStatus == VR_STATUS_RECOG) {
            mVoiceStatus = VR_STATUS_IDLE;
        }
        if (mRecordServiceBinder != null) {
            mRecordServiceBinder.abandonAudioFocus();
        }
    }

    public void onActivityPause() {
        isPaused = true;
        switch (mVoiceStatus) {
            case VR_STATUS_RECOG:
                CarlifeUtil.sendModuleControlToMd(ModuleStatusModel.CARLIFE_VR_MODULE_ID,
                        ModuleStatusModel.VR_STATUS_IDLE);
                break;
            case VR_STATUS_WAKEUP:
                mRecordServiceBinder.onRecordEnd();
                break;
            case VR_STATUS_IDLE:
            default:
                break;
        }
    }

    public void onActivityResume() {
        isPaused = false;
        switch (mVoiceStatus) {
            case VR_STATUS_RECOG:
                mRecordServiceBinder.onVRStart();
                break;
            case VR_STATUS_WAKEUP:
                mRecordServiceBinder.onWakeUpStart();
                break;
            case VR_STATUS_IDLE:
            default:
                break;
        }

    }

}
