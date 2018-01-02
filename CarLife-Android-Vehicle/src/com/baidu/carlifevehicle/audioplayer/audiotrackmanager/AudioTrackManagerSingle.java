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
package com.baidu.carlifevehicle.audioplayer.audiotrackmanager;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioTrack;
import android.os.Message;

import com.baidu.carlife.protobuf.CarlifeModuleStatusProto.CarlifeModuleStatus;
import com.baidu.carlifevehicle.BaseActivity;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.audioplayer.MediaButtonReceiver;
import com.baidu.carlifevehicle.audioplayer.ModeService;
import com.baidu.carlifevehicle.audioplayer.PCMPlayerUtils;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.model.ModuleStatusModel;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * Module functions:
 * <p>
 * 1.audio track management;
 * <p>
 * 2.audio focus management.
 * 
 * @author liucaiquan
 */
public class AudioTrackManagerSingle {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX
            + AudioTrackManagerSingle.class.getSimpleName();

    private AudioTrack mAudioTrack;

    private int mSampleRate;
    private int mChannelConfig;
    private int mSampleFormat;

    private AudioManager mAM;

    public static enum EAudioTrackType {
        MEDIA, TTS
    }

    private static AudioTrackManagerSingle mInstance;

    private ModeService mModeService;

    private AudioTrackManagerSingle() {
        mAM = (AudioManager) BaseActivity.mContext.getSystemService(Context.AUDIO_SERVICE);
        mModeService = ModeService.getInstance();

        ComponentName mMediaButtonReceiverComponent;
        mMediaButtonReceiverComponent =
                new ComponentName("com.baidu.carlifevehicle", MediaButtonReceiver.class.getName());
        mAM.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
    }

    public static AudioTrackManagerSingle getInstance() {
        if (mInstance == null) {
            mInstance = new AudioTrackManagerSingle();
        }

        return mInstance;
    }

    public void initAudioTrack(int sampleRate, int channelConfig, int sampleFormat, EAudioTrackType type) {
        int tChannelConfig;
        int tSampleFormat;
        int audioMinBufSizeLocal;

        if (mAudioTrack != null) {
            mAudioTrack.pause();
            mAudioTrack.flush();
        }

        // get audio track focus
        if (EAudioTrackType.MEDIA == type) {
            getAudioTrackFocus();
        }

        if (mSampleRate != sampleRate || mChannelConfig != channelConfig || mSampleFormat != sampleFormat
                || mAudioTrack == null) {

            mSampleRate = sampleRate;
            mChannelConfig = channelConfig;
            mSampleFormat = sampleFormat;

            releaseAudioTrack(type);

            // avoid crush
            if (mSampleRate <= 0) {
                mSampleRate = 16000;
            }

            if (mChannelConfig == 2) {
                tChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            } else if (mChannelConfig == 1) {
                tChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
            } else {
                tChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            }

            if (sampleFormat == 8) {
                tSampleFormat = AudioFormat.ENCODING_PCM_8BIT;
            } else {
                tSampleFormat = AudioFormat.ENCODING_PCM_16BIT;
            }

            audioMinBufSizeLocal = AudioTrack.getMinBufferSize(mSampleRate, tChannelConfig, tSampleFormat);
            // audioMinBufSizeLocal = PCMPlayerUtils.MEDIA_AUDIO_TRACK_BUFFER_SIZE;

            LogUtil.d(TAG, "audioMinBufSizeLocal= " + audioMinBufSizeLocal);

            try {
                mAudioTrack =
                        new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, tChannelConfig, tSampleFormat,
                                audioMinBufSizeLocal, AudioTrack.MODE_STREAM);
            } catch (IllegalArgumentException e) {
                mAudioTrack = null;
                e.printStackTrace();
            }
        }

        if (mAudioTrack != null) {
            mAudioTrack.play();
        }
    }

    public void releaseAudioTrack(EAudioTrackType type) {
        if (mAudioTrack == null) {
            return;
        } else {
            try {
                /**
                 * make sure data in buffer play completely
                 * 
                 * When used on an instance created in MODE_STREAM mode, audio will stop playing after the last buffer
                 * that was written has been played. For an immediate stop, use pause(), followed by flush() to discard
                 * audio data that hasn't been played back yet.
                 */
                mAudioTrack.stop();

                mAudioTrack.release();
                mAudioTrack = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void pauseAudioTrack() {
        if ((mAudioTrack != null) && (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
            try {
                mAudioTrack.pause();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            // release audio track focus
            releaseAudioTrackFocus();
        }
    }

    public void resumeAudioTrack() {
        if ((mAudioTrack != null) && (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)) {
            try {
                mAudioTrack.pause();
                mAudioTrack.flush();
                mAudioTrack.play();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        // get audio track focus
        getAudioTrackFocus();
    }

    public void writeAudioTrack(byte[] data, int offset, int size) {
        if (mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.write(data, offset, size);
        }
    }

    /**
     * 
     * @return 0:success; 1:failed;
     */
    private int getAudioTrackFocus() {
        // Request audio focus for playback
        int result =
                mAM.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LogUtil.d(TAG, "audio track get successfully!");

            return 0;
        } else {
            LogUtil.d(TAG, "audio track get failed!");

            return -1;
        }
    }

    private int releaseAudioTrackFocus() {
        return 0;
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            boolean isPause = mModeService.getMode(focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    informMusicPause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPause) {
                        informMusicPause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!isPause) {
                        informMusicResume();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void sendCommandToMd(int moduleId, int statusId) {
        CarlifeModuleStatus.Builder moduleStatusBuilder = CarlifeModuleStatus.newBuilder();
        moduleStatusBuilder.setModuleID(moduleId);
        // start
        moduleStatusBuilder.setStatusID(statusId);
        CarlifeModuleStatus carlifeModuleStatus = moduleStatusBuilder.build();
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_MODULE_CONTROL);
        command.setData(carlifeModuleStatus.toByteArray());
        command.setLength(carlifeModuleStatus.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    private void informMusicPause() {
        sendCommandToMd(ModuleStatusModel.CARLIFE_MUSIC_MODULE_ID, ModuleStatusModel.MUSIC_STATUS_IDLE);
    }

    private void informMusicResume() {
        sendCommandToMd(ModuleStatusModel.CARLIFE_MUSIC_MODULE_ID, ModuleStatusModel.MUSIC_STATUS_RUNNING);
    }
}
