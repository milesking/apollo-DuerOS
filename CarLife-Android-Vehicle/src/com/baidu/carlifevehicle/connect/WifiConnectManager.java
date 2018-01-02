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
package com.baidu.carlifevehicle.connect;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.ErrorCodeReport;
import com.baidu.carlifevehicle.util.ErrorCodeType;
import com.baidu.carlifevehicle.util.LogUtil;

public class WifiConnectManager {
    private static final String TAG = "WIFIConnectManager";
    private static final String WIFI_CONNECT_THREAD_NAME = "WIFIConnectThread";
    private static final int RECEIVE_BUFFER_SIZE = 1024;
    private byte[] receiveBuf = null;

    private DatagramSocket mSocket = null;
    private DatagramPacket mPacket = null;

    private WifiConnectThread mWIFIConnectThread = null;

    private static WifiConnectManager mInstance = null;
    private String lastIPAddress = null;

    private boolean isListen = true;

    private WifiConnectManager() {
        try {
            mSocket = new DatagramSocket(CommonParams.BOARDCAST_WIFI_PORT);
            receiveBuf = new byte[RECEIVE_BUFFER_SIZE];
            mWIFIConnectThread = new WifiConnectThread();
            mWIFIConnectThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "WifiConnectManager create fail");
            e.printStackTrace();
        }
    }

    public static WifiConnectManager getInstance() {
        if (null == mInstance) {
            synchronized (WifiConnectManager.class) {
                if (null == mInstance) {
                    mInstance = new WifiConnectManager();
                }
            }
        }
        return mInstance;
    }

    public void startWifiConnectThread() {
        LogUtil.d(TAG, "WifiConnectThread start");
        isListen = true;
    }

    public void stopWifiConnectThread() {
        LogUtil.d(TAG, "WifiConnectThread end");
        isListen = false;
    }

    private class WifiConnectThread extends Thread {
        private boolean isRunning = true;

        public WifiConnectThread() {
            LogUtil.d(TAG, "WifiConnectThread Created");
            setName(WIFI_CONNECT_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    LogUtil.e(TAG, "read broadcast socket...");
                    mPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                    mSocket.receive(mPacket);
                    if (false == isRunning) {
                        LogUtil.e(TAG, "WifiConnectThread has been canceled");
                        continue;
                    }
                    LogUtil.e(TAG, "receive broadcast packet");
                    if (isListen && null != mPacket) {
                        final String serverIPAddress = mPacket.getAddress().getHostAddress();
                        LogUtil.e(TAG, "received %s", serverIPAddress);
                        lastIPAddress = serverIPAddress;
                        ConnectManager.SERVER_WIFI_URL = serverIPAddress;

                        if (ConnectClient.getInstance().isCarlifeConnecting()) {
                            ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.IOS_WIFI_ERROR_IS_CONNECTING);
                            LogUtil.e(TAG, "already connecting");
                            continue;
                        }
                        if (ConnectClient.getInstance().isCarlifeConnected()) {
                            ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.IOS_WIFI_ERROR_IS_CONNECTED);
                            LogUtil.d(TAG, "already connected");
                            continue;
                        }
                        ConnectClient.getInstance().setIsConnecting(true);
                        ConnectManager.getInstance().initConnectType(ConnectManager.CONNECTED_BY_WIFI);
                        MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 50, 0, null);
                        ConnectManager.getInstance().startAllConnectSocket();
                        ConnectManager.getInstance().stopConnectThread();
                    }

                } catch (Exception e) {
                    LogUtil.e(TAG, "UDPSocket IOException, Receive Data Fail");
                    e.printStackTrace();
                }
            }
        }

    }
}