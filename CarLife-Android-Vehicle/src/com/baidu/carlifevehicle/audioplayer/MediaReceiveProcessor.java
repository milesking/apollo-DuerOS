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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.audioplayer.arbitrationmodule.ArbitrationModuleInterface;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.encryption.AESManager;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * module functions:
 * <p>
 * receive music data package;
 *
 * @author Liu Caiquan
 */
public class MediaReceiveProcessor {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + MediaReceiveProcessor.class.getSimpleName();

    private DataQueue mQueue;

    private PackageHeadAnalyseModule mPCMPackageHeadAnalyseMode;

    private Thread mMusicPCMReceiveThread;

    private boolean mThreadFlag;

    private PCMPlayerUtils.EPCMPackageType mPCMPackageHeadType;
    private int mTimeStamp;

    private int mPCMDataSize;

    private byte[] mMusicPCMDataBuffer;

    private AESManager mAESManager = new AESManager();

    /**
     * reason for using Arbitration module in Music receive thread:
     * <p>
     * fast previous/next operation will result bad user experience(all previous/next operation will be execute at HU
     * side which is not expected)
     */
    private ArbitrationModuleInterface mPriorityArbitrationModuleInterface;

    public MediaReceiveProcessor(ArbitrationModuleInterface priorityArbitratorModule) {
        mPriorityArbitrationModuleInterface = priorityArbitratorModule;

        mPCMPackageHeadAnalyseMode = new PackageHeadAnalyseModule();

    }

    public void initQueue(DataQueue pcmQueue) {
        mQueue = pcmQueue;
    }

    public void startThread() {
        mMusicPCMReceiveThread = new MusicPCMReceiveThread();

        if (mQueue != null) {
            mQueue.clear();
        } else {
            LogUtil.d(TAG, "mQueue has not been initialized!");
        }
        mMusicPCMReceiveThread.start();
    }

    private class MusicPCMReceiveThread extends Thread {

        public void run() {
            int usbConnectiontatus;

            mThreadFlag = true;
            while (mThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    // get PCMPackageHead
                    usbConnectiontatus =
                            ConnectManager.getInstance().readAudioData(
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadBuffer(),
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadSize());

                    if (usbConnectiontatus >= 0) {
                        mPCMPackageHeadType = mPCMPackageHeadAnalyseMode.getPCMPackageHeadType();
                        mTimeStamp = mPCMPackageHeadAnalyseMode.getPCMPackageHeadTimeStamp();
                        // normal PCMDataPackage
                        if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_NORMAL_DATA) {
                            mPCMDataSize = mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize();
                            if (mPCMDataSize < 0 || mPCMDataSize >= PCMPlayerUtils.MAX_RCEV_MEDIA_DATA_LEN) {
                                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
                                usbConnectiontatus = -1;
                                LogUtil.e(TAG, "Media: mPCMDataSize is negtive or too big, connection will be broken!");
                            } else {
                                mMusicPCMDataBuffer = new byte[mPCMDataSize];

                                usbConnectiontatus =
                                        ConnectManager.getInstance().readAudioData(mMusicPCMDataBuffer, mPCMDataSize);
                                // Decryption processing
                                if (EncryptSetupManager.getInstance().isEncryptEnable() && mPCMDataSize > 0) {
                                    mMusicPCMDataBuffer = mAESManager.decrypt(mMusicPCMDataBuffer,
                                            mPCMDataSize);
                                    if (mMusicPCMDataBuffer == null) {
                                        LogUtil.e(TAG, "decrypt failed!");
                                        return;
                                    }
                                    mPCMDataSize = mMusicPCMDataBuffer.length;
                                }
                            }

                            if (usbConnectiontatus >= 0) {
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mQueue.add(mPCMPackageHeadType, mTimeStamp, mMusicPCMDataBuffer, mPCMDataSize);
                                }

                                // normally this log should be closed
                            } else {
                                mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                mPriorityArbitrationModuleInterface.informMediaPlayThreadRelease();
                                break;
                            }
                        } else {
                            // command PCMDataPackage
                            if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_INITIAL) {
                                usbConnectiontatus =
                                        ConnectManager.getInstance().readAudioData(
                                                mPCMPackageHeadAnalyseMode.getMusicAudioTrackInitParameterDataBuf(),
                                                mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize());

                                if (usbConnectiontatus >= 0) {
                                    mQueue.clear();

                                    mPCMPackageHeadAnalyseMode.getMusicAudioTrackInitParameter();
                                    if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                        mPriorityArbitrationModuleInterface.priorityArbitrationProcessor(
                                                PCMPlayerUtils.EPCMPackageType.MUSIC_INITIAL,
                                                mPCMPackageHeadAnalyseMode.getMusicAudioTrackSampleRate(),
                                                mPCMPackageHeadAnalyseMode.getMusicAudioTrackChannelConfig(),
                                                mPCMPackageHeadAnalyseMode.getMusicAudioTrackSampleFormat());
                                    }

                                    LogUtil.d(
                                            TAG,
                                            "get data frame: MUSIC_INITIAL " + "music sampleRate: "
                                                    + mPCMPackageHeadAnalyseMode.getMusicAudioTrackSampleRate()
                                                    + "music channelConfig: "
                                                    + mPCMPackageHeadAnalyseMode.getMusicAudioTrackChannelConfig()
                                                    + "music format: "
                                                    + mPCMPackageHeadAnalyseMode.getMusicAudioTrackSampleFormat());
                                } else {
                                    mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                    mPriorityArbitrationModuleInterface.informMediaPlayThreadRelease();
                                    break;
                                }
                            } else {
                                // MUSIC_OTHER_COMMAND
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mPriorityArbitrationModuleInterface.priorityArbitrationProcessor(
                                            mPCMPackageHeadType, 0, 0, 0);
                                }

                                LogUtil.d(TAG, "get data frame: MUSIC_OTHER_COMMAND");
                                if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_PAUSE) {
                                    LogUtil.d(TAG, "MUSIC_PAUSE");
                                } else if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_RESUME_PLAY) {
                                    LogUtil.d(TAG, "MUSIC_RESUME_PLAY");
                                } else if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_SEEK_TO) {
                                    LogUtil.d(TAG, "MUSIC_SEEK_TO");
                                } else if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.MUSIC_STOP) {
                                    LogUtil.d(TAG, "MUSIC_STOP");
                                }
                            }
                        }

                    } else {
                        mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                        mPriorityArbitrationModuleInterface.informMediaPlayThreadRelease();
                        break;
                    }
                } else {
                    mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                    mPriorityArbitrationModuleInterface.informMediaPlayThreadRelease();
                    break;
                }
            }
        }
    }
}
