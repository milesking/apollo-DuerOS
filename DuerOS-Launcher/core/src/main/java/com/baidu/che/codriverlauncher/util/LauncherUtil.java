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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.baidu.che.codriverlauncher.CommonParams;
import com.baidu.che.codriverlauncher.core.R;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/*
 * device information tools used by launcher
 */
public class LauncherUtil {

    private static final String TAG = "LauncherUtil";
    private static final String UNKNOW = "unknow";
    private static int mVersionCode = -1;

    private static String mPackageName;
    private static String mVersionName;
    private static int mScreenWidth;
    private static int mScreenHeight;
    private static String mChannel;
    private static String mAv;
    private static String mAk;
    private static String mCuid;
    private static String mImei;
    private static String mImsi;
    private static String mWifiMac;
    private static String mBtMac;
    private static String mEXT;

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            mPackageName = pi.packageName;
            mVersionName = pi.versionName;
            mVersionCode = pi.versionCode;

            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowMgr.getDefaultDisplay().getMetrics(dm);
            mScreenWidth = Math.max(dm.widthPixels, dm.heightPixels);
            mScreenHeight = Math.min(dm.widthPixels, dm.heightPixels);

            setBuildNumber();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void init(Context context, String av, String ak, String channel, String cuid) {
        init(context);
        setAV(av);
        setAK(ak);
        setChannel(channel);
        setCuid(cuid);
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getResolution() {
        return mScreenWidth + "*" + mScreenHeight;
    }

    public static int getScreenWidth() {
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        return mScreenHeight;
    }

    public static String getPackageName() {
        return TextUtils.isEmpty(mPackageName) ? UNKNOW : mPackageName;
    }

    public static String getVersionName() {
        return TextUtils.isEmpty(mVersionName) ? UNKNOW : mVersionName;
    }

    public static int getVersionCode() {
        return mVersionCode;
    }

    /**
     * Get application OS version
     *
     * @return
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Acquisition equipment manufacturer
     *
     * @return
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * Get mobile phone model
     *
     * @return
     */
    public static String getModel() {
        return Build.MODEL;
    }

    public static String getChannel() {
        if (TextUtils.isEmpty(mChannel)) {
            mChannel = "CoDriver";
        }
        return mChannel;
    }

    public static void setChannel(String channel) {
        mChannel = channel;
    }

    public static String getAV() {
        if (TextUtils.isEmpty(mAv)) {
            mAv = "3";
        }
        return mAv;
    }

    public static void setAV(String av) {
        mAv = av;
    }

    public static String getAK() {
        if (TextUtils.isEmpty(mAk)) {
            mAk = "nc";
        }
        return mAk;
    }

    public static void setAK(String ak) {
        mAk = ak;
    }

    // Carlife is limited to a special parameter,
    // when the request is assigned,
    // and the request assignment is null
    public static String getEXT() {
        return mEXT;
    }

    // Carlife is limited to a special parameter,
    // when the request is assigned,
    // and the request assignment is null
    public static void setEXT(String ext) {
        mEXT = ext;
    }

    public static void setCuid(String cuid) {
        mCuid = cuid;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isDebug() {
        return CommonParams.LOG_LEVEL < Log.ERROR;
    }

    /*
     * Light up screen
     */
    private static void brightScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            PowerManager.WakeLock wl =
                    pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
    }

    //    public static String getCuid() {
    //        String temp = CommonParam.getCUID(mContext);
    //        return TextUtils.isEmpty(temp) ? "unknow" : temp;
    //    }

    public static String getIMEI() {
        if (TextUtils.isEmpty(mImei)) {
            TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mImei = tm.getDeviceId();
        }

        if (mImei == null) {
            mImei = "";
        }

        return mImei;
    }

    /**
     * Get IMSI, international mobile user identification code（IMSI：International Mobile Subscriber Identification Number）
     */
    public static String getIMSI() {
        if (TextUtils.isEmpty(mImsi)) {
            TelephonyManager teleMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mImsi = teleMgr.getSubscriberId();
        }

        if (mImsi == null) {
            mImsi = "";
        }

        return mImsi;
    }

    // Wifi Mac address
    public static String getWifiMac() {
        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            mWifiMac = info.getMacAddress();
        }
        if (!TextUtils.isEmpty(mWifiMac)) {
            mWifiMac = mWifiMac.replace(":", "");
            return mWifiMac;
        } else {
            return "";
        }
    }

    // BlueTooth Mac address
    public static String getBtMac() {
        mBtMac = BluetoothAdapter.getDefaultAdapter().getAddress();
        if (!TextUtils.isEmpty(mBtMac)) {
            mBtMac = mBtMac.replace(":", "");
            return mBtMac;
        } else {
            return "";
        }
    }

    // Android Id
    public static String getAndroidId() {
        return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Determine whether it is a WIFI state
     */
    public static boolean isWifi() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = null;
        if (connectivityManager != null) {
            activeNetInfo = connectivityManager.getActiveNetworkInfo();
        }

        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Read the channel number from APK
     *
     * @return channel number of APK
     */
    public static String readTnNumbersFromApk(int id) {
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = mContext.getResources().openRawResource(id);
            bos = new ByteArrayOutputStream();
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = in.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }

            String result = new String(bos.toByteArray()).trim();
            return result;
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e.toString());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e.toString());
                }
            }
        }

        return "CoDriver";
    }

    public static boolean startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(mContext, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogUtil.e(TAG, "catch Exception ", e);
        }
        return false;
    }

    private static void setBuildNumber() {
        if (isDebug()) {
            CommonParams.BUILD_NUMBER = CommonParams.BUILD_NUMBER.replace("r", "d");
        }
        LogUtil.e(TAG, "BuildNumber = " + CommonParams.BUILD_NUMBER);
    }
}
