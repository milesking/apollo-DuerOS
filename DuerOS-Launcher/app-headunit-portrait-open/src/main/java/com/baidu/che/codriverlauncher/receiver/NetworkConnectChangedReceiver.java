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
package com.baidu.che.codriverlauncher.receiver;

import com.baidu.che.codriverlauncher.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

/**
 * network changed broadcast
 */

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkConnectChangedReceiver";

    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3G";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "wifi";
        }
        return connType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Monitor wifi status, have nothing to do with wifi connection
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            LogUtil.d(TAG, "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                default:
                    break;
            }
        }
        // Monitor the connection status of WiFi, that is whether or not there is an effective wireless routing
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                // Get the network status of the NetWorkInfo object
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                // The State object is the state of connection success or not
                NetworkInfo.State state = networkInfo.getState();
                // Determine whether or not the network has been connected
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                LogUtil.d(TAG, "isConnected:" + isConnected);
            }
        }
        // To monitor the network connection, including wifi and mobile data opening and closing,
        // as well as connection available on the connection had been listening
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            // Get the NetworkInfo object of the network state
            NetworkInfo info = intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                // If the current network connection is successful and the network connection is available
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI
                            || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        LogUtil.i(TAG, getConnectionType(info.getType()) + " connected");
                        // transform to the following module
                        if (mOnNetworkChangeListener != null) {
                            LogUtil.i(TAG, "mOnLocationListener is set");
                            mOnNetworkChangeListener.onNetworkChange();
                        }
                    }
                } else {
                    LogUtil.i(TAG, getConnectionType(info.getType()) + " disconnected");
                    if (mOnNetworkChangeListener != null) {
                        LogUtil.i(TAG, "onShowCompass is set");
                        mOnNetworkChangeListener.onShowCompass();
                    }
                }
            }

        }
    }

    private static OnNetworkChangeListener mOnNetworkChangeListener;

    public static void setNetworkLtListener(OnNetworkChangeListener onNetworkChangeListener) {
        mOnNetworkChangeListener = onNetworkChangeListener;
    }
}
