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
package com.baidu.che.codriverlauncher.home;

import com.baidu.che.codriverlauncher.BaseActivity;
import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.more.MoreActivity;
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
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * show launcher
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";

    private static final String BAIDU_NAVIAUTO_START_ACTIVITY_ACTION = "com.baidu.naviauto.action.START";

    private ImageView mNetworkImg;
    private ImageView mGpsImg;
    private ImageView mBtImg;
    private ImageView mVoiceView;

    private LocationManager mLocationManager;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;

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
    }

    private void initView() {
        findViewById(R.id.main_item_map).setOnClickListener(this);
        findViewById(R.id.main_item_recorder).setOnClickListener(this);
        findViewById(R.id.main_item_personnal_center).setOnClickListener(this);
        findViewById(R.id.main_item_phone).setOnClickListener(this);
        findViewById(R.id.main_item_setting).setOnClickListener(this);
        findViewById(R.id.main_item_more).setOnClickListener(this);
        mNetworkImg = (ImageView) findViewById(R.id.main_imgv_network);
        mGpsImg = (ImageView) findViewById(R.id.main_imgv_gps);
        mBtImg = (ImageView) findViewById(R.id.main_imgv_bluetooth);
        mVoiceView = (ImageView) findViewById(R.id.main_imgv_voice);
        mVoiceView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_item_map:
                gotoMapAuto();
                break;
            case R.id.main_item_recorder:
                gotoRecorder();
                break;
            case R.id.main_item_personnal_center:
                gotoPersonCenter();
                break;
            case R.id.main_item_phone:
                gotoPhone();
                break;
            case R.id.main_item_setting:
                gotoSetting();
                break;
            case R.id.main_item_more:
                gotoMoreActivity();
                break;
            case R.id.main_imgv_voice:
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
        sendBroadcast(intent);
    }

    private void gotoPersonCenter() {
        Intent intent = new Intent();
        intent.setClassName("com.baidu.che.codriver", "com.baidu.che.codriver.ui.SettingActivity");
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoRecorder() {
        Toast.makeText(this, getResources().getString(R.string.has_no_recorder), Toast.LENGTH_SHORT).show();
    }

    private void gotoPhone() {
        Toast.makeText(this, getResources().getString(R.string.has_no_phone), Toast.LENGTH_SHORT).show();
    }

    private void gotoMapAuto() {
        Uri uri = Uri.parse("codriver://");
        Intent intent = new Intent(BAIDU_NAVIAUTO_START_ACTIVITY_ACTION, uri);
        LauncherUtil.startActivitySafely(intent);
    }

    private void gotoSetting() {
        Toast.makeText(this, getResources().getString(R.string.has_no_settings), Toast.LENGTH_SHORT).show();
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
            for (GpsSatellite sat : mLocationManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }
            LogUtil.i(TAG, "GpsStatus.Listener: " + satellites + " Used In Last Fix (" + satellitesInFix + ")");
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
            LogUtil.i(TAG, "network type name: " + info.getTypeName());
            LogUtil.i(TAG, "network subType name: " + info.getSubtypeName());
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