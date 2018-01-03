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
 * Module functions:
 * <p>
 * 1.audio play(three thread: media, navi, tts);
 * <p>
 * 2.using ArbitrationModuleSingel.
 * 
 * @author Liu Caiquan
 */
public class AMPPlayProcessDual implements AMPPlayProcessInterface {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + AMPPlayProcessDual.class.getSimpleName();

    private DataQueue mMusicQueue;
    private DataQueue mTTSQueue;
    private DataQueue mVRQueue;

    private ArbitrationModuleInterface mPriorityArbitrationModule;

    private Thread mAMPMusicPlayThread;
    private Thread mAMPTTSPlayThread;
    private Thread mAMPVRPlayThread;

    private boolean mMusicThreadFlag;
    private boolean mTTSThreadFlag;
    private boolean mVRThreadFlag;

    private DataUnit mMusicDataUnit;
    private DataUnit mTTSDataUnit;
    private DataUnit mVRDataUnit;

    public AMPPlayProcessDual(ArbitrationModuleInterface priorityArbitrationModule) {
        mPriorityArbitrationModule = priorityArbitrationModule;
    }

    @Override
    public void initMusicQueue(DataQueue queue) {
        mMusicQueue = queue;
    }

    @Override
    public void initTTSQueue(DataQueue queue) {
        mTTSQueue = queue;
    }

    @Override
    public void initVRQueue(DataQueue queue) {
        mVRQueue = queue;
    }

    @Override
    public void startThread() {
        mAMPMusicPlayThread = new AMPMusicPlayThread();
        mAMPTTSPlayThread = new AMPTTSPlayThread();
        mAMPVRPlayThread = new AMPVRPlayThread();

        mAMPMusicPlayThread.start();
        mAMPTTSPlayThread.start();
        mAMPVRPlayThread.start();
    }

    private class AMPMusicPlayThread extends Thread {

        public void run() {
            mMusicThreadFlag = true;
            while (mMusicThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {

                    mMusicDataUnit = mMusicQueue.takeIndex();

                    // reduce CPU occupancy rate
                    if (mMusicDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE) {
                        LogUtil.d(TAG, "get head type: AMP_PLAYER_RELEASE");
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

    private class AMPTTSPlayThread extends Thread {

        public void run() {
            mTTSThreadFlag = true;
            while (mTTSThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    mTTSDataUnit = mTTSQueue.takeIndex();

                    // reduce CPU occupancy rate
                    if (mTTSDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE) {
                        LogUtil.d(TAG, "get head type: AMP_PLAYER_RELEASE");
                        break;
                    }

                    if (mTTSDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA) {
                        ampPlayerTTSProcess();

                    } else if (mTTSDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_INITIAL) {
                        mPriorityArbitrationModule
                                .priorityArbitrationProcessor(mTTSDataUnit.getHeadType(), mTTSDataUnit.getSampleRate(),
                                        mTTSDataUnit.getChannelConfig(), mTTSDataUnit.getFormat());

                        LogUtil.d(TAG, "get PCM frame: TTS_INITIAL");
                    } else {
                        mPriorityArbitrationModule.priorityArbitrationProcessor(mTTSDataUnit.getHeadType(), 0, 0, 0);

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
                        LogUtil.d(TAG, "get head type: AMP_PLAYER_RELEASE");
                        break;
                    }

                    if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA) {
                        ampPlayerVRProcess();
                    } else if (mVRDataUnit.getHeadType() == PCMPlayerUtils.EPCMPackageType.TTS_INITIAL) {
                        // clear TTS buffer
                        mTTSQueue.clear();

                        mPriorityArbitrationModule.priorityArbitrationProcessor(
                                PCMPlayerUtils.EPCMPackageType.VR_INITIAL, mVRDataUnit.getSampleRate(),
                                mVRDataUnit.getChannelConfig(), mVRDataUnit.getFormat());

                        LogUtil.d(TAG, "get PCM frame: VR_INITIAL");
                    } else {
                        mPriorityArbitrationModule.priorityArbitrationProcessor(PCMPlayerUtils.EPCMPackageType.VR_STOP,
                                0, 0, 0);

                        LogUtil.d(TAG, "get PCM frame: VR_OTHER_COMMAND");
                    }

                } else {
                    break;
                }
            }
        }
    }

    private void ampPlayerMusicProcess() {
        mPriorityArbitrationModule.writeMusicAudioTrack(mMusicDataUnit.getPCMData(), 0, mMusicDataUnit.getDataSize());
    }

    private void ampPlayerTTSProcess() {
        mPriorityArbitrationModule.writeTTSAudioTrack(mTTSDataUnit.getPCMData(), 0, mTTSDataUnit.getDataSize());
    }

    private void ampPlayerVRProcess() {
        mPriorityArbitrationModule.writeTTSAudioTrack(mVRDataUnit.getPCMData(), 0, mVRDataUnit.getDataSize());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
