/******************************************************************************
 * Copyright 2018 The Baidu Authors. All Rights Reserved.
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
package com.baidu.che.codriverlauncher.util;

import com.baidu.che.codriversdk.InitListener;
import com.baidu.che.codriversdk.manager.CdAsrManager;
import com.baidu.che.codriversdk.manager.CdBlueToothManager;
import com.baidu.che.codriversdk.manager.CdPhoneManager;
import com.baidu.che.codriversdk.manager.CdSystemManager;

import android.content.Context;
import android.content.Intent;

/**
 * callback of DuerOS
 */

public class DuerOSRunnable implements InitListener {

    private static final String TAG = "DuerOSRunnable";
    private static final String VOICE_STATUS_ACTION_FOR_FLYAUDIO = "flyaudio.intent.action.VOICE_STATUS";

    private CdPhoneManager.PhoneContactList mPhoneModel;
    private Context mContext;

    public DuerOSRunnable(Context context) {
        mContext = context;
        initData();
    }

    private void initData() {
        mPhoneModel = new CdPhoneManager.PhoneContactList();
    }

    @Override
    public void onConnectedToRemote() {
        initBlueToothTool();
        initAsrTool();
        CdAsrManager.getInstance().openFullBargin();
        CdSystemManager.getInstance().setSystemTool(mSystemTool);
    }

    private void initAsrTool() {
        CdAsrManager.getInstance().setAsrTool(new CdAsrManager.AsrTool() {
            @Override
            public void onVrDialogShow() {
                LogUtil.d(TAG, "onVrDialogShow");
                Intent intent = new Intent();
                intent.setAction(VOICE_STATUS_ACTION_FOR_FLYAUDIO);
                intent.putExtra("VOICE_ACTION", "enter_voice");
                mContext.sendBroadcast(intent);
            }

            @Override
            public void onVrDialogDismiss() {
                LogUtil.d(TAG, "onVrDialogDismiss");
                Intent intent = new Intent();
                intent.setAction(VOICE_STATUS_ACTION_FOR_FLYAUDIO);
                intent.putExtra("VOICE_ACTION", "exit_voice");
                mContext.sendBroadcast(intent);
            }

        });
    }

    private void initBlueToothTool() {
        CdBlueToothManager.getInstance()
                .onNotifyBTPhoneStatus(CdBlueToothManager.BTPhoneStatus.BT_PHONE_CANNOT_AUTHORIZED);
        CdBlueToothManager.getInstance().setBlueToothTool(new CdBlueToothManager.BlueToothTool() {
            @Override
            public void openBlueToothView() {
            }

            @Override
            public void openContractDownloadView() {
            }
        });
        CdPhoneManager.getInstance().setPhoneTool(new CdPhoneManager.PhoneTool() {
            @Override
            public void dialNum(String number) {
            }
        });
    }

    @Override
    public void onDisconnectedToRemote() {
        LogUtil.d(TAG, "onDisconnectedToRemote:");
    }

    private CdSystemManager.SystemTool mSystemTool = new CdSystemManager.SystemTool() {
        @Override
        public void closeFeature(String s) {

        }

        @Override
        public void openFeature(String s) {

        }

        @Override
        public void increaseFeature(String s) {

        }

        @Override
        public void reduceFeature(String s) {

        }

        @Override
        public void maxFeature(String s) {

        }

        @Override
        public void minFeature(String s) {

        }

        @Override
        public void operateFeature(String s, String s1) {
        }

    };

}
