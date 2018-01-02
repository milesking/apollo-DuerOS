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

import com.baidu.carlifevehicle.util.CarlifeConfUtil;

/**
 * module functions:
 * <p>
 * 1.common macro define, variable, method for audio module;
 * 
 * @author Liu Caiquan
 * 
 * 
 */
public class PCMPlayerUtils {

    public static enum EPCMPackageType {
        MUSIC_STOP, MUSIC_INITIAL, MUSIC_PAUSE, MUSIC_RESUME_PLAY, MUSIC_NORMAL_DATA, MUSIC_SEEK_TO, TTS_STOP,
        TTS_INITIAL, TTS_NORMAL_DATA, VR_INITIAL, VR_STOP, VR_NORMAL_DATA, INVALID_TYPE, PHONE_START, PHONE_STOP,
        AMP_PLAYER_RELEASE, VR_INTERRUPT,
    };

    public static enum EMusicStatus {
        MUSIC_STOP, MUSIC_INITIAL, MUSIC_PAUSE, MUSIC_RESUME_PLAY, MUSIC_NORMAL_DATA, MUSIC_SEEK_TO, MUSIC_PENDING,
        MUSIC_WORKING, INVALID_STATUS
    };

    public static enum ETTSStatus {
        TTS_STOP, TTS_INITIAL, TTS_NORMAL_DATA, INVALID_STATUS
    };

    public static enum EAMPStatus {
        MUSIC_USED, TTS_USED, PHONE_USED, INVALID_STATUS
    }

    public static enum EAudioTrackFocus {
        AUDIO_TRACK_FOCUS_MUSIC_REQUEST, AUDIO_TRACK_FOCUS_MUSIC_RELEASE, AUDIO_TRACK_FOCUS_TTS_REQUEST,
        AUDIO_TRACK_FOCUS_TTS_RELEASE, AUDIO_TRACK_INVALID
    }

    public static enum AudioTransmissionMode {
        INDEPENDENT_CHANNEL_MODE(0), BLUE_TOOTH_MODE(1);

        private int tMode;

        private AudioTransmissionMode(int mode) {
            tMode = mode;
        }

        public int getAudioTransmissionMode() {
            return tMode;
        }
    }

    public static boolean isBTTransmissionMode() {
        if (CarlifeConfUtil.VALUE_INT_AUDIO_TRANSMISSION_MODE ==
                PCMPlayerUtils.AudioTransmissionMode.INDEPENDENT_CHANNEL_MODE
                .getAudioTransmissionMode()) {
            return false;
        } else {
            return true;
        }
    }

    public static final int MUSIC_QUEUE_HORIZONTAL_SIZE = 1024 * 10;
    //
    public static final int TTS_QUEUE_HORIZONTAL_SIZE = 1024 * 10;

    public static final int MUSIC_QUEUE_SIZE = 500;
    public static final int TTS_QUEUE_SIZE = 500;

    public static final int MUSIC_AUDIO_TRACK_INIT_PARAMETER_DATA_SIZE = 4 * 3 * 10;
    public static final int TTS_AUDIO_TRACK_INIT_PARAMETER_DATA_SIZE = 4 * 3 * 10;

    public static final int TTS_TYPE_NAVI = 0;
    public static final int TTS_TYPE_VR = 1;

    // 1000ms
    public static final int MEDIA_AUDIO_TRACK_BUFFER_SIZE = 1024 * 200;

    // 500ms
    public static final int TTS_AUDIO_TRACK_BUFFER_SIZE = 1024 * 16;

    // public static final boolean VR_AUDIO_TRACK_FOCUS_SWITCH = true;

    public static final String AUDIO_MODULE_PREFIX = "Audio-";
    public static final String AUDIO_MODULE_FATAL = "Fatal";

    public static final int MAX_RCEV_MEDIA_DATA_LEN = 100 * 1024;
}
