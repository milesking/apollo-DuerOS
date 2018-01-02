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
package com.baidu.carlifevehicle.audioplayer;

import android.media.AudioManager;

import com.baidu.carlifevehicle.audioplayer.ampplayprocess.AMPPlayProcessDual;
import com.baidu.carlifevehicle.audioplayer.ampplayprocess.AMPPlayProcessInterface;
import com.baidu.carlifevehicle.audioplayer.ampplayprocess.AMPPlayProcessSingle;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleDual;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleInterface;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleSingel;
import com.baidu.carlifevehicle.audioplayer.audiotrackmanager.AudioTrackManagerDualInterface;
import com.baidu.carlifevehicle.audioplayer.audiotrackmanager.AudioTrackManagerDualNormal;
import com.baidu.carlifevehicle.audioplayer.audiotrackmanager.AudioTrackManagerDualStreamType;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * adapt vehicle:
 * <p>
 * 1. single audio track/normal dual audio track/hyundai;
 * <p>
 * 2. stream type(ChangAn/BYD/YuanFeng);
 * <p>
 * 3. media AudioTrack buffer size(reserved);
 * <p>
 * 4. tts AudioTrack buffer size(reserved);
 * <p>
 * 5. AudioTrack.getPlayStatus()(reserved);
 *
 * @author liucaiquan
 */
public class VehicleFactoryAdapter {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + VehicleFactoryAdapter.class.getSimpleName();
    private static VehicleFactoryAdapter mInstance;

    private static final int AUDIO_TRACK_STREAM_TYPE_BYD = 10;
    private static final int AUDIO_TRACK_STREAM_TYPE_CHANGAN = 11;
    private static final int AUDIO_TRACK_STREAM_TYPE_YUANFENG = 0x22;
    private static final int AUDIO_TRACK_STREAM_TYPE_CADILLAC = 0x22;
    private static final int AUDIO_TRACK_STREAM_TYPE_BAIDU = -10000;

    private VehicleFactoryAdapter() {

    }

    public static VehicleFactoryAdapter getInstance() {
        if (mInstance == null) {
            mInstance = new VehicleFactoryAdapter();
        }

        return mInstance;
    }

    public ArbitrationModuleInterface getArbitrationModule(boolean isDualAudioTrack, DataQueue mQ, DataQueue tQ) {
        if (isDualAudioTrack) {
            return new ArbitrationModuleDual(mQ, tQ);
        } else {
            return new ArbitrationModuleSingel(mQ, tQ);
        }
    }

    public AMPPlayProcessInterface getAMPPlayProcess(boolean isDulAudioTrack,
                                                     ArbitrationModuleInterface arbitrationModule) {
        if (isDulAudioTrack) {
            return new AMPPlayProcessDual(arbitrationModule);
        } else {
            return new AMPPlayProcessSingle(arbitrationModule);
        }

    }

    public AudioTrackManagerDualInterface getAudioTrackManager() {
        if (getTTSAudioTrackStreamType() == AudioManager.STREAM_MUSIC) {
            return AudioTrackManagerDualNormal.getInstance();
        } else {
            return AudioTrackManagerDualStreamType.getInstance();
        }
    }

    public int getTTSAudioTrackStreamType() {
        int streamType = CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_AUDIO_TRACK_STREAM_TYPE);
        LogUtil.d(TAG, "Get audio stream type = " + streamType);
        return streamType;
    }

    public boolean isTTSRequestAudioFocus() {
        boolean ret =
                CarlifeConfUtil.getInstance().getBooleanProperty(CarlifeConfUtil.KEY_BOOL_TTS_REQUEST_AUDIO_FOCUS);
        LogUtil.d(TAG, "TTS require audio focus = " + ret);
        return ret;
    }

    public boolean isDualAudioTrack() {
        int audioTrackNum = CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_AUDIO_TRACK_NUM);
        LogUtil.d(TAG, "Get audio track number = " + audioTrackNum);
        return (audioTrackNum == 2);
    }

    public boolean isContentEncrypt() {
        boolean rst = CarlifeConfUtil.getInstance().getBooleanProperty(CarlifeConfUtil.KEY_CONTENT_ENCRYPTION);
        LogUtil.d(TAG, "content encrypt = " + rst);
        return rst;
    }

    public int getEngineType() {
        int rst = CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_ENGINE_TYPE);
        LogUtil.d(TAG, "engine type= " + rst);
        return rst;
    }

}
