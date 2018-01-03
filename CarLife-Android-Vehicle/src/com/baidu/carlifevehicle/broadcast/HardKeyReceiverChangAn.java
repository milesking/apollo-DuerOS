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
package com.baidu.carlifevehicle.broadcast;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.touch.TouchListenerManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HardKeyReceiverChangAn extends BroadcastReceiver {
    private boolean mIsLongPress = false;
    private boolean isHoldingAudioFocus = false;

    public void setHoldingAudioFocus(boolean isHoldingAudioFocus) {
        this.isHoldingAudioFocus = isHoldingAudioFocus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isHoldingAudioFocus) {
            return;
        }
        int mKeyCode = intent.getIntExtra("android.intent.extra.c3_hardkey_keycode", -1);
        int mKeyEvent = intent.getIntExtra("android.intent.extra.c3_hardkey_action", -1);
        // Seek+ pressed
        if ((mKeyCode == 243 || mKeyCode == 245)) {
            if (mKeyEvent == 0 && !mIsLongPress) {
                mIsLongPress = true;
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_ADD);
            } else if (mKeyEvent == 1) {
                mIsLongPress = false;
            }
        } else if ((mKeyCode == 244 || mKeyCode == 246)) {
            // Seek- pressed
            if (mKeyEvent == 0 && !mIsLongPress) {
                mIsLongPress = true;
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_SUB);
            } else if (mKeyEvent == 1) {
                mIsLongPress = false;
            }
        }
    }
}