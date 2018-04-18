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
package com.baidu.che.codriverlauncher;

import com.baidu.che.codriverlauncher.core.CoreApplication;
import com.baidu.che.codriverlauncher.mapsdk.MapSdkWrapper;
import com.baidu.che.codriverlauncher.util.DuerOSRunnable;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriversdk.manager.CdConfigManager;
import com.baidu.che.codriversdk.manager.CdNaviManager;

import android.app.ActivityManager;
import android.content.Context;

/**
 * init some config when app start
 */

public class LauncherApp extends CoreApplication {

    private static LauncherApp mInstance = null;
    private DuerOSRunnable mInitListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        String curProcessName = getCurProcessName(this);
        String packageName = getPackageName();

        if (curProcessName != null && !curProcessName.equals(packageName)) {
            return;
        }

        mInitListener = new DuerOSRunnable(this);
        CdConfigManager.getInstance().initialize(this, mInitListener);
        LauncherUtil.init(getApplicationContext());
        // init location module
        MapSdkWrapper.init(this);
        // Set the baidu map as the default navigation app, so that switch mode for day and night
        CdNaviManager.getInstance().setDefaultNaviApp(CdNaviManager.NaviApp.Baidu);
    }

    public String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();

            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {

                    if (appProcess != null && appProcess.pid == pid) {
                        return appProcess.processName;
                    }

                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static LauncherApp getInstance() {
        return mInstance;
    }
}
