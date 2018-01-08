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
package com.baidu.che.codriverlauncher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.che.codriverlauncher.LauncherApp;
import com.baidu.che.codriverlauncher.model.AppInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * app info
 */
public class AppInfoProvider {
    private static final String APP_NO_FILTER = "no_package_filter";
    private PackageManager mPackageManager;
    private Context mContext;
    List<AppInfo> mlistAppInfoSystem = new ArrayList<AppInfo>();
    List<AppInfo> mlistAppInfoThrid = new ArrayList<AppInfo>();

    public AppInfoProvider(Context context) {
        this.mPackageManager = context.getPackageManager();
        this.mContext = context;

    }

    public List<AppInfo> queryAppInfo() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = mPackageManager
                .queryIntentActivities(mainIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        // sort by name which is system default
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));

        List<AppInfo> mlistAppInfo = new ArrayList<AppInfo>();
        if (mlistAppInfo != null) {
            mlistAppInfo.clear();
            mlistAppInfoSystem.clear();
            mlistAppInfoThrid.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                AppInfo info = getSingleInfo(reInfo, APP_NO_FILTER);
                if (info != null) {
                    if (LauncherApp.getInstance().getPackageName().equals(info.getPackageName())) {
                        continue;
                    }
                    if (info.isSystem()) {
                        mlistAppInfoSystem.add(info);
                    } else {
                        mlistAppInfoThrid.add(info);
                    }
                }
            }
            // mlistAppInfo.add(appInfo);
        }
        mlistAppInfo.addAll(mlistAppInfoSystem);
        mlistAppInfo.addAll(mlistAppInfoThrid);
        return mlistAppInfo;
    }

    public AppInfo getSingleInfo(ResolveInfo reInfo, String packageFilter) {
        String pkgName = reInfo.activityInfo.packageName; // Get the package name of the application
        AppInfo appInfo = null;
        if (TextUtils.equals(pkgName, packageFilter) || TextUtils.equals(APP_NO_FILTER, packageFilter)) {
            String activityName = reInfo.activityInfo.name; // Get the name of the startup Activity for the application
            String appLabel = (String) reInfo.loadLabel(mPackageManager); // Get the application Label
            Drawable icon = reInfo.loadIcon(mPackageManager); // Get the application icon
            // Preparing Intent for application startup Activity
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName,
                    activityName));

            // create an AppInfo
            appInfo = new AppInfo();
            appInfo.setPackageName(pkgName);
            appInfo.setAppName(appLabel);
            appInfo.setIcon(icon);
            appInfo.setActivityName(activityName);
            appInfo.setIntent(launchIntent);

            try {
                PackageInfo mPackageInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    // third apps
                    appInfo.setSystem(false);
                } else {
                    // system apps
                    appInfo.setSystem(true);
                }
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.e("AppInfoProvider", e.getMessage().toString());
            }
        }
        return appInfo;
    }

    public boolean isAppInstalled(Context context, String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = mPackageManager.getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }
}
