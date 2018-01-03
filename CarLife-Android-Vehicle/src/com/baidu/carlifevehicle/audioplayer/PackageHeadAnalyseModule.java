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

import com.baidu.carlife.protobuf.CarlifeMusicInitProto.CarlifeMusicInit;
import com.baidu.carlife.protobuf.CarlifeTTSInitProto.CarlifeTTSInit;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.encryption.AESManager;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.util.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * module function:
 * <p>
 * 1.data package head analyze
 *
 * @author Liu Caiquan
 */
public class PackageHeadAnalyseModule {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX
            + PackageHeadAnalyseModule.class.getSimpleName();

    /**
     * [0-3](0-31):length [4-7](32-63):time stamp [8-11](64-95):service type
     */
    private byte[] mPCMPacakgeHeadBuffer;
    private final int mPCMPackageHeadLen = 12;

    private int mMusicSampleRate;
    private int mMusicChannleCondfig;
    private int mMusicSampleFormate;

    private int mTTSSampleRate;
    private int mTTSChannleCondfig;
    private int mTTSSampleFormate;

    private byte[] mMusicAudioTrackInitParameterData;
    private byte[] mTTSAudioTrackInitParameterData;

    private AESManager mAESManager = new AESManager();

    public PackageHeadAnalyseModule() {
        mPCMPacakgeHeadBuffer = new byte[mPCMPackageHeadLen];

        mMusicAudioTrackInitParameterData = new byte[PCMPlayerUtils.MUSIC_AUDIO_TRACK_INIT_PARAMETER_DATA_SIZE];
        mTTSAudioTrackInitParameterData = new byte[PCMPlayerUtils.TTS_AUDIO_TRACK_INIT_PARAMETER_DATA_SIZE];
    }

    public byte[] getPCMPackageHeadBuffer() {
        return mPCMPacakgeHeadBuffer;
    }

    public byte[] getMusicAudioTrackInitParameterDataBuf() {
        return mMusicAudioTrackInitParameterData;
    }

    public byte[] getTTSAudioTrackInitParameterDataBuf() {
        return mTTSAudioTrackInitParameterData;
    }

    public int getPCMPackageHeadSize() {
        return mPCMPackageHeadLen;
    }

    // [0-3]:data length
    // BigEnd
    public int getPCMPackageHeadDataSize() {
        int dataSize;
        int dataHH = (mPCMPacakgeHeadBuffer[0] & 0xff);
        int dataHL = (mPCMPacakgeHeadBuffer[1] & 0xff);
        int dataLH = (mPCMPacakgeHeadBuffer[2] & 0xff);
        int dataLL = (mPCMPacakgeHeadBuffer[3] & 0xff);

        dataSize =
                ((dataHH << 24) & 0xff000000) | ((dataHL << 16) & 0x00ff0000) | ((dataLH << 8) & 0x0000ff00)
                        | ((dataLL) & 0x000000ff);

        return dataSize;
    }

    // [4-7]:time stamp
    // BigEnd
    public int getPCMPackageHeadTimeStamp() {
        int timeStamp;
        int dataHH = (mPCMPacakgeHeadBuffer[4] & 0xff);
        int dataHL = (mPCMPacakgeHeadBuffer[5] & 0xff);
        int dataLH = (mPCMPacakgeHeadBuffer[6] & 0xff);
        int dataLL = (mPCMPacakgeHeadBuffer[7] & 0xff);

        timeStamp =
                ((dataHH << 24) & 0xff000000) | ((dataHL << 16) & 0x00ff0000) | ((dataLH << 8) & 0x0000ff00)
                        | ((dataLL) & 0x000000ff);

        return timeStamp;
    }

    // [8-11]:service type
    // BigEnd
    public PCMPlayerUtils.EPCMPackageType getPCMPackageHeadType() {
        int type;
        int dataHH = (mPCMPacakgeHeadBuffer[8] & 0xff);
        int dataHL = (mPCMPacakgeHeadBuffer[9] & 0xff);
        int dataLH = (mPCMPacakgeHeadBuffer[10] & 0xff);
        int dataLL = (mPCMPacakgeHeadBuffer[11] & 0xff);

        type =
                ((dataHH << 24) & 0xff000000) | ((dataHL << 16) & 0x00ff0000) | ((dataLH << 8) & 0x0000ff00)
                        | ((dataLL) & 0x000000ff);

        switch (type) {
            case CommonParams.MSG_MEDIA_STOP:
                LogUtil.d(TAG, "get mTypeMusicStop head");
                return PCMPlayerUtils.EPCMPackageType.MUSIC_STOP;

            case CommonParams.MSG_MEDIA_INIT:
                LogUtil.d(TAG, "get mTypeMusicInitial head");
                return PCMPlayerUtils.EPCMPackageType.MUSIC_INITIAL;

            case CommonParams.MSG_MEDIA_PAUSE:
                LogUtil.d(TAG, "get mTypeMusicPause head");
                return PCMPlayerUtils.EPCMPackageType.MUSIC_PAUSE;

            case CommonParams.MSG_MEDIA_RESUME_PLAY:
                LogUtil.d(TAG, "get mTypeMusicResumePlay head");
                return PCMPlayerUtils.EPCMPackageType.MUSIC_RESUME_PLAY;

            case CommonParams.MSG_MEDIA_DATA:
                return PCMPlayerUtils.EPCMPackageType.MUSIC_NORMAL_DATA;

            case CommonParams.MSG_MEDIA_SEEK_TO:
                LogUtil.d(TAG, "get mTypeMusicSeekTo head");
                return PCMPlayerUtils.EPCMPackageType.MUSIC_SEEK_TO;

            case CommonParams.MSG_NAVI_TTS_END:
                LogUtil.d(TAG, "get mTypeTTSStop head");
                return PCMPlayerUtils.EPCMPackageType.TTS_STOP;

            case CommonParams.MSG_NAVI_TTS_INIT:
                LogUtil.d(TAG, "get mTypeTTSInitial head");
                return PCMPlayerUtils.EPCMPackageType.TTS_INITIAL;

            case CommonParams.MSG_NAVI_TTS_DATA:
                // LogUtil.d(TAG, "get mTypeTTSNormalData head");
                return PCMPlayerUtils.EPCMPackageType.TTS_NORMAL_DATA;

            case CommonParams.MSG_VR_AUDIO_INIT:
                return PCMPlayerUtils.EPCMPackageType.VR_INITIAL;

            case CommonParams.MSG_VR_AUDIO_DATA:
                return PCMPlayerUtils.EPCMPackageType.VR_NORMAL_DATA;

            case CommonParams.MSG_VR_AUDIO_STOP:
                return PCMPlayerUtils.EPCMPackageType.VR_STOP;

            case CommonParams.MSG_VR_AUDIO_INTERRUPT:
                return PCMPlayerUtils.EPCMPackageType.VR_INTERRUPT;

            default:
                LogUtil.d(TAG, "get invalid head");
                return PCMPlayerUtils.EPCMPackageType.INVALID_TYPE;
        }
    }

    public void getMusicAudioTrackInitParameter() {
        CarlifeMusicInit audioTrackInitParameter = null;

        byte[] recData = mMusicAudioTrackInitParameterData;
        int recLen = getPCMPackageHeadDataSize();

        // Decryption processing
        if (EncryptSetupManager.getInstance().isEncryptEnable() && recLen > 0) {
            recData = mAESManager.decrypt(mMusicAudioTrackInitParameterData, recLen);
            if (recData == null) {
                LogUtil.e(TAG, "decrypt failed!");
                return;
            }
            recLen = recData.length;
        }

        byte[] pdData = new byte[recLen];
        System.arraycopy(recData, 0, pdData, 0, pdData.length);

        try {
            audioTrackInitParameter = audioTrackInitParameter.parseFrom(pdData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (audioTrackInitParameter != null) {
            mMusicSampleRate = audioTrackInitParameter.getSampleRate();
            mMusicChannleCondfig = audioTrackInitParameter.getChannelConfig();
            mMusicSampleFormate = audioTrackInitParameter.getSampleFormat();
        } else {
            mMusicSampleRate = 44100;
            mMusicChannleCondfig = 2;
            mMusicSampleFormate = 16;
        }
    }

    public int getMusicAudioTrackSampleRate() {
        return mMusicSampleRate;
    }

    public int getMusicAudioTrackChannelConfig() {
        return mMusicChannleCondfig;
    }

    public int getMusicAudioTrackSampleFormat() {
        return mMusicSampleFormate;
    }

    public void getTTSAudioTrackInitParameter() {
        CarlifeTTSInit audioTrackInitParameter = null;

        byte[] recData = mTTSAudioTrackInitParameterData;
        int recLen = getPCMPackageHeadDataSize();

        // Decryption processing
        if (EncryptSetupManager.getInstance().isEncryptEnable() && recLen > 0) {
            recData = mAESManager.decrypt(mTTSAudioTrackInitParameterData, recLen);
            if (recData == null) {
                LogUtil.e(TAG, "decrypt failed!");
                return;
            }
            recLen = recData.length;
        }

        byte[] pdData = new byte[recLen];
        System.arraycopy(recData, 0, pdData, 0, pdData.length);

        try {
            audioTrackInitParameter = audioTrackInitParameter.parseFrom(pdData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (audioTrackInitParameter != null) {
            mTTSSampleRate = audioTrackInitParameter.getSampleRate();
            mTTSChannleCondfig = audioTrackInitParameter.getChannelConfig();
            mTTSSampleFormate = audioTrackInitParameter.getSampleFormat();
        } else {
            mTTSSampleRate = 16000;
            mTTSChannleCondfig = 1;
            mTTSSampleFormate = 16;
        }
    }

    public int getTTSAudioTrackSampleRate() {
        return mTTSSampleRate;
    }

    public int getTTSAudioTrackChannelConfig() {
        return mTTSChannleCondfig;
    }

    public int getTTSAudioTrackSampleFormat() {
        return mTTSSampleFormate;
    }
}
