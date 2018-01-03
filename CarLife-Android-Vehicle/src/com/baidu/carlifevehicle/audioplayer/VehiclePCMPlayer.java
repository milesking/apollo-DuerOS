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

import com.baidu.carlifevehicle.audioplayer.ampplayprocess.AMPPlayProcessInterface;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleInterface;

/**
 * module functions:
 * <p>
 * 1.initialize whole audio module;
 * <p>
 * 2.provide method interface for CarLife main activity;
 * <p>
 * <p>
 * provide method:
 * <p>
 * public static VehiclePCMPlayer getInstance();
 * <p>
 * public void initial();
 * <p>
 * public void threadStart();
 * 
 * @author Liu Caiquan
 * 
 * 
 */
public class VehiclePCMPlayer {
    private static VehiclePCMPlayer mInstance;

    private NaviReceiveProcessor mVehicleTTSProcessor;
    private VRReceiveProcessor mVehicleVRProcessor;
    private MediaReceiveProcessor mVehicleMusicProcessor;

    private AMPPlayProcessInterface mAMPPlayProcess;
    private ArbitrationModuleInterface mPriorityArbitrationModule;

    private DataQueue mMusicQueue;
    private DataQueue mTTSQueue;
    private DataQueue mVRQueue;

    private VehiclePCMPlayer() {

    }

    public static VehiclePCMPlayer getInstance() {
        if (mInstance == null) {

            mInstance = new VehiclePCMPlayer();
        }
        return mInstance;
    }

    public void initial() {
        mMusicQueue = new DataQueue(PCMPlayerUtils.MUSIC_QUEUE_SIZE);
        mTTSQueue = new DataQueue(PCMPlayerUtils.TTS_QUEUE_SIZE);
        mVRQueue = new DataQueue(PCMPlayerUtils.TTS_QUEUE_SIZE);

        boolean isDulAudioTrack = VehicleFactoryAdapter.getInstance().isDualAudioTrack();

        // compile switch
        mPriorityArbitrationModule =
                VehicleFactoryAdapter.getInstance().getArbitrationModule(isDulAudioTrack, mMusicQueue, mTTSQueue);
        mVehicleTTSProcessor = new NaviReceiveProcessor();
        mVehicleTTSProcessor.initQueue(mTTSQueue);

        mVehicleVRProcessor = new VRReceiveProcessor();
        mVehicleVRProcessor.initQueue(mVRQueue);

        mVehicleMusicProcessor = new MediaReceiveProcessor(mPriorityArbitrationModule);
        mVehicleMusicProcessor.initQueue(mMusicQueue);

        // compile switch
        mAMPPlayProcess =
                VehicleFactoryAdapter.getInstance().getAMPPlayProcess(isDulAudioTrack, mPriorityArbitrationModule);
        mAMPPlayProcess.initMusicQueue(mMusicQueue);
        mAMPPlayProcess.initTTSQueue(mTTSQueue);
        mAMPPlayProcess.initVRQueue(mVRQueue);
    }

    public void threadStart() {
        mVehicleMusicProcessor.startThread();
        mVehicleTTSProcessor.startThread();
        mVehicleVRProcessor.startThread();
        mAMPPlayProcess.startThread();
    }

    public int requestVRAudioFocus() {
        if (mPriorityArbitrationModule == null) {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
        return mPriorityArbitrationModule.requestVRAudioFocus();
    }

    public int abandonVRAudioFocus() {
        if (mPriorityArbitrationModule == null) {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }

        return mPriorityArbitrationModule.abandonVRAudioFocus();
    }

}
