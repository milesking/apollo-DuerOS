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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.model.AppInfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

/**
 * Get the related information of the application
 */

public class AppInfoProvider {
    private static final String TAG = "AppInfoProvider";

    private PackageManager mPackageManager;
    private Context mContext;
    private List<AppInfo> mListAppInfoSystem = new ArrayList<>();
    private List<AppInfo> mListAppInfoThird = new ArrayList<>();

    // Get a package manager
    public AppInfoProvider(Context context) {
        this.mPackageManager = context.getPackageManager();
        this.mContext = context;
    }

    public List<AppInfo> queryAppInfo() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // Through the query, get all ResolveInfo object
        List<ResolveInfo> resolveInfos = mPackageManager
                .queryIntentActivities(mainIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        // Call system sorting, sort by name
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));

        List<AppInfo> mListAppInfo = new ArrayList<>();
        mListAppInfo.clear();
        mListAppInfoSystem.clear();
        mListAppInfoThird.clear();
        for (ResolveInfo reInfo : resolveInfos) {
            String activityName = reInfo.activityInfo.name; // Get the name of the startup activity of the application
            String pkgName = reInfo.activityInfo.packageName; // Get the application package name
            String appLabel = (String) reInfo.loadLabel(mPackageManager); // Get the application label
            Drawable icon = reInfo.loadIcon(mPackageManager); // Get the application logo
            if (getFilterApp().contains(pkgName)) {
                // Get rid of yourself
                continue;
            }
            icon = filterAppIcon(activityName, icon);

            // For the startup activity of application，preparing Intent
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName,
                    activityName));

            // Create a AppInfo object and assignment
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(pkgName);
            appInfo.setAppName(appLabel);
            appInfo.setIcon(icon);
            appInfo.setActivityName(activityName);
            appInfo.setIntent(launchIntent);

            try {
                // Get PackageInfo mPackageInfo by package name（need handle exception）
                PackageInfo mPackageInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    // The third party application
                    appInfo.setSystem(false);
                    mListAppInfoThird.add(appInfo);
                } else {
                    // System application
                    appInfo.setSystem(true);
                    mListAppInfoSystem.add(appInfo);
                }
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.e(TAG, e.getMessage());
            }
        }
        mListAppInfo.addAll(mListAppInfoSystem);
        mListAppInfo.addAll(mListAppInfoThird);
        return mListAppInfo;
    }

    private Drawable filterAppIcon(String pkgName, Drawable icon) {
        switch (pkgName) {
            case "com.android.launcher.Media":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_video);
                break;

            //  Car machine setting
            case "com.android.launcher.Setup":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_car_setting);
                break;

            //  Vehicle information
            case "com.android.launcher.DrivingInfo":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_information);
                break;

            //  telephone
            case "com.android.dialer.DialtactsActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_phone);
                break;

            //  television
            case "com.android.launcher.TV":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_tv);
                break;
            //  email
            case "com.android.email.activity.Welcome":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_email);
                break;
            //  VCD/DVD player
            case "com.android.launcher.DVD":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_dvd);
                break;

            //  Auxiliary input
            case "com.android.launcher.Aux":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_helpinput);
                break;

            //  Calculator
            case "com.android.calculator2.Calculator":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_calculator);
                break;

            //  Air conditioner
            case "com.android.launcher.Air":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_airconditioning);
                break;

            //  Bluetooth phone
            case "com.android.launcher.Bluetooth":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_bt_call);
                break;

            //  Bluetooth music
            case "com.android.launcher.A2dp":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_btmusic);
                break;

            //  Calendar
            case "com.android.calendar.AllInOneActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_calendar);
                break;

            //  Setting
            case "com.android.settings.Settings":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_setting);
                break;

            //  Radio
            case "com.android.launcher.Radio":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_fm);
                break;

            //  Tire pressure
            case "com.android.launcher.Tpms":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_pressure);
                break;

            //  Address book
            case "com.android.contacts.activities.PeopleActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_contract);
                break;

            //  Gallery
            case "com.android.gallery3d.app.GalleryActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_photo);
                break;

            //  System update
            case "cn.flyaudio.otaupgrade.ui.CheckNewVersionActvity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_systemupdate);
                break;

            //  Download
            case "com.android.providers.downloads.ui.DownloadList":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_download);
                break;

            //  Message
            case "com.android.mms.ui.BootActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_message);
                break;

            //  Driver recorder
            case "com.android.launcher.Dvr":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_recorder);
                break;

            //  Application market
            case "com.flyaudio.flyaudioappmarket.ui.activity.MainActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_application);
                break;

            //  Remote diagnosis
            case "cn.flyaudio.flyremotediagnose.ui.GuideActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_remotehelp);
                break;

            //  Fly Audio
            case "cn.flyaudio.clientservice.qrcode.QRActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_smartfeige);
                break;
            //  Fly Audio v2
            case "cn.flyaudio.clientservicev2.qrcode.Activity.QRActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_smartfeige);
                break;

            //  Theme
            case "com.flyaudio.flyaudioskinmanager.ui.MainActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_theme);
                break;
            //  SMS
            case "com.android.mms.ui.ConversationList":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_message);
                break;
            //  Network sharing
            case "com.android.settings.Settings$TetherSettingsActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_networkshare);
                break;
            //  File management
            case "cn.flyaudio.flyfilemanager.ui.activity.MainActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_file);
                break;
            //  Baidu map
            case "com.baidu.naviauto.NaviAutoActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_baidu_map);
                break;

            //  Kaola FM
            case "com.kaolafm.auto.home.MainActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_kaolafm);
                break;

            // Kuwo music
            case "cn.kuwo.kwmusiccar.WelcomeActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_kuwomusic);
                break;
            //  Mx player
            case "com.mxtech.videoplayer.ad.ActivityMediaList":

                break;
            //  iFlytek Input
            case "com.iflytek.inputmethod.LauncherActivity":
                icon = mContext.getResources().getDrawable(R.drawable.home_more_ic_xunfei_inputmethod);
                break;
            default:
                break;
        }
        return icon;
    }

    private ArrayList<String> getFilterApp() {
        ArrayList<String> filterapp = new ArrayList<>();
        if (mContext != null) {
            String[] app = mContext.getResources().getStringArray(R.array.filter_app);
            if (app != null) {
                filterapp.addAll(Arrays.asList(app));
            }
        }

        return filterapp;
    }

}
