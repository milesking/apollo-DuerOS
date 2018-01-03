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
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.encryption.AESManager;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * module functions:
 * <p>
 * 1.receive TTS data package;
 *
 * @author Liu Caiquan
 */
public class NaviReceiveProcessor {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + NaviReceiveProcessor.class.getSimpleName();

    private DataQueue mQueue;

    private PackageHeadAnalyseModule mPCMPackageHeadAnalyseMode;
    private int mPCMDataSize;

    private Thread mTTSPCMReceiveThread;
    private boolean mThreadFlag;

    private PCMPlayerUtils.EPCMPackageType mPCMPackageHeadType;
    private int mPCMPackageTimeStamp;
    private byte[] mTTSPCMDataBuffer;

    private AESManager mAESManager = new AESManager();

    public NaviReceiveProcessor() {
        mPCMPackageHeadAnalyseMode = new PackageHeadAnalyseModule();
    }

    public void initQueue(DataQueue pcmQueue) {
        mQueue = pcmQueue;
    }

    public void startThread() {
        mTTSPCMReceiveThread = new TTSPCMReceiveThread();

        if (mQueue != null) {
            mQueue.clear();
        } else {
            LogUtil.d(TAG, "mQueue has not been initialized!");
        }

        mTTSPCMReceiveThread.start();
    }

    private class TTSPCMReceiveThread extends Thread {

        public void run() {
            int usbConnectionStatus;

            mThreadFlag = true;
            while (mThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    // get PCMPackageHead
                    usbConnectionStatus =
                            ConnectManager.getInstance().readAudioTTSData(
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadBuffer(),
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadSize());
                    if (usbConnectionStatus >= 0) {
                        mPCMPackageHeadType = mPCMPackageHeadAnalyseMode.getPCMPackageHeadType();
                        mPCMPackageTimeStamp = mPCMPackageHeadAnalyseMode.getPCMPackageHeadTimeStamp();

                        // normal PCMDataPackage
                        if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA) {
                            mPCMDataSize = mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize();

                            if (mPCMDataSize < 0 || mPCMDataSize >= PCMPlayerUtils.MAX_RCEV_MEDIA_DATA_LEN) {
                                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
                                usbConnectionStatus = -1;
                                LogUtil.e(TAG, "Navi: mPCMDataSize is negtive or too big, connection will be broken!");
                            } else {
                                mTTSPCMDataBuffer = new byte[mPCMDataSize];
                                usbConnectionStatus =
                                        ConnectManager.getInstance().readAudioTTSData(mTTSPCMDataBuffer, mPCMDataSize);

                                // Decryption processing
                                if (EncryptSetupManager.getInstance().isEncryptEnable() && mPCMDataSize > 0) {
                                    mTTSPCMDataBuffer = mAESManager.decrypt(mTTSPCMDataBuffer,
                                            mPCMDataSize);
                                    if (mTTSPCMDataBuffer == null) {
                                        LogUtil.e(TAG, "decrypt failed!");
                                        return;
                                    }
                                    mPCMDataSize = mTTSPCMDataBuffer.length;
                                }
                            }

                            if (usbConnectionStatus >= 0) {
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mQueue.add(mPCMPackageHeadType, mPCMPackageTimeStamp, mTTSPCMDataBuffer,
                                            mPCMDataSize);
                                }
                            } else {
                                mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                break;
                            }
                        } else {
                            if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.TTS_INITIAL) {

                                int dataSize = mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize();

                                usbConnectionStatus =
                                        ConnectManager.getInstance().readAudioTTSData(
                                                mPCMPackageHeadAnalyseMode.getTTSAudioTrackInitParameterDataBuf(),
                                                dataSize);

                                if (usbConnectionStatus >= 0) {
                                    mPCMPackageHeadAnalyseMode.getTTSAudioTrackInitParameter();

                                    if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                        mQueue.add(PCMPlayerUtils.EPCMPackageType.TTS_INITIAL,
                                                mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleRate(),
                                                mPCMPackageHeadAnalyseMode.getTTSAudioTrackChannelConfig(),
                                                mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleFormat());
                                    }

                                    LogUtil.d(TAG,
                                            "get Navi init: " + mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleRate()
                                                    + " " + mPCMPackageHeadAnalyseMode.getTTSAudioTrackChannelConfig()
                                                    + " " + mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleFormat());

                                } else {
                                    mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                    break;
                                }
                            } else {
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mQueue.add(mPCMPackageHeadType);
                                }

                                LogUtil.d(TAG, " get Navi stop");
                            }

                        }

                    } else {
                        mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                        break;
                    }
                } else {
                    mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                    break;
                }
            }
        }
    }
}
