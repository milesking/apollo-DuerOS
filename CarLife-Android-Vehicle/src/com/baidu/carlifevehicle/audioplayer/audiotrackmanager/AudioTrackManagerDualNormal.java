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

import android.annotation.SuppressLint;
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
import com.baidu.carlifevehicle.audioplayer.VehicleFactoryAdapter;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.model.ModuleStatusModel;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * module functions:
 * <p>
 * 1.audio track management;
 * <p>
 * 2.audio focus management;
 * 
 * @author liucaiquan
 */
public class AudioTrackManagerDualNormal implements AudioTrackManagerDualInterface {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX
            + AudioTrackManagerDualNormal.class.getSimpleName();
    private static final String FTAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + PCMPlayerUtils.AUDIO_MODULE_FATAL;

    private AudioTrack mMusicAudioTrack;
    private AudioTrack mTTSAudioTrack;

    private int mMusicSampleRate;
    private int mMusicChannelConfig;
    private int mMusicSampleFormat;

    private int mTTSSampleRate;
    private int mTTSChannelConfig;
    private int mTTSSampleFormat;

    private AudioManager mAM;
    // adjustable
    private final float mMusicAudioTrackVolumReduceRatio = 3;

    private static AudioTrackManagerDualNormal mInstance;

    private ModeService mModeService;

    private boolean mIsMediaOwnAudioFocus = false;
    private Object mMediaAudioFocusLock = new Object();
    private boolean mIsTTSOwnAudioFocus = false;
    private Object mTTSAudioFocusLock = new Object();

    private int mTTSType;

    private int mMediaPlayStatus = AudioTrack.PLAYSTATE_PAUSED;
    private int mTTSPlayStatus = AudioTrack.PLAYSTATE_PAUSED;

    private MsgChangeHandler mHandler;
    private ComponentName mMediaButtonReceiverComponent;

    private int mPreMediaSampleRate;
    private int mPreMediaChannelConfig;
    private int mPreMediaFormate;
    private int mPreMinBuffSize;

    private AudioTrackManagerDualNormal() {
        mAM = (AudioManager) BaseActivity.mContext.getSystemService(Context.AUDIO_SERVICE);
        mModeService = ModeService.getInstance();

        mMediaButtonReceiverComponent =
                new ComponentName("com.baidu.carlifevehicle", MediaButtonReceiver.class.getName());

        mHandler = new MsgChangeHandler();
        MsgHandlerCenter.registerMessageHandler(mHandler);
    }

    public static AudioTrackManagerDualInterface getInstance() {
        if (mInstance == null) {
            mInstance = new AudioTrackManagerDualNormal();
        }

        return mInstance;
    }

    @Override
    public void initMusicAudioTrack(int sampleRate, int channelConfig, int sampleFormat) {
        int tChannelConfig;
        int tSampleFormat;
        int audioMinBufSizeLocal;

        // request Audio Focus
        if (getMusicAudioTrackFocus() == 0) {
            setMediaAudioFocusStatus(true);
        } else {
            setMediaAudioFocusStatus(false);
        }

        mMusicSampleRate = sampleRate;
        mMusicChannelConfig = channelConfig;
        mMusicSampleFormat = sampleFormat;

        releaseMusicAudioTrack();

        // avoid crush
        if (mMusicSampleRate <= 0) {
            mMusicSampleRate = 44100;
        }

        if (mMusicChannelConfig == 2) {
            tChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
        } else if (mMusicChannelConfig == 1) {
            tChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
        } else {
            tChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
        }

        if (sampleFormat == 8) {
            tSampleFormat = AudioFormat.ENCODING_PCM_8BIT;
        } else {
            tSampleFormat = AudioFormat.ENCODING_PCM_16BIT;
        }

        audioMinBufSizeLocal = AudioTrack.getMinBufferSize(mMusicSampleRate, tChannelConfig, tSampleFormat);

        LogUtil.d(TAG, "audioMinBufSizeLocal= " + audioMinBufSizeLocal);

        mPreMediaSampleRate = mMusicSampleRate;
        mPreMediaChannelConfig = tChannelConfig;
        mPreMediaFormate = tSampleFormat;
        mPreMinBuffSize = audioMinBufSizeLocal;

        try {
            mMusicAudioTrack =
                    new AudioTrack(AudioManager.STREAM_MUSIC, mMusicSampleRate, tChannelConfig, tSampleFormat,
                            audioMinBufSizeLocal, AudioTrack.MODE_STREAM);
        } catch (IllegalArgumentException e) {
            mMusicAudioTrack = null;
            informMusicPause();
            e.printStackTrace();
        }

        if (mMusicAudioTrack != null && mMusicAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            try {
                mMusicAudioTrack.play();
            } catch (IllegalStateException e) {
                LogUtil.e(FTAG, "media audio track init failed!");
                mMusicAudioTrack = null;
                informMusicPause();
                e.printStackTrace();
            }
        }
    }

    private void reInitAudioTrack() {
        try {
            mMusicAudioTrack =
                    new AudioTrack(AudioManager.STREAM_MUSIC, mPreMediaSampleRate, mPreMediaChannelConfig,
                            mPreMediaFormate, mPreMinBuffSize, AudioTrack.MODE_STREAM);

        } catch (IllegalArgumentException e) {
            informMusicPause();
            mMusicAudioTrack = null;
            e.printStackTrace();
        }
    }

    @Override
    public void initTTSAudioTrack(int ttsType, int sampleRate, int channelConfig, int sampleFormat) {
        int tChannelConfig;
        int tSampleFormat;
        int audioMinBufSizeLocal;

        // request AudioFocus for TTS
        if (getTTSAudioTrackFocus(ttsType) == 0) {
            setTTSAudioFocusStatus(true);
        } else {
            setTTSAudioFocusStatus(false);
        }

        if (mTTSSampleRate != sampleRate || mTTSChannelConfig != channelConfig || mTTSSampleFormat != sampleFormat
                || mTTSAudioTrack == null) {

            mTTSSampleRate = sampleRate;
            mTTSChannelConfig = channelConfig;
            mTTSSampleFormat = sampleFormat;

            releaseTTSAudioTrack();

            // avoid crush
            if (mTTSSampleRate <= 0) {
                mTTSSampleRate = 16000;
            }

            if (mTTSChannelConfig == 2) {
                tChannelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            } else if (mTTSChannelConfig == 1) {
                tChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
            } else {
                tChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
            }

            if (sampleFormat == 8) {
                tSampleFormat = AudioFormat.ENCODING_PCM_8BIT;
            } else {
                tSampleFormat = AudioFormat.ENCODING_PCM_16BIT;
            }

            audioMinBufSizeLocal = AudioTrack.getMinBufferSize(mTTSSampleRate, tChannelConfig, tSampleFormat);

            LogUtil.d(TAG, "audioMinBufSizeLocal= " + audioMinBufSizeLocal);

            try {
                int ttsAudioTrackType = VehicleFactoryAdapter.getInstance().getTTSAudioTrackStreamType();
                mTTSAudioTrack =
                        new AudioTrack(ttsAudioTrackType, mTTSSampleRate, tChannelConfig, tSampleFormat,
                                audioMinBufSizeLocal, AudioTrack.MODE_STREAM);
            } catch (IllegalArgumentException e) {
                mTTSAudioTrack = null;
                e.printStackTrace();
            }

            if (mTTSAudioTrack != null) {
                try {
                    mTTSAudioTrack.play();
                } catch (IllegalStateException e) {
                    mTTSAudioTrack = null;
                    LogUtil.e(FTAG, "tts audio track init failed!");
                    e.printStackTrace();
                }
            }
        }
    }

    private void releaseMusicAudioTrack() {
        // release Audio Focus
        // releaseMusicAudioTrackFocus();

        if (mMusicAudioTrack == null) {
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
                mMusicAudioTrack.stop();

                mMusicAudioTrack.release();
                mMusicAudioTrack = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseTTSAudioTrack() {
        // setTTSPlayState(AudioTrack.PLAYSTATE_STOPPED);

        if (mTTSAudioTrack == null) {
            return;
        } else {
            try {
                mTTSAudioTrack.stop();

                mTTSAudioTrack.release();
                mTTSAudioTrack = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void pauseMusicAudioTrack() {
        // release Audio Focus
        releaseMusicAudioTrackFocus();

        if ((mMusicAudioTrack != null) && (mMusicAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)) {
            try {
                mMusicAudioTrack.pause();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    // stop TTS
    @Override
    public void pauseTTSAudioTrack() {
        // release TTS AudioFocus
        releaseTTSAudioTrackFocus();
    }

    @Override
    public void resumeMusicAudioTrack() {
        // some times, initMusicAudioTrack() maybe fail
        if (mMusicAudioTrack == null) {
            reInitAudioTrack();
        }

        if ((mMusicAudioTrack != null) && (mMusicAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)) {

            try {
                mMusicAudioTrack.play();
            } catch (IllegalStateException e) {
                LogUtil.e(FTAG, "media resume failed!");
                informMusicPause();
                e.printStackTrace();
            }
        }

        if (getMusicAudioTrackFocus() == 0) {
            setMediaAudioFocusStatus(true);
        } else {
            setMediaAudioFocusStatus(false);
        }
    }

    @Override
    public void writeMusicAudioTrack(byte[] data, int offset, int size) {
        if (mMusicAudioTrack != null && mMusicAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            // can not catch exception
            if (getMediaAudioFocusStatus()) {
                mMusicAudioTrack.write(data, offset, size);
            }
        }
    }

    @Override
    public void writeTTSAudioTrack(byte[] data, int offset, int size) {
        /*
         * some times, even set AudioTrack.play(), but AudioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING
         */
        if (mTTSAudioTrack != null && mTTSAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            // can not catch exception
            if (getTTSAudioFocusStatus()) {
                if (size > 0) {
                    mTTSAudioTrack.write(data, offset, size);
                }
            }
        }
    }

    private void reduceMusicAudioTrackVolum() {
        if (mMusicAudioTrack != null) {
            float maxVolume = mMusicAudioTrack.getMaxVolume();
            setVolume(maxVolume / mMusicAudioTrackVolumReduceRatio);
        }
    }

    private void muteMusicAudioTrackVolum() {
        if (mMusicAudioTrack != null) {
            float minVolume = mMusicAudioTrack.getMinVolume();
            setVolume(minVolume);
        }
    }

    private void resumeMusicAudioTrackVolum() {
        if (mMusicAudioTrack != null) {
            float maxVolume = mMusicAudioTrack.getMaxVolume();
            setVolume(maxVolume);
        }
    }

    @SuppressLint("NewApi")
    private void setVolume(float vol) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mMusicAudioTrack.setVolume(vol);
        } else {
            mMusicAudioTrack.setStereoVolume(vol, vol);
        }
    }

    /**
     * 
     * @return 0:success; 1:failed;
     */
    private int getMusicAudioTrackFocus() {
        int result = mAM.requestAudioFocus(mMusicAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // register button listener
            mAM.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
            LogUtil.d(TAG, "music audio track get successfully!");

            return 0;
        } else {
            informMusicPause();
            LogUtil.d(TAG, "music audio track get failed!");

            return -1;
        }
    }

    private int getTTSAudioTrackFocus(int ttsType) {
        setTTSType(ttsType);
        if (ttsType == PCMPlayerUtils.TTS_TYPE_VR) {
            return 0;
        }

        int audioFocusType;

        audioFocusType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;

        int ttsAudioTrackType = VehicleFactoryAdapter.getInstance().getTTSAudioTrackStreamType();
        int result = mAM.requestAudioFocus(mTTSAudioFocusListener, ttsAudioTrackType, audioFocusType);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LogUtil.d(TAG, "tts audio track get successfully!");

            return 0;
        } else {
            LogUtil.d(TAG, "tts audio track get failed!");

            return -1;
        }

    }

    private int releaseMusicAudioTrackFocus() {
        // normally, it is not neccessary to release AudioFocus
        setMediaAudioFocusStatus(false);
        return 0;
    }

    private int releaseTTSAudioTrackFocus() {
        /**
         * TTS audio focus is just set by other application grasp;
         */

        if (getTTSType() == PCMPlayerUtils.TTS_TYPE_VR) {
            return 0;
        }

        mAM.abandonAudioFocus(mTTSAudioFocusListener);

        return 0;
    }

    private OnAudioFocusChangeListener mMusicAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            boolean isPause = mModeService.getMode(focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAM.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

                    setMediaAudioFocusStatus(false);
                    // audio focus loss caused by other music start playing,we need to pause carlife music 
                    informMusicPause();
                    LogUtil.d(TAG, "music AUDIOFOCUS_LOSS");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mAM.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

                    setMediaAudioFocusStatus(false);
                    // audio focus loss by VR or telephone, we need to pause carlife music
                    // and afterwards we need resume playing when audio focus gained
                    if (isPause) {
                        informMusicPause();
                    }
                    LogUtil.d(TAG, "music AUDIOFOCUS_LOSS_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    reduceMusicAudioTrackVolum();
                    LogUtil.d(TAG, "music AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mAM.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

                    setMediaAudioFocusStatus(true);

                    resumeMusicAudioTrackVolum();
                    // if audio focus loss caused by VR or telephone we need to resume music
                    if (!isPause) {
                        informMusicResume();
                    }
                    LogUtil.d(TAG, "music AUDIOFOCUS_GAIN");
                    break;

                default:
                    break;
            }
        }
    };

    private OnAudioFocusChangeListener mTTSAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    LogUtil.d(TAG, "tts AUDIOFOCUS_LOSS");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // setTTSAudioFocusStatus(false);
                    /**
                     * step 1: CarLife Navi TTS play;
                     * <p>
                     * step 2: CarLife VR play;
                     * <p>
                     * CarLife Navi TTS will not send MSG_NAVI_TTS_STOP message,
                     * <p>
                     * but navi TTS audio focus needs to be abandoned;
                     */
                    mAM.abandonAudioFocus(mTTSAudioFocusListener);
                    LogUtil.d(TAG, "tts AUDIOFOCUS_LOSS_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    LogUtil.d(TAG, "tts AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    LogUtil.d(TAG, "tts AUDIOFOCUS_GAIN");
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

    private void setMediaAudioFocusStatus(boolean isMediaOwnAudioFocus) {
        synchronized (mMediaAudioFocusLock) {
            mIsMediaOwnAudioFocus = isMediaOwnAudioFocus;
        }
    }

    private boolean getMediaAudioFocusStatus() {
        synchronized (mMediaAudioFocusLock) {
            return mIsMediaOwnAudioFocus;
        }
    }

    private void setTTSAudioFocusStatus(boolean isTTSOwnAudioFocus) {
        synchronized (mTTSAudioFocusLock) {
            mIsTTSOwnAudioFocus = isTTSOwnAudioFocus;
        }
    }

    private boolean getTTSAudioFocusStatus() {
        synchronized (mTTSAudioFocusLock) {
            return mIsTTSOwnAudioFocus;
        }
    }

    private void setTTSType(int type) {
        mTTSType = type;
    }

    private int getTTSType() {
        return mTTSType;
    }

    private class MsgChangeHandler extends MsgBaseHandler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonParams.MSG_CONNECT_STATUS_DISCONNECTED:
                    // avoid tts occupy Audio focus which can not be grabbed by the App
                    releaseTTSAudioTrackFocus();
                    mAM.abandonAudioFocus(mVRAudioFocusListener);
                    break;

                default:
                    break;
            }
        }

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
        }
    }

    private OnAudioFocusChangeListener mVRAudioFocusListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    LogUtil.d(TAG, "VR AUDIOFOCUS_LOSS");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    LogUtil.d(TAG, "VR AUDIOFOCUS_LOSS_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    LogUtil.d(TAG, "VR AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    LogUtil.d(TAG, "VR AUDIOFOCUS_GAIN");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int requestVRAudioFocus() {
        LogUtil.d(TAG, "request VR audio focus!");

        if (mAM != null) {
            // abandon Navi TTS audio focus
            mAM.abandonAudioFocus(mTTSAudioFocusListener);
            return mAM.requestAudioFocus(mVRAudioFocusListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
    }

    public int abandonVRAudioFocus() {
        LogUtil.d(TAG, "abonden VR audio focus!");

        if (mAM != null) {
            return mAM.abandonAudioFocus(mVRAudioFocusListener);
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
    }
}
