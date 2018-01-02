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
package com.baidu.carlifevehicle.audioplayer.arbitrationmodule;


import com.baidu.carlifevehicle.audioplayer.DataQueue;
import com.baidu.carlifevehicle.audioplayer.ModeService;
import com.baidu.carlifevehicle.audioplayer.PCMPlayerUtils;
import com.baidu.carlifevehicle.audioplayer.VehicleFactoryAdapter;
import com.baidu.carlifevehicle.audioplayer.audiotrackmanager.AudioTrackManagerDualInterface;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * module functions:
 * <p>
 * 1.priority arbitration;
 * <p>
 * 2.audio track management;
 * <p>
 * 3.using AudioTrackManagerSingle;
 * <p>
 * <p>
 * TTS and Music will not affect each other because of lock mechanism;
 * 
 * @author Liu Caiquan
 * 
 */

public class ArbitrationModuleDual implements ArbitrationModuleInterface {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + ArbitrationModuleDual.class.getSimpleName();

    private AudioTrackManagerDualInterface mVehicleAudioTrackMannager;

    /**
     * avoid Music Receive thread and Playback thread use music AudioTrack synchronously;
     */
    private Object mMusicAudioTrackLock;
    /**
     * avoid VR thread and Navi thread use TTS audio track synchronously;
     */
    private Object mTTSAudioTrackLock;

    private ModeService mModeService;

    private boolean mIsMediaPlaying = false;

    private boolean mIsConnectionBreak = false;

    private boolean mIsClearBuffer = false;

    public ArbitrationModuleDual(DataQueue musicQueue, DataQueue ttsQueue) {
        mVehicleAudioTrackMannager =
                VehicleFactoryAdapter.getInstance().getAudioTrackManager();
        mMusicAudioTrackLock = new Object();
        mTTSAudioTrackLock = new Object();

        mModeService = ModeService.getInstance();
    }

    @Override
    public void priorityArbitrationProcessor(PCMPlayerUtils.EPCMPackageType packageType, int sampleRate,
            int channelConfig, int format) {
        mModeService.setMode(packageType);
        switch (packageType) {
            case MUSIC_STOP:
                setMediaPlayStatus(false);
                pauseMusicAudioTrack();

                break;

            case MUSIC_INITIAL:
                initMusicAudioTrack(sampleRate, channelConfig, format);
                setMediaPlayStatus(true);
                break;
            case MUSIC_PAUSE:
                setMediaPlayStatus(false);
                pauseMusicAudioTrack();

                break;
            case MUSIC_RESUME_PLAY:
                resumeMusicAudioTrack();
                setMediaPlayStatus(true);
                break;
            case MUSIC_SEEK_TO:

                break;
            case TTS_INITIAL:
                initTTSAudioTrack(PCMPlayerUtils.TTS_TYPE_NAVI, sampleRate, channelConfig, format);

                break;
            case TTS_STOP:
                pauseTTSAudioTrack();
                break;

            case VR_INITIAL:
                initTTSAudioTrack(PCMPlayerUtils.TTS_TYPE_VR, sampleRate, channelConfig, format);
                break;

            case VR_STOP:
                pauseTTSAudioTrack();
                break;
            case PHONE_START:
                break;
            case PHONE_STOP:
                LogUtil.d(TAG, "command: PHONE_STOP");
                break;
            default:

                break;
        }
    }

    @Override
    public void writeAudioTrack(byte[] data, int offset, int size) {

    }

    @Override
    public void writeMusicAudioTrack(byte[] data, int offset, int size) {
        if (mIsConnectionBreak) {
            return;
        }

        synchronized (mMusicAudioTrackLock) {
            if (!getMediaPlayStatus()) {
                try {
                    mMusicAudioTrackLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!mIsConnectionBreak) {
                if (!mIsClearBuffer) {
                    mVehicleAudioTrackMannager.writeMusicAudioTrack(data, offset, size);
                } else {
                    mIsClearBuffer = false;
                }
            }
        }
    }

    @Override
    public void writeTTSAudioTrack(byte[] data, int offset, int size) {
        synchronized (mTTSAudioTrackLock) {
            mVehicleAudioTrackMannager.writeTTSAudioTrack(data, offset, size);
        }
    }

    @Override
    public PCMPlayerUtils.EAMPStatus getAMPStatus() {
        return PCMPlayerUtils.EAMPStatus.INVALID_STATUS;
    }

    @Override
    public void informMediaPlayThreadRelease() {
        mIsConnectionBreak = true;
        synchronized (mMusicAudioTrackLock) {
            mMusicAudioTrackLock.notify();
        }
    }

    private void pauseMusicAudioTrack() {
        synchronized (mMusicAudioTrackLock) {
            mVehicleAudioTrackMannager.pauseMusicAudioTrack();
        }
    }

    private void resumeMusicAudioTrack() {
        // no need to add lock
        mVehicleAudioTrackMannager.resumeMusicAudioTrack();
    }

    private void initMusicAudioTrack(int sampleRate, int channelConfig, int format) {
        synchronized (mMusicAudioTrackLock) {
            mVehicleAudioTrackMannager.initMusicAudioTrack(sampleRate, channelConfig, format);
        }
    }

    private void pauseTTSAudioTrack() {
        synchronized (mTTSAudioTrackLock) {
            mVehicleAudioTrackMannager.pauseTTSAudioTrack();
        }
    }

    private void initTTSAudioTrack(int ttyType, int sampleRate, int channelConfig, int format) {
        synchronized (mTTSAudioTrackLock) {
            mVehicleAudioTrackMannager.initTTSAudioTrack(ttyType, sampleRate, channelConfig, format);
        }
    }

    private void setMediaPlayStatus(boolean isPlaying) {
        mIsMediaPlaying = isPlaying;
        mIsClearBuffer = true;

        if (mIsMediaPlaying) {
            synchronized (mMusicAudioTrackLock) {
                mMusicAudioTrackLock.notify();
            }
        }
    }

    private boolean getMediaPlayStatus() {
        return mIsMediaPlaying;
    }

    public int requestVRAudioFocus() {
        return mVehicleAudioTrackMannager.requestVRAudioFocus();
    }

    public int abandonVRAudioFocus() {
        return mVehicleAudioTrackMannager.abandonVRAudioFocus();
    }

}
