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

import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class PcmRecorder extends Thread {

    private static final String TAG = "CarLifeVoice";
    private static final int RECORD_SAMPLE_RATE_8K = 8000;
    private static final int RECORD_SAMPLE_RATE_16K = 16000;
    private static final int RECORD_DATA_PACKAGE_SIZE = 1024;
    private volatile boolean isRecording = false;
    private final Object mutex = new Object();
    private static final int FREQUENCY = RECORD_SAMPLE_RATE_16K;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * set to 64k Bytes to avoid the buffer to be flushed sometimes
     * **/
    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    private PcmSender mPcmSender = null;
    private AudioRecord mRecordInstance = null;

    public PcmRecorder() {
        super();
        LogUtil.d(TAG, "--new---PcmRecorder()--");
        mPcmSender = new PcmSender();
    }

    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        mPcmSender.start();
        if (mRecordInstance == null) {
            mRecordInstance =
                    new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, FREQUENCY,
                            AudioFormat.CHANNEL_IN_MONO, AUDIO_ENCODING, DEFAULT_BUFFER_SIZE);
        }

        int bufferRead = 0;
        byte[] tempBufferBytes = new byte[RECORD_DATA_PACKAGE_SIZE];
        boolean isStoped = false;

        while (true) {

            if (!this.isRecording) {
                synchronized (mutex) {
                    if (!this.isRecording) {
                        try {
                            mutex.wait();
                        } catch (InterruptedException e) {
                            throw new IllegalStateException("Wait() interrupted!", e);
                        }
                    }
                }
            }

            try {
                mRecordInstance.startRecording();
            } catch (IllegalStateException e) {
                LogUtil.e(TAG, "startRecording--error");
                CarlifeUtil.showToastInUIThread(R.string.pcm_recorder_error);
                break;
            }
            mPcmSender.setRecording(true);
            while (this.isRecording) {
                bufferRead = mRecordInstance.read(tempBufferBytes, 0, RECORD_DATA_PACKAGE_SIZE);
                if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    LogUtil.e(TAG, "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                    CarlifeUtil.showToastInUIThread(R.string.pcm_recorder_wrong);
                    isStoped = true;
                    break;
                } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
                    LogUtil.e(TAG, "read() returned AudioRecord.ERROR_BAD_VALUE");
                    CarlifeUtil.showToastInUIThread(R.string.pcm_recorder_wrong);
                    isStoped = true;
                    break;
                }
                mPcmSender.putData(tempBufferBytes, bufferRead);
            }

            mRecordInstance.stop();
            mPcmSender.setRecording(false);
            if (isStoped) {
                break;
            }
        }

        mRecordInstance.release();
        mRecordInstance = null;
    }

    public void setRecording(boolean flag) {
        this.isRecording = flag;
        if (this.isRecording) {
            synchronized (mutex) {
                if (this.isRecording) {
                    mutex.notify();
                }
            }
        }
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    public void setDownSampleStatus(boolean flag) {
        if (mPcmSender != null) {
            mPcmSender.setDownSampleStatus(flag);
        }
    }

}
