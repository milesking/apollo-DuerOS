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
package com.baidu.che.codriverlauncher.ui;

import com.baidu.che.codriverlauncher.BaseActivity;
import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.util.AppInfoProvider;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * launcher home activity
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";

    private ImageView mNetworkImg;
    private ImageView mGpsImg;
    private ImageView mBtImg;

    private LocationManager mLocationManager;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    private AppInfoProvider appInfoProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(mGpsStatusListener);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);

        updateBlueToothStatus();

        appInfoProvider = new AppInfoProvider(HomeActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        findViewById(R.id.main_tv_personnal_center).setOnClickListener(this);
        findViewById(R.id.main_tv_phone).setOnClickListener(this);
        findViewById(R.id.main_tv_radio).setOnClickListener(this);
        findViewById(R.id.main_tv_video).setOnClickListener(this);
        findViewById(R.id.main_tv_more).setOnClickListener(this);
        findViewById(R.id.main_tv_setting).setOnClickListener(this);
        findViewById(R.id.main_img_voice_bt).setOnClickListener(this);

        mNetworkImg = (ImageView) findViewById(R.id.main_img_network);
        mGpsImg = (ImageView) findViewById(R.id.main_img_gps);
        mBtImg = (ImageView) findViewById(R.id.main_img_bt);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_tv_personnal_center:
                gotoPersonCenter();
                break;
            case R.id.main_tv_phone:
                gotoPhone();
                break;
            case R.id.main_tv_radio:
                gotoRadio();
                break;
            case R.id.main_tv_video:
                gotoVideo();
                break;
            case R.id.main_tv_more:
                gotoMoreActivity();
                break;
            case R.id.main_tv_setting:
                gotoSetting();
                break;
            case R.id.main_img_voice_bt:
                gotoVoice();
                break;
            default:
                break;
        }
    }

    private void gotoMoreActivity() {
        Intent intent = new Intent(this, MoreActivity.class);
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoVoice() {
        Intent intent = new Intent("android.intent.action.START_CODRIVER");
        intent.setPackage("com.baidu.che.codriver");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        boolean appInstalled = appInfoProvider.isAppInstalled(this, "com.baidu.che.codriver");
        if (!appInstalled) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
        sendBroadcast(intent);
    }

    private void gotoPersonCenter() {
        Intent intent = new Intent();
        intent.setClassName("com.baidu.che.codriver", "com.baidu.che.codriver.ui.SettingActivity");
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoPhone() {
        Intent intent = new Intent();
        intent.setClassName("com.flyaudio.flyguardian", "com.android.launcher.Bluetooth");
        LauncherUtil.startActivitySafely(intent);
        //        intent.setAction(Intent.ACTION_DIAL);
        //        intent.setData(Uri.parse("tel:"));
        //        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoRadio() {
        Intent intent = new Intent();
        intent.setClassName("com.flyaudio.flyguardian", "com.android.launcher.Media");
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoVideo() {
        Intent intent = new Intent();
        intent.setClassName("com.flyaudio.flyguardian", "com.android.launcher.Radio");
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoSetting() {
        Intent intent = new Intent();
        intent.setClassName("com.flyaudio.flyguardian", "com.android.launcher.Setup");
        LauncherUtil.startActivitySafely(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.removeGpsStatusListener(mGpsStatusListener);
        unregisterReceiver(mReceiver);
    }

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            int satellites = 0;
            int satellitesInFix = 0;
            int timeToFix = mLocationManager.getGpsStatus(null).getTimeToFirstFix();
            for (GpsSatellite sat : mLocationManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }
            LogUtil.i(TAG, satellites + " Used In Last Fix (" + satellitesInFix + ")");
            if (satellites > 10) {
                mGpsImg.setImageResource(R.drawable.ic_gps_1);
            } else if (satellites > 0) {
                mGpsImg.setImageResource(R.drawable.ic_gps_2);
            } else {
                mGpsImg.setImageResource(R.drawable.ic_gps_3);
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onReceive(): " + action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                updateBlueToothStatus();
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                updateNetworkStatus();
            }
        }
    };

    private void updateBlueToothStatus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mBtImg.setImageResource(R.drawable.bt_off);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mBtImg.setImageResource(R.drawable.bt_on);
            } else {
                mBtImg.setImageResource(R.drawable.bt_off);
            }
        }
    }

    private void updateNetworkStatus() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (info != null) {
            LogUtil.i(TAG, "getTypeName(): " + info.getTypeName());
            LogUtil.i(TAG, "getSubtypeName(): " + info.getSubtypeName());
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                if (mWifiManager.getConnectionInfo() != null) {
                    int rssi = mWifiManager.getConnectionInfo().getRssi();
                    int level = WifiManager.calculateSignalLevel(rssi, 4);
                    switch (level) {
                        case 0:
                            mNetworkImg.setImageResource(R.drawable.ic_wifi4);
                            break;
                        case 1:
                            mNetworkImg.setImageResource(R.drawable.ic_wifi3);
                            break;
                        case 2:
                            mNetworkImg.setImageResource(R.drawable.ic_wifi2);
                            break;
                        case 3:
                            mNetworkImg.setImageResource(R.drawable.ic_wifi);
                            break;
                        default:
                            break;
                    }
                    LogUtil.i(TAG, "Wifi Level " + level);
                }
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        mNetworkImg.setImageResource(R.drawable.ic_net_e);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        mNetworkImg.setImageResource(R.drawable.ic_net_3g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        mNetworkImg.setImageResource(R.drawable.ic_net_4g);
                        break;
                    default:
                        mNetworkImg.setImageResource(R.drawable.ic_net_none);
                        break;
                }
            }
        } else {
            mNetworkImg.setImageResource(R.drawable.ic_net_none);
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }
}