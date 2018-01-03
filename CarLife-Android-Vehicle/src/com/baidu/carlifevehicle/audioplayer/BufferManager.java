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

import com.baidu.carlifevehicle.util.LogUtil;

/** reserved for future using */
public class BufferManager {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + BufferManager.class.getSimpleName();
    private int mThresholdValue = PCMPlayerUtils.MEDIA_AUDIO_TRACK_BUFFER_SIZE;
    private boolean mIsThresholdEnable = false;
    private int mCurrentDataSize = 0;
    private Object mLock = new Object();

    // called by media receive thread
    public void addCommand(PCMPlayerUtils.EPCMPackageType type) {
        switch (type) {
            case MUSIC_STOP:
                // mIsThresholdEnable=false;
                break;
            case MUSIC_INITIAL:
                mCurrentDataSize = 0;
                mIsThresholdEnable = true;
                break;
            case MUSIC_PAUSE:
                // mIsThresholdEnable=false;
                break;
            case MUSIC_RESUME_PLAY:
                mCurrentDataSize = 0;
                mIsThresholdEnable = true;
                break;
            case MUSIC_SEEK_TO:
                mCurrentDataSize = 0;
                mIsThresholdEnable = true;
                break;

            default:
                break;
        }
    }

    public void addData(int size) {
        if (mIsThresholdEnable) {
            mCurrentDataSize += size;
            if (mCurrentDataSize >= mThresholdValue) {
                mCurrentDataSize = mThresholdValue;

                synchronized (mLock) {
                    mLock.notify();
                    LogUtil.d(TAG, "notify!");
                }
            }
        }
    }

    // called by AMP play thread
    public void mediaPlayStatusCheck() {
        if (mIsThresholdEnable) {
            synchronized (mLock) {
                try {
                    LogUtil.d(TAG, "media play thread is blocked!");

                    mLock.wait();
                    mIsThresholdEnable = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
