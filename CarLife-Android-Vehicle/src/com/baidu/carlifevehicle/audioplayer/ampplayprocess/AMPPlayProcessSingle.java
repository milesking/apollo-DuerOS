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
package com.baidu.carlifevehicle.audioplayer.ampplayprocess;

import com.baidu.carlifevehicle.audioplayer.DataQueue;
import com.baidu.carlifevehicle.audioplayer.DataUnit;
import com.baidu.carlifevehicle.audioplayer.PCMPlayerUtils;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleInterface;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * module functions:
 * <p>
 * 1.audio play( three thread: media, navi, vr);
 * <p>
 * 2.using ArbitrationModuleSingel;
 * 
 * @author Liu Caiquan
 * 
 */
public class AMPPlayProcessSingle implements AMPPlayProcessInterface {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + AMPPlayProcessSingle.class.getSimpleName();

    private DataQueue mMusicQueue;
    private DataQueue mNaviQueue;
    private DataQueue mVRQueue;

    private ArbitrationModuleInterface mPriorityArbitrationModule;

    private Thread mAMPMusicPlayThread;
    private Thread mAMPNaviPlayThread;
    private Thread mAMPVRPlayThread;

    private boolean mMusicThreadFlag;
    private boolean mNaviThreadFlag;
    private boolean mVRThreadFlag;

    private DataUnit mMusicDataUnit;
    private DataUnit mNaviDataUnit;
    private DataUnit mVRDataUnit;

    private boolean mMusicThreadRunningFlag = true;
    private boolean mNaviThreadRunningFlag = true;

    public AMPPlayProcessSingle(ArbitrationModuleInterface priArbM) {
        mPriorityArbitrationModule = priArbM;
    }

    @Override
    public void initMusicQueue(DataQueue queue) {
        mMusicQueue = queue;
    }

    @Override
    public void initTTSQueue(DataQueue queue) {
        mNaviQueue = queue;
    }

    @Override
    public void initVRQueue(DataQueue queue) {
        mVRQueue = queue;
    }

    @Override
    public void startThread() {
        mAMPMusicPlayThread = new AMPMusicPlayThread();
        mAMPNaviPlayThread = new AMPNaviPlayThread();
        mAMPVRPlayThread = new AMPVRPlayThread();

        mAMPMusicPlayThread.start();
        LogUtil.d(TAG, "AMPMusicPlayThread start!");

        mAMPNaviPlayThread.start();
        LogUtil.d(TAG, "AMPTTSPlayThread start!");

        mAMPVRPlayThread.start();
        LogUtil.d(TAG, "AMPVRPlayThread start!");
    }

    private class AMPMusicPlayThread extends Thread {

        public void run() {
            mMusicThreadFlag = true;
            while (mMusicThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    mMusicDataUnit = mMusicQueue.takeIndex();

                    // reduce CPU occupancy rate
                    if (mMusicDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE) {
                        LogUtil.d(TAG, "get AMP_PLAYER_RELEASE_INDEX");
                        break;
                    }

                    if (mMusicDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.MUSIC_NORMAL_DATA) {
                        ampPlayerMusicProcess();
                    }

                } else {
                    break;
                }
            }
        }
    }

    private class AMPNaviPlayThread extends Thread {

        public void run() {
            mNaviThreadFlag = true;
            while (mNaviThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    mNaviDataUnit = mNaviQueue.takeIndex();

                    // reduce CPU occupancy rate
                    if (mNaviDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE) {
                        LogUtil.d(TAG, "get AMP_PLAYER_RELEASE_INDEX");
                        break;
                    }

                    if (mNaviDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA) {
                        ampPlayerTTSProcess();
                    } else if (mNaviDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_INITIAL) {
                        /**
                         * 1. clear music queue;
                         * <p>
                         * 2. avoid music thread seize mAMPStatusLock
                         * <p>
                         * which will result in tts thread blocking;
                         */
                        pauseMusicThread();
                        mPriorityArbitrationModule.priorityArbitrationProcessor(mNaviDataUnit.getHeadType(),
                                mNaviDataUnit.getSampleRate(), mNaviDataUnit.getChannelConfig(),
                                mNaviDataUnit.getFormat());

                        LogUtil.d(TAG, "get PCM frame: TTS_INITIAL");
                    } else if (mNaviDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_STOP) {
                        resumeMusicThread();
                        mPriorityArbitrationModule.priorityArbitrationProcessor(
                                PCMPlayerUtils.EPCMPackageType.TTS_STOP, 0, 0, 0);
                    } else {
                        LogUtil.d(TAG, "get PCM frame: TTS_OTHER_COMMAND");
                    }
                } else {
                    break;
                }
            }
        }
    }

    private class AMPVRPlayThread extends Thread {

        public void run() {
            mVRThreadFlag = true;
            while (mVRThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    mVRDataUnit = mVRQueue.takeIndex();

                    // reduce CPU occupancy rate
                    if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE) {
                        LogUtil.d(TAG, "get AMP_PLAYER_RELEASE_INDEX");
                        break;
                    }

                    if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA) {
                        ampPlayerVRProcess();

                        // LogUtil.d(TAG, "get PCM frame: VR_NORMAL_DATA");
                    } else if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_INITIAL) {
                        /**
                         * 1. clear music and tts queue;
                         * <p>
                         * 2. avoid music and tts thread seize mAMPStatusLock
                         * <p>
                         * which will result in vr thread blocking;
                         */
                        pauseMusicThread();
                        pauseNaviThread();

                        mPriorityArbitrationModule.priorityArbitrationProcessor(mVRDataUnit.getHeadType(),
                                mVRDataUnit.getSampleRate(), mVRDataUnit.getChannelConfig(), mVRDataUnit.getFormat());

                        LogUtil.d(TAG, "get PCM frame: VR_INITIAL");
                    } else if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_STOP) {
                        resumeMusicThread();
                        resumeNaviThread();
                        mPriorityArbitrationModule.priorityArbitrationProcessor(
                                PCMPlayerUtils.EPCMPackageType.TTS_STOP, 0, 0, 0);
                    } else {
                        LogUtil.d(TAG, "get PCM frame: VR_OTHER_COMMAND");
                    }

                } else {
                    break;
                }
            }
        }
    }

    private void ampPlayerMusicProcess() {
        if (mMusicThreadRunningFlag) {
            if (mPriorityArbitrationModule.getAMPStatus() == PCMPlayerUtils.EAMPStatus.MUSIC_USED) {
                mPriorityArbitrationModule
                        .writeAudioTrack(mMusicDataUnit.getPCMData(), 0, mMusicDataUnit.getDataSize());
            }
        }
    }

    private void ampPlayerTTSProcess() {
        if (mNaviThreadRunningFlag) {
            mPriorityArbitrationModule.writeAudioTrack(mNaviDataUnit.getPCMData(), 0, mNaviDataUnit.getDataSize());
        }
    }

    private void ampPlayerVRProcess() {
        mPriorityArbitrationModule.writeAudioTrack(mVRDataUnit.getPCMData(), 0, mVRDataUnit.getDataSize());
    }

    private void pauseMusicThread() {
        mMusicThreadRunningFlag = false;

        mMusicQueue.clear();
    }

    private void resumeMusicThread() {
        mMusicQueue.clear();
        mMusicThreadRunningFlag = true;
    }

    private void pauseNaviThread() {
        mNaviThreadRunningFlag = false;
        mNaviQueue.clear();
    }

    private void resumeNaviThread() {
        mNaviQueue.clear();
        mNaviThreadRunningFlag = true;
    }
}
