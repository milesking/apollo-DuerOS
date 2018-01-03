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

import android.media.AudioManager;
import android.os.Message;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;

/**
 * media play status management on vehicle side
 * 
 * @author liucaiquan
 * 
 */
public class ModeService {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + ModeService.class.getSimpleName();
    private static ModeService mInstance;

    // Music is paused on account of user's proactive behavior on mobile device 
    private boolean mIsUserPause = true;
    // Music is paused due to some reasons arise on HU side like audio gain loss 
    // If HU lead to music paused and meanwhile it is responsible to resume music playing afterwards
    private boolean mIsMachinePause = false;

    private boolean mIsVRWorking = false;

    private Object mUserLock = new Object();
    private Object mMachineLock = new Object();
    private Object mVRStatusLock = new Object();

    private MsgHandler mMsgHandler;

    private ModeService() {
        mMsgHandler = new MsgHandler();
        MsgHandlerCenter.registerMessageHandler(mMsgHandler);
    }

    public static ModeService getInstance() {
        if (mInstance == null) {
            mInstance = new ModeService();
        }

        return mInstance;
    }

    /**
     * 1. called when receiving Mobile media status
     * <p>
     * 2. only change status of mIsUserPause
     * 
     * @param packageType
     */
    public void setMode(PCMPlayerUtils.EPCMPackageType packageType) {
        switch (packageType) {
            case MUSIC_STOP:
                setIsUserPause(true);
                break;
            case MUSIC_PAUSE:
                if (!getIsMachinePause()) {
                    setIsUserPause(true);
                }
                break;

            case MUSIC_INITIAL:
                setIsUserPause(false);
                break;
            case MUSIC_RESUME_PLAY:
                if (getIsUserPause()) {
                    setIsUserPause(false);
                }
                break;

            case VR_INITIAL:
                setIsVRWorking(true);
                break;

            case VR_STOP:
                setIsVRWorking(false);
                break;

            default:
                break;
        }
    }

    /**
     * 1. called when audio focus changed
     * <p>
     * 2. only change status of mIsMachinePause
     * 
     * @param audioFocusStatus
     * 
     * @return <p>
     *         false:can resume/can not pause;
     *         <p>
     *         true:can pause/can not resume;
     */
    
    public boolean getMode(int audioFocusStatus) {
        switch (audioFocusStatus) {
            // On HU side, VR and telephone may lead to audio focus loss transient, in this case HU will
            // actively pause the music playing back by sending Module Control command
            // and after that, HU need resume music playing when audio focus gained
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (getIsVRWorking()) {
                    return false;
                }

                if (!getIsUserPause()) {
                    // Set flag to indicate music paused by HU due to audio focus loss
                    setIsMachinePause(true);
                    return true;
                } else {
                    return false;
                }

            case AudioManager.AUDIOFOCUS_GAIN:
                if (!getIsUserPause()) {
                    if (getIsMachinePause()) {
                        setIsMachinePause(false);
                        return false;
                    }
                } else {
                    return true;
                }

            default:
                break;
        }

        return true;
    }

    private void setIsUserPause(boolean status) {
        synchronized (mUserLock) {
            mIsUserPause = status;
        }
    }

    private boolean getIsUserPause() {
        synchronized (mUserLock) {
            return mIsUserPause;
        }
    }

    private void setIsMachinePause(boolean status) {
        synchronized (mMachineLock) {
            mIsMachinePause = status;
        }
    }

    private boolean getIsMachinePause() {
        synchronized (mMachineLock) {
            return mIsMachinePause;
        }
    }

    private void setIsVRWorking(boolean status) {
        synchronized (mVRStatusLock) {
            mIsVRWorking = status;
        }
    }

    private boolean getIsVRWorking() {
        synchronized (mVRStatusLock) {
            return mIsVRWorking;
        }
    }

    private void resetMode() {
        setIsUserPause(true);
        setIsMachinePause(false);
    }

    private class MsgHandler extends MsgBaseHandler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonParams.MSG_CONNECT_STATUS_DISCONNECTED:
                    resetMode();
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

}
