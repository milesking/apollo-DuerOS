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

/**
 * used for permanent data
 * 
 * @author liucaiquan
 * 
 */
public class RomStorage {
    private static RomStorage mInstance;
    private boolean mAudioFocusStatus = false;

    public static RomStorage getInstance() {
        if (mInstance == null) {

            mInstance = new RomStorage();
        }
        return mInstance;
    }

    public void setAudioFocusStatus(boolean status) {
        mAudioFocusStatus = status;
    }

    public boolean getAudioFocusStatus() {
        return mAudioFocusStatus;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
