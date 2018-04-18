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
package com.baidu.che.codriverlauncher.ui;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.core.ScreenAdapter;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * launcher home activity
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";

    private TextView mLoginText;
    private View mUpgradeRedDotView;
    private TextView mPhoneText;
    private TextView mRadioText;
    private TextView mVideoText;
    private TextView mMoreText;
    private TextView mSettingText;
    private ImageView mNetworkImg;
    private ImageView mGpsImg;
    private ImageView mBtImg;
    private ImageView mVoiceView;

    private LocationManager mLocationManager;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenAdapter.getInstance().adaptDensity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager != null) {
            mLocationManager.addGpsStatusListener(mGpsStatusListener);
        }

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);

        updateBlueToothStatus();

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onServiceStateChanged(ServiceState serviceState) {
                    super.onServiceStateChanged(serviceState);
                    updateNetworkStatus();
                }
            }, PhoneStateListener.LISTEN_SERVICE_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpgradeRedDotView.setVisibility(View.GONE);
    }

    private void initView() {
        mLoginText = (TextView) findViewById(R.id.tv_personal_center);
        mUpgradeRedDotView = findViewById(R.id.personal_center_red_dot);
        mPhoneText = (TextView) findViewById(R.id.tv_phone);
        mRadioText = (TextView) findViewById(R.id.tv_radio);
        mVideoText = (TextView) findViewById(R.id.tv_video);
        mMoreText = (TextView) findViewById(R.id.tv_more);
        mSettingText = (TextView) findViewById(R.id.tv_setting);
        mNetworkImg = (ImageView) findViewById(R.id.img_network);
        mGpsImg = (ImageView) findViewById(R.id.img_gps);
        mBtImg = (ImageView) findViewById(R.id.img_bt);
        mVoiceView = (ImageView) findViewById(R.id.voice_bt);

        mLoginText.setOnClickListener(this);
        mPhoneText.setOnClickListener(this);
        mRadioText.setOnClickListener(this);
        mVideoText.setOnClickListener(this);
        mMoreText.setOnClickListener(this);
        mSettingText.setOnClickListener(this);
        mVoiceView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_personal_center:
                gotoPersonCenter();
                break;
            case R.id.tv_phone:
                gotoPhone();
                break;
            case R.id.tv_radio:
                gotoRadio();
                break;
            case R.id.tv_video:
                gotoVideo();
                break;
            case R.id.tv_more:
                gotoMoreActivity();
                break;
            case R.id.tv_setting:
                gotoSetting();
                break;
            case R.id.voice_bt:
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
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
        @SuppressLint("MissingPermission")
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
                mGpsImg.setImageResource(R.drawable.gps_1);
            } else if (satellites > 0) {
                mGpsImg.setImageResource(R.drawable.gps_2);
            } else {
                mGpsImg.setImageResource(R.drawable.gps_3);
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
                            mNetworkImg.setImageResource(R.drawable.wifi4);
                            break;
                        case 1:
                            mNetworkImg.setImageResource(R.drawable.wifi3);
                            break;
                        case 2:
                            mNetworkImg.setImageResource(R.drawable.wifi2);
                            break;
                        case 3:
                            mNetworkImg.setImageResource(R.drawable.wifi);
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
                        mNetworkImg.setImageResource(R.drawable.net_e);
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
                        mNetworkImg.setImageResource(R.drawable.net_3g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        mNetworkImg.setImageResource(R.drawable.net_4g);
                        break;
                    default:
                        mNetworkImg.setImageResource(R.drawable.net_none);
                        break;
                }
            }
        } else {
            mNetworkImg.setImageResource(R.drawable.net_none);
        }
    }

    @Override
    public void onBackPressed() {
    }

}