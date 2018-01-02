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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class IphoneUsbTethering {
    private static final String TAG = "IphoneUsbTethering";
    private static final String USB_TETHERING_CONNECT_THREAD_NAME = "UsbTetheringConnectThread";
    private static final int RECEIVE_BUFFER_SIZE = 1024;
    private static IphoneUsbTethering mInstance = null;
    private byte[] receiveBuf = null;

    private DatagramSocket mSocket = null;
    private DatagramPacket mPacket = null;

    private UsbTetheringConnectThread mUsbTetheringConnectThread = null;
    private boolean isListen = true;

    private IphoneUsbTethering() {
        try {
            mSocket = new DatagramSocket(CommonParams.BOARDCAST_WIFI_PORT);
            receiveBuf = new byte[RECEIVE_BUFFER_SIZE];
            mUsbTetheringConnectThread = new UsbTetheringConnectThread();
            mUsbTetheringConnectThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "IphoneUsbTethering create fail");
            e.printStackTrace();
        }
    }

    public static IphoneUsbTethering getInstance() {
        if (null == mInstance) {
            synchronized (IphoneUsbTethering.class) {
                if (null == mInstance) {
                    mInstance = new IphoneUsbTethering();
                }
            }
        }
        return mInstance;
    }

    public void startUsbTetheringConnectThread() {
        LogUtil.d(TAG, "UsbTetheringConnectThread start");
        isListen = true;
    }

    public void stopUsbTetheringConnectThread() {
        LogUtil.d(TAG, "UsbTetheringConnectThread end");
        isListen = false;
    }

    private class UsbTetheringConnectThread extends Thread {
        private boolean isRunning = true;

        public UsbTetheringConnectThread() {
            LogUtil.d(TAG, "UsbTetheringConnectThread Created");
            setName(USB_TETHERING_CONNECT_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    LogUtil.e(TAG, "UsbTethering read broadcast socket...");
                    mPacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                    mSocket.receive(mPacket);
                    if (false == isRunning) {
                        LogUtil.e(TAG, "UsbTetheringConnectThread has been canceled");
                        continue;
                    }
                    LogUtil.e(TAG, "UsbTethering receive broadcast packet");
                    if (isListen && null != mPacket) {
                        final String serverIPAddress = mPacket.getAddress().getHostAddress();
                        LogUtil.e(TAG, "UsbTethering received %s", serverIPAddress);

                        ConnectManager.SERVER_USB_TETHERING_URL = serverIPAddress;

                        if (ConnectClient.getInstance().isCarlifeConnecting()) {
                            LogUtil.e(TAG, "already connecting");
                            continue;
                        }
                        if (ConnectClient.getInstance().isCarlifeConnected()) {
                            LogUtil.d(TAG, "already connected");
                            continue;
                        }

                        ConnectClient.getInstance().setIsConnecting(true);
                        ConnectManager.getInstance().initConnectType(ConnectManager.CONNECTED_BY_USB_TETHERING);
                        MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 60, 0, null);
                        ConnectManager.getInstance().startAllConnectSocket();
                        ConnectManager.getInstance().stopConnectThread();
                    }

                } catch (Exception e) {
                    LogUtil.e(TAG, "UsbTethering UDPSocket IOException, Receive Data Fail");
                    e.printStackTrace();
                }
            }
        }

    }
}
