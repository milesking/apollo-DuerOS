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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.encryption.AESManager;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.util.LogUtil;

public class PcmSender extends Thread {

    private static final String TAG = "CarLifeVoice";
    private static final int BUFFER_MAX_SIZE = 320;
    private final Object mutex = new Object();
    private volatile boolean isRecording = false;
    private DataOutputStream dataOutputStreamInstance;
    private boolean isDownSample = false;
    private ConcurrentLinkedQueue<RawData> list;
    private byte[] packageHead = new byte[12];

    private AESManager mAESManager = new AESManager();

    public PcmSender() {
        super();

        LogUtil.d(TAG, "-----PcmSender()---------");
        list = new ConcurrentLinkedQueue<RawData>();
    }

    public void run() {
        LogUtil.d(TAG, "pcmwriter thread runing");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        RawData rawData = null;
        while (true) {

            if (this.isRecording()) {
                LogUtil.d(TAG, "pcmwriter thread ---startRecord-");
                if (!list.isEmpty()) {
                    rawData = list.poll();
                    if (sendData(rawData) == -1) {
                        // 通道断开
                        list.clear();
                    }

                } else {
                    LogUtil.d(TAG, "pcmwriter thread -sleep(100)-");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }

            } else {
                LogUtil.d(TAG, "----rawData clear---");
                rawData = null;
                list.clear();
                synchronized (mutex) {
                    try {
                        mutex.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;

                    }
                }

            }
        }
    }

    public void putData(byte[] buf, int size) {
        LogUtil.d(TAG, "-putData----listLen:" + list.size() + "---dataSize:" + size);
        if (size <= 0) {
            return;
        }

        if (list.size() < BUFFER_MAX_SIZE) {
            RawData data = new RawData();
            data.size = size;
            data.pcm = new byte[size];
            System.arraycopy(buf, 0, data.pcm, 0, size);
            list.add(data);
        } else {
            LogUtil.e(TAG, "---putData---list is full-----");
        }

        return;
    }

    public void stopSaveToDisk() {
        if (dataOutputStreamInstance != null) {
            try {
                dataOutputStreamInstance.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataOutputStreamInstance = null;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
        if (this.isRecording) {
            synchronized (mutex) {
                if (this.isRecording) {
                    mutex.notify();
                }
            }
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setDownSampleStatus(boolean flag) {
        this.isDownSample = flag;
    }

    private class RawData {
        int size;
        byte[] pcm;
    }

    private int sendData(RawData data) {
        if (data == null || data.size == 0) {
            return 0;
        }

        byte[] downSampleBuffer = null;
        int ret = 0;
        if (isDownSample) {
            downSampleBuffer = downSample(data);
            ret = sendAudioVRData(downSampleBuffer, data.size / 2);
        } else {
            LogUtil.d(TAG, "pcmwriter thread -sendAudioVRData-");
            ret = sendAudioVRData(data.pcm, data.size);
        }

        return ret;
    }

    /**
     * down sample, 16k -> 8k
     * @param rawData raw data to be down sampled
     * @return down sampled data as byte array
     */
    private byte[] downSample(RawData rawData) {
        if (rawData == null || rawData.size == 0) {
            return null;
        }

        byte[] downSampleBuffer = new byte[rawData.size / 2];

        for (int i = 0, j = 0; i < rawData.size; i += 4) {
            downSampleBuffer[j++] = rawData.pcm[i];
            downSampleBuffer[j++] = rawData.pcm[i + 1];
        }
        return downSampleBuffer;
    }

    /**
     * send recorded audio data
     *
     * @return the count of data sent, -1 for error, socket is disconnected
     */
    private int sendAudioVRData(byte[] audioData, int length) {
        LogUtil.d(TAG, "----sendAudioVRData---len:" + length);

        setDataLenInHead(length);
        // timestamp
        packageHead[4] = 0;
        packageHead[5] = 0;
        packageHead[6] = 0;
        packageHead[7] = 0;
        // service type
        packageHead[8] = (byte) ((CommonParams.MSG_VR_DATA & 0xff000000) >> 24);
        packageHead[9] = (byte) ((CommonParams.MSG_VR_DATA & 0xff0000) >> 16);
        packageHead[10] = (byte) ((CommonParams.MSG_VR_DATA & 0xff00) >> 8);
        packageHead[11] = (byte) ((CommonParams.MSG_VR_DATA & 0xff) >> 0);

        // encrypt
        if (EncryptSetupManager.getInstance().isEncryptEnable() && length > 0) {
            audioData = mAESManager.encrypt(audioData, length);
            if (audioData == null) {
                LogUtil.e(TAG, "encrypt failed!");
                return -1;
            }
            length = audioData.length;
            setDataLenInHead(length);
        }

        ConnectManager.getInstance().writeAudioVRData(packageHead, 12);
        return ConnectManager.getInstance().writeAudioVRData(audioData, length);
    }

    private void setDataLenInHead(int length) {
        packageHead[0] = (byte) ((length & 0xff000000) >> 24);
        packageHead[1] = (byte) ((length & 0xff0000) >> 16);
        packageHead[2] = (byte) ((length & 0xff00) >> 8);
        packageHead[3] = (byte) ((length & 0xff) >> 0);
    }
}
