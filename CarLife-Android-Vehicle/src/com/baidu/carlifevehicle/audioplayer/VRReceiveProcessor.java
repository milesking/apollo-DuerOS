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
 * receive thread for vr channel;
 *
 * @author liucaiquan
 */
public class VRReceiveProcessor {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + VRReceiveProcessor.class.getSimpleName();
    private static final String VR_MSG_HANDLE_THREAD_NAME = "VR_MSG_HANDLE_THREAD";

    private DataQueue mQueue;

    private PackageHeadAnalyseModule mPCMPackageHeadAnalyseMode;
    private int mPCMDataSize;

    private Thread mVRReceiveThread;
    private boolean mThreadFlag;

    private PCMPlayerUtils.EPCMPackageType mPCMPackageHeadType;
    private int mPCMPackageTimeStamp;
    private byte[] mVRPCMDataBuffer;

    private AESManager mAESManager = new AESManager();

    public VRReceiveProcessor() {
        mPCMPackageHeadAnalyseMode = new PackageHeadAnalyseModule();
    }

    public void initQueue(DataQueue pcmQueue) {
        mQueue = pcmQueue;
    }

    public void startThread() {
        mVRReceiveThread = new VRReceiveThread();

        if (mQueue != null) {
            mQueue.clear();
        } else {
            LogUtil.d(TAG, "mQueue has not been initialized!");
        }

        mVRReceiveThread.start();
    }

    private class VRReceiveThread extends Thread {

        public void run() {
            int usbConnectionStatus;

            mThreadFlag = true;
            while (mThreadFlag == true) {
                if (ConnectClient.getInstance().isCarlifeConnected()) {
                    // get PCMPackageHead
                    usbConnectionStatus =
                            ConnectManager.getInstance().readAudioVRData(
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadBuffer(),
                                    mPCMPackageHeadAnalyseMode.getPCMPackageHeadSize());
                    if (usbConnectionStatus >= 0) {
                        mPCMPackageHeadType = mPCMPackageHeadAnalyseMode.getPCMPackageHeadType();
                        mPCMPackageTimeStamp = mPCMPackageHeadAnalyseMode.getPCMPackageHeadTimeStamp();

                        // Increase the voice interrupt message, receive voice interrupt message, Delete the received data
                        if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.VR_INTERRUPT) {
                            mQueue.clear();
                            LogUtil.d(TAG, "TTS Init: Clear Queue!", mQueue.getBufferDataNum());
                        }

                        // normal PCMDataPackage
                        if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.VR_NORMAL_DATA) {
                            mPCMDataSize = mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize();

                            if (mPCMDataSize < 0 || mPCMDataSize >= PCMPlayerUtils.MAX_RCEV_MEDIA_DATA_LEN) {
                                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
                                usbConnectionStatus = -1;
                                LogUtil.e(TAG, "VR: mPCMDataSize is negtive or too big, connection will be broken!");
                            } else {
                                mVRPCMDataBuffer = new byte[mPCMDataSize];

                                // LogUtil.d(TAG, "get VR normal data, data size: "+ mPCMDataSize);

                                usbConnectionStatus =
                                        ConnectManager.getInstance().readAudioVRData(mVRPCMDataBuffer, mPCMDataSize);

                                // Decryption processing
                                if (EncryptSetupManager.getInstance().isEncryptEnable() && mPCMDataSize > 0) {
                                    mVRPCMDataBuffer = mAESManager.decrypt(mVRPCMDataBuffer,
                                            mPCMDataSize);
                                    if (mVRPCMDataBuffer == null) {
                                        LogUtil.e(TAG, "decrypt failed!");
                                        return;
                                    }
                                    mPCMDataSize = mVRPCMDataBuffer.length;
                                }
                            }

                            if (usbConnectionStatus >= 0) {
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mQueue.add(PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA, mPCMPackageTimeStamp,
                                            mVRPCMDataBuffer, mPCMDataSize);
                                }
                            } else {
                                mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                break;
                            }
                        } else {
                            if (mPCMPackageHeadType == PCMPlayerUtils.EPCMPackageType.VR_INITIAL) {

                                int dataSize = mPCMPackageHeadAnalyseMode.getPCMPackageHeadDataSize();

                                usbConnectionStatus =
                                        ConnectManager.getInstance().readAudioVRData(
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
                                            "get VR init: " + mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleRate()
                                                    + " " + mPCMPackageHeadAnalyseMode.getTTSAudioTrackChannelConfig()
                                                    + " " + +mPCMPackageHeadAnalyseMode.getTTSAudioTrackSampleFormat());
                                } else {
                                    mQueue.add(PCMPlayerUtils.EPCMPackageType.AMP_PLAYER_RELEASE);
                                    break;
                                }
                            } else {
                                if (!PCMPlayerUtils.isBTTransmissionMode()) {
                                    mQueue.add(PCMPlayerUtils.EPCMPackageType.TTS_STOP);
                                }
                                LogUtil.d(TAG, "get VR stop");
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
