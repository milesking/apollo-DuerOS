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
package com.baidu.che.codriverlauncher.core;

import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriversdk.InitListener;
import com.baidu.che.codriversdk.manager.CdConfigManager;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

/**
 * init some config when app start
 */

public abstract class CoreApplication extends Application {

    private static CoreApplication mInstance = null;

    public static CoreApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        String curProcessName = getCurProcessName(this);
        String packageName = getPackageName();
        if (curProcessName != null && !curProcessName.equals(packageName)) {
            return;
        }
        init();
    }

    private void init() {
        LauncherUtil.init(getApplicationContext());
        initCoDriverSdk();
    }

    private void initCoDriverSdk() {
        CdConfigManager.getInstance().initialize(this, new InitListener() {
            @Override
            public void onConnectedToRemote() {

            }

            @Override
            public void onDisconnectedToRemote() {

            }
        });
    }

    private String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess != null && appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
