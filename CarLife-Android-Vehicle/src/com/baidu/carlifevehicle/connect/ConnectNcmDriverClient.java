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

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Communication with the system
 */
public class ConnectNcmDriverClient {
    public static final String TAG = "ConnectNcmDriverClient";
    public static ConnectNcmDriverClient mInstance = null;
    public NcmDriverClientThread mNcmDriverClientThread = null;
    public static final int SLEEP_TIME_500MS = 500;
    public static final int SLEEP_TIME_100MS = 100;

    private static final String socketName = "/data/local/tmp/ncm.sock";
    private static final int sendUsbRoleSwitch = 0x01;
    private static final int iap2AuthBefore = 0x02;
    private static final int iap2AuthAfter = 0x03;
    private static final int allocationNcmIp = 0x04;
    private static final int eaOpenSession = 0x05;
    private static final int eaCloseSession = 0x06;

    private static final int usbDisconnect = 0x10;

    public static final int PULL_UP_CARLIFE = 0x11;
    public static final int USB_RESET = 0x12;
    public boolean ncmConnecting = false;

    private DataInputStream mReader = null;
    private DataOutputStream mWriter = null;
    private LocalSocket ncmLocalSocket = null;

    public static ConnectNcmDriverClient getInstance() {
        if (null == mInstance) {
            synchronized (ConnectNcmDriverClient.class) {
                if (null == mInstance) {
                    mInstance = new ConnectNcmDriverClient();
                }
            }
        }
        return mInstance;
    }

    private ConnectNcmDriverClient() {

    }

    public void setNcmConnecting(boolean tag) {
        ncmConnecting = tag;
        LogUtil.d(TAG, "setNcmConnecting NcmConnecting = " + ncmConnecting);
    }

    public boolean getNcmConnecting() {
        LogUtil.d(TAG, "getNcmConnecting " + ncmConnecting);
        return ncmConnecting;
    }

    public void startNcmDriverClientThread() {
        LogUtil.d(TAG, "NcmDriverClientThread Created");
        
        int connectTypeValue = ConnectManager.getInstance().getIphoneConnectType();
        if ((ConnectManager.CONNECTED_BY_AOA != connectTypeValue)
                && (ConnectManager.CONNECTED_BY_EAN != connectTypeValue)) {
            LogUtil.e(TAG, "wifi connect! ");
            return ;
        }
        mNcmDriverClientThread = new NcmDriverClientThread();
        mNcmDriverClientThread.start();
    }

    public void stopNcmDriverClientThread() {
        LogUtil.d(TAG, "stop NcmDriverClientThread");
        if (mNcmDriverClientThread != null) {
            mNcmDriverClientThread.cancel();
            mNcmDriverClientThread = null;
        }
        closeLocalSocket();
    }

    public void closeLocalSocket() {
        if (ncmLocalSocket == null) {
            return;
        }
        try {
            LogUtil.d(TAG, "close Local Socket!");
            mWriter.close();
            mReader.close();
            ncmLocalSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mWriter = null;
        mReader = null;
        ncmLocalSocket = null;
    }

    public boolean writeDataToDriver(int data) {
        boolean writeState = false;
        int connectTypeValue = ConnectManager.getInstance().getIphoneConnectType();
        LogUtil.d(TAG, "writeDataToDriver " + data);
        if (null == mWriter) {
            LogUtil.d(TAG, "mWriteData is null ");
            return writeState;
        }
        if ((ConnectManager.CONNECTED_BY_AOA != connectTypeValue)
                && (ConnectManager.CONNECTED_BY_EAN != connectTypeValue)) {
            LogUtil.e(TAG, "wifi connect! ");
            return writeState;
        }
        try {
            mWriter.write(data);
            writeState = true;
        } catch (IOException e) {
            LogUtil.e(TAG, "writeDataToDriver error ");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return writeState;
    }

    private class NcmDriverClientThread extends Thread {
        private boolean isRunning = true;
        public byte driverState = 0;

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            int numberProgress = 0;
            boolean ret = false;
            
            try {
                LogUtil.d(TAG, "Local Socket connect ");
                int connectTypeValue = ConnectManager.getInstance().getIphoneConnectType();;
                ncmLocalSocket = new LocalSocket();
                ncmLocalSocket.connect(new LocalSocketAddress(socketName));

                LogUtil.d(TAG, "Local Socket connect sucess! ");
                mReader = new DataInputStream(ncmLocalSocket.getInputStream());
                mWriter = new DataOutputStream(ncmLocalSocket.getOutputStream());

                while (isRunning) {
                    try {
                        driverState = mReader.readByte();
                        LogUtil.d(TAG, "read driver state  " + driverState);
                        if (ConnectManager.CONNECTED_BY_EAN != connectTypeValue) {
                            LogUtil.d(TAG, "wifi connect! ");
                            sleep(SLEEP_TIME_100MS);
                            continue;
                        }

                        if (ConnectClient.getInstance().isCarlifeConnected()) {
                            ret = false;
                            switch (driverState) {
                                case usbDisconnect:
                                case eaOpenSession:
                                case eaCloseSession:
                                    ret = true;
                                    break;
                                default:
                                    break;
                            }
                            if (!ret) {
                                LogUtil.d(TAG, "already connected");
                                sleep(SLEEP_TIME_100MS);
                                continue;
                            }
                        }
                        switch (driverState) {
                            case sendUsbRoleSwitch:
                                if (numberProgress < 10) {
                                    numberProgress = 10;
                                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER,
                                            numberProgress, 0, null);
                                }
                                break;
                            case iap2AuthBefore:
                                if (numberProgress < 30) {
                                    numberProgress = 30;
                                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER,
                                            numberProgress, 0, null);
                                }
                                break;
                            case iap2AuthAfter:
                                if (numberProgress < 60) {
                                    numberProgress = 60;
                                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER,
                                            numberProgress, 0, null);
                                }
                                break;
                            case allocationNcmIp:
                                if (numberProgress < 70) {
                                    numberProgress = 70;
                                    writeDataToDriver(PULL_UP_CARLIFE);
                                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER,
                                            numberProgress, 0, null);
                                }
                                break;
                            case eaOpenSession:
                                EanConnectManager.getInstance().startEanConnectThread();
                                break;
                            case eaCloseSession:
                                ConnectClient.getInstance().setIsConnected(false);
                                break;
                            case usbDisconnect:
                                if (ConnectClient.getInstance().isCarlifeConnected()) {
                                    ConnectClient.getInstance().setIsConnected(false);
                                    ConnectManager.getInstance().stopConnectThread();
                                } else {
                                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
                                    // MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL);
                                    LogUtil.d(TAG, "carLife is disconnect");
                                }
                                numberProgress = 0;
                                setNcmConnecting(false);
                                break;
                            default:
                                LogUtil.d(TAG, "read data is not activity!");
                                Thread.sleep(SLEEP_TIME_500MS);
                                break;
                        }
                    } catch (Exception e) {
                        LogUtil.e(TAG, "get Exception in readByte");
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException e) {
                LogUtil.e(TAG, "Local Socket error ");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
