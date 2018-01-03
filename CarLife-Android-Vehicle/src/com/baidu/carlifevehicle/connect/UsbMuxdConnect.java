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

import java.util.HashMap;
import java.util.Iterator;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.logic.CarlifeProtocolVersionInfoManager;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.DigitalTrans;
import com.baidu.carlifevehicle.util.LogUtil;
import com.baidu.carlifevehicle.message.MsgBaseHandler;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;

/**
 * Usbmuxd Connection Manager
 */

public class UsbMuxdConnect {
    public static final String TAG = "UsbMuxdConnect";
    public static String SERVER_IOS_ADDRESS = "127.0.0.1";
    private static final int VID_IPHONE = 0x05AC;
    private static final int PID_IPHONE = 0x12A8;

    public static int ConnectType = ConnectManager.CONNECTED_BY_USBMUXD;
    public static final int SLEEP_TIME_1S = 1000;
    public static final int SLEEP_TIME_2S = 2000;
    public static final int SOCKET_USBMUXD_PORT = 7210;
    public static final int SOCKET_VIDEO_USBMUXD_PORT = 8210;
    public static final int SOCKET_AUDIO_USBMUXD_PORT = 9210;
    public static final int SOCKET_AUDIO_TTS_USBMUXD_PORT = 9211;
    public static final int SOCKET_AUDIO_VR_USBMUXD_PORT = 9212;
    public static final int SOCKET_TOUCH_USBMUXD_PORT = 9310;

    private volatile boolean mIsConnectedToDevice = false;
    private boolean usbAttch = false;
    private MsgBaseHandler mMSGHandler;
    private UsbMuxdConnectThread mUsbMuxdConnectThread = null;
    private static UsbMuxdConnect mInstance = null;
    private UsbManager mUsbManager = null;
    private Context mContext = null;

    public static UsbMuxdConnect getInstance() {
        if (null == mInstance) {
            synchronized (UsbMuxdConnect.class) {
                if (null == mInstance) {
                    mInstance = new UsbMuxdConnect(SERVER_IOS_ADDRESS);
                }
            }
        }
        return mInstance;
    }

    private UsbMuxdConnect(String serverAddress) {
        ConnectManager.SERVER_USBMUXD_URL = serverAddress;
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mContext = context;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }

    public void setUsbAttch(boolean status) {
        usbAttch = status;
    }

    public boolean scanIphoneDevices() {
        if (mContext == null || mUsbManager == null) {
            LogUtil.e(TAG, "scanUsbDevices fail");
            return false;
        }

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        LogUtil.d(TAG, "device count = " + deviceList.size());
        if (deviceList.size() == 0) {
            return false;
        }
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device == null) {
                continue;
            }

            int vid = device.getVendorId();
            int pid = device.getProductId();
            LogUtil.d(TAG, "vid = 0x" + DigitalTrans.algorismToHEXString(vid, 4));
            LogUtil.d(TAG, "pid = 0x" + DigitalTrans.algorismToHEXString(pid, 4));

            if (device.getVendorId() == VID_IPHONE) {
                LogUtil.d(TAG, "the device is iPhone");
                return true;
            }
            LogUtil.d(TAG, device.toString());
        }
        return false;
    }

    public void startUsbMuxdConnectThread() {
        LogUtil.d(TAG, "UsbMuxdConnectThread start");
        mMSGHandler = new UsbMuxdConnectHandler();
        MsgHandlerCenter.registerMessageHandler(mMSGHandler);
        mUsbMuxdConnectThread = new UsbMuxdConnectThread();
        mUsbMuxdConnectThread.start();
    }

    public void stopUsbMuxdConnectThread() {
        LogUtil.d(TAG, "UsbMuxdConnectThread stop");
        MsgHandlerCenter.unRegisterMessageHandler(mMSGHandler);
        mMSGHandler = null;
        if (mUsbMuxdConnectThread != null) {
            mUsbMuxdConnectThread.cancel();
            mUsbMuxdConnectThread = null;
        }
    }

    private class UsbMuxdConnectThread extends Thread {
        private boolean isRunning = true;

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            int tryTime = 1;
            while (isRunning) {
                mIsConnectedToDevice = false;
                try {
                    if (!isRunning) {
                        LogUtil.e(TAG, "read data cancled");
                        return;
                    }
                    if (!scanIphoneDevices()) {
                        LogUtil.d(TAG, "no iPhone connect");
                        break;
                    }
                    if (usbAttch) {
                        usbAttch = false;
                        MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 10, 0, null);
                        LogUtil.d(TAG, "iPhone attach connect,sleep 1s");
                        sleep(SLEEP_TIME_2S);
                        MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 30, 0, null);
                        sleep(SLEEP_TIME_2S);
                    }
                    LogUtil.d(TAG, "usbmuxd connecting");
                    ConnectClient.getInstance().setIsConnecting(true);
                    ConnectManager.getInstance().initConnectType(ConnectType);
                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 60, 0, null);
                    ConnectManager.getInstance().startAllConnectSocket();
                    if (!ConnectClient.getInstance().isCarlifeConnected()) {
                        LogUtil.d(TAG, "usbmuxd socket is connected");
                        CarlifeProtocolVersionInfoManager.getInstance().sendProtocolMatchStatus();
                    } else {
                        LogUtil.d(TAG, "calife is connected");
                        break;
                    }
                    sleep(SLEEP_TIME_1S);
                    if (!mIsConnectedToDevice) {
                        ConnectManager.getInstance().stopAllConnectSocket();
                        if (tryTime > 0) {
                            tryTime--;
                            LogUtil.d(TAG, "usbmuxd connect tryTime = " + tryTime);
                            continue;
                        } else if (tryTime == 0) {
                            LogUtil.d(TAG, "usbmuxd  connect fail");
                            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL);
                            break;
                        }
                    }
                    ConnectClient.getInstance().setIsConnected(true);
                    ConnectManager.getInstance().stopConnectThread();
                } catch (Exception e) {
                    LogUtil.e(TAG, "UsbMuxdConnectThread Fail");
                    e.printStackTrace();
                }
            }

        }
    }

    private class UsbMuxdConnectHandler extends MsgBaseHandler {

        public void handleMessage(Message msg) {
            LogUtil.d(TAG, "UsbMuxdConnectHandler msg.what = " + msg.what);
            if (msg.what == CommonParams.MSG_CMD_PROTOCOL_VERSION_MATCH_STATUS) {
                mIsConnectedToDevice = true;
            }
        }

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_PROTOCOL_VERSION_MATCH_STATUS);
        }
    }

}
