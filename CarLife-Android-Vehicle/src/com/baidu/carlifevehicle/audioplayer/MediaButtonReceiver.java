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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.touch.TouchListenerManager;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * function:
 * <p>
 * hardkey listener for Previous song/next song
 * 
 * @author liucaiquan
 * 
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + MediaButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (keyEvent == null) {
                return;
            }

            int action = keyEvent.getAction();
            int keyCode = keyEvent.getKeyCode();
            long eventTime = keyEvent.getEventTime();

            LogUtil.d(TAG, "carlife: onReceive is triggered!");

            if (action == KeyEvent.ACTION_UP) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        LogUtil.d(TAG, "KEYCODE_MEDIA_PREVIOUS");
                        TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_SUB);
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        LogUtil.d(TAG, "KEYCODE_MEDIA_NEXT");
                        TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_ADD);
                        break;
                    default:
                        break;
                }
            }

            if (isOrderedBroadcast()) {
                abortBroadcast();
            }
        }
    }
}
