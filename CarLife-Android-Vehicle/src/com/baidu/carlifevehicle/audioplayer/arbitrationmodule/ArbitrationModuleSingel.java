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

import android.media.AudioManager;

import com.baidu.carlifevehicle.audioplayer.DataQueue;
import com.baidu.carlifevehicle.audioplayer.ModeService;
import com.baidu.carlifevehicle.audioplayer.PCMPlayerUtils;
import com.baidu.carlifevehicle.audioplayer.audiotrackmanager.AudioTrackManagerSingle;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * 
 * 
 * Module functions:
 * <p>
 * 1.priority arbitration;
 * <p>
 * 2.audio track management;
 * <p>
 * 3.using AudioTrackManagerSingle.
 * <p>
 * <p>
 * TTS and Music will not affect each other because of lock mechanism.
 * 
 * @author Liu Caiquan
 */

public class ArbitrationModuleSingel implements ArbitrationModuleInterface {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX
            + ArbitrationModuleSingel.class.getSimpleName();

    private PCMPlayerUtils.EAMPStatus mEAMPStatus;

    private int mMusicAudioTrackSampleRate;
    private int mTTSAudioTrackSampleRate;

    // 1:mono
    // 2:stereo
    private int mMusicAudioTrackChannelConfig;
    private int mTTSAudioTrackChannelConfig;

    private int mMusicAudioTrackSampleFormat;
    private int mTTSAudioTrackSampleFormat;

    private boolean mPreMusicAMPStatus;

    private int mPreMusicAudioTrackSampleRate;
    private int mPreMusicAudioTrackChannelConfig;
    private int mPreMusicAudioTrackSampleFormat;

    private AudioTrackManagerSingle mVehicleAudioTrackMannager;

    /**
     * avoid Navi VR Music use AMP/AudioTrack synchronously;
     */
    private Object mAMPStatusLock; // tts vs media
    private Object mAudioTrackLock;

    private ModeService mModeService;

    public ArbitrationModuleSingel(DataQueue musicQueue, DataQueue ttsQueue) {
        mEAMPStatus = PCMPlayerUtils.EAMPStatus.INVALID_STATUS;

        mPreMusicAMPStatus = false;

        mVehicleAudioTrackMannager = AudioTrackManagerSingle.getInstance();

        mAMPStatusLock = new Object();
        mAudioTrackLock = new Object();

        mModeService = ModeService.getInstance();
    }

    @Override
    public void priorityArbitrationProcessor(PCMPlayerUtils.EPCMPackageType packageType, int sampleRate,
            int channelConfig, int format) {
        mModeService.setMode(packageType);

        switch (packageType) {
            case MUSIC_STOP:
                if (getAMPStatus() == PCMPlayerUtils.EAMPStatus.MUSIC_USED) {
                    pauseAudioTrack();
                }
                releaseAudioTrack(AudioTrackManagerSingle.EAudioTrackType.MEDIA);

                break;

            case MUSIC_INITIAL:
                mMusicAudioTrackSampleRate = sampleRate;
                mMusicAudioTrackChannelConfig = channelConfig;
                mMusicAudioTrackSampleFormat = format;

                mPreMusicAMPStatus = true;
                mPreMusicAudioTrackSampleRate = mMusicAudioTrackSampleRate;
                mPreMusicAudioTrackChannelConfig = mMusicAudioTrackChannelConfig;
                mPreMusicAudioTrackSampleFormat = mMusicAudioTrackSampleFormat;

                if (getAMPStatus() != PCMPlayerUtils.EAMPStatus.TTS_USED) {

                    initAudioTrack(mMusicAudioTrackSampleRate, mMusicAudioTrackChannelConfig,
                            mMusicAudioTrackSampleFormat, AudioTrackManagerSingle.EAudioTrackType.MEDIA);

                    // get AMP
                    setAMPStatus(PCMPlayerUtils.EAMPStatus.MUSIC_USED);
                }

                break;
            case MUSIC_PAUSE:
                if (getAMPStatus() == PCMPlayerUtils.EAMPStatus.MUSIC_USED) {
                    pauseAudioTrack();
                }

                break;
            case MUSIC_RESUME_PLAY:
                resumeAudioTrack();
                break;
            case MUSIC_SEEK_TO:
                break;
            case TTS_INITIAL:
                mTTSAudioTrackSampleRate = sampleRate;
                mTTSAudioTrackChannelConfig = channelConfig;
                mTTSAudioTrackSampleFormat = format;

                initAudioTrack(mTTSAudioTrackSampleRate, mTTSAudioTrackChannelConfig, mTTSAudioTrackSampleFormat,
                        AudioTrackManagerSingle.EAudioTrackType.TTS);

                // get AMP
                setAMPStatus(PCMPlayerUtils.EAMPStatus.TTS_USED);

                break;
            case TTS_STOP:

                releaseAudioTrack(AudioTrackManagerSingle.EAudioTrackType.TTS);

                if (mPreMusicAMPStatus == true) {
                    mMusicAudioTrackSampleRate = mPreMusicAudioTrackSampleRate;
                    mMusicAudioTrackChannelConfig = mPreMusicAudioTrackChannelConfig;
                    mMusicAudioTrackSampleFormat = mPreMusicAudioTrackSampleFormat;

                    initAudioTrack(mMusicAudioTrackSampleRate, mMusicAudioTrackChannelConfig,
                            mMusicAudioTrackSampleFormat, AudioTrackManagerSingle.EAudioTrackType.MEDIA);

                    // music get channel again
                    setAMPStatus(PCMPlayerUtils.EAMPStatus.MUSIC_USED);

                } else if (getAMPStatus() == PCMPlayerUtils.EAMPStatus.TTS_USED) {
                    // release AMP
                    setAMPStatus(PCMPlayerUtils.EAMPStatus.INVALID_STATUS);
                }

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
        synchronized (mAudioTrackLock) {
            mVehicleAudioTrackMannager.writeAudioTrack(data, offset, size);
        }
    }

    @Override
    public void writeTTSAudioTrack(byte[] data, int offset, int size) {

    }

    @Override
    public void writeMusicAudioTrack(byte[] data, int offset, int size) {

    }

    @Override
    public PCMPlayerUtils.EAMPStatus getAMPStatus() {
        synchronized (mAMPStatusLock) {
            return mEAMPStatus;
        }
    }

    @Override
    public void informMediaPlayThreadRelease() {

    }

    private void setAMPStatus(PCMPlayerUtils.EAMPStatus status) {
        synchronized (mAMPStatusLock) {
            mEAMPStatus = status;
        }
    }

    private void pauseAudioTrack() {
        synchronized (mAudioTrackLock) {
            mVehicleAudioTrackMannager.pauseAudioTrack();
        }
    }

    private void resumeAudioTrack() {
        mVehicleAudioTrackMannager.resumeAudioTrack(); // no need to add lock
    }

    private void initAudioTrack(int sampleRate, int channelConfig, int format,
            AudioTrackManagerSingle.EAudioTrackType type) {
        synchronized (mAudioTrackLock) {
            mVehicleAudioTrackMannager.initAudioTrack(sampleRate, channelConfig, format, type);
        }
    }

    private void releaseAudioTrack(AudioTrackManagerSingle.EAudioTrackType type) {
        synchronized (mAudioTrackLock) {
            mVehicleAudioTrackMannager.releaseAudioTrack(type);
        }
    }

    public int requestVRAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    public int abandonVRAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

}
