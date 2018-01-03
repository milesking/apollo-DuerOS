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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.DigitalTrans;
import com.baidu.carlifevehicle.util.LogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


public class AOAHostSetup {

    private static final String TAG = "AOAHostSetup";

    private static AOAHostSetup mInstance = null;
    private Context mContext = null;

    private static final int PID_ACCESSORY_ONLY = 0x2D00;
    private static final int PID_ACCESSORY_ADB = 0x2D01;
    private static final int PID_ACCESSORY_AUDIO = 0x2D02;
    private static final int PID_ACCESSORY_AUDIO_ADB = 0x2D03;
    private static final int PID_ACCESSORY_AUDIO_BULK = 0x2D04;
    private static final int PID_ACCESSORY_AUDIO_ADB_BULK = 0x2D05;

    private static final int VID_ACCESSORY = 0x18D1;
    private static final int PID_ACCESSORY = PID_ACCESSORY_AUDIO_ADB_BULK;

    private static final int VID_IPHONE = 0x05AC;
    private static final int PID_IPHONE = 0x12A8;

    private static final int BULK_ENDPOINT_IN = 0x81;
    private static final int BULK_ENDPOINT_OUT = 0x02;
    private static final int BULK_ENDPOINT_IN_ADDRESS = 0;
    private static final int BULK_ENDPOINT_OUT_ADDRESS = 1;
    private static final int TIME_OUT = 0;

    private static final int AOA_GET_PROTOCOL = 51;
    private static final int AOA_SEND_IDENT = 52;
    private static final int AOA_START_ACCESSORY = 53;
    private static final int AOA_AUDIO_SUPPORT = 58;

    private static final String AOA_MANUFACTURER = "Baidu";
    private static final String AOA_MODEL_NAME = "CarLife";
    private static final String AOA_DESCRIPTION = "Baidu CarLife";
    private static final String AOA_VERSION = "1.0.0";
    private static final String AOA_URI = "http://carlife.baidu.com/";
    private static final String AOA_SERIAL_NUMBER = "0720SerialNo.";

    private static final int SLEEP_TIME_MS = 1000;

    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent = null;

    private UsbDevice mUsbDevice = null;
    private UsbInterface mUsbInterface = null;
    private UsbDeviceConnection mUsbDeviceConnection = null;

    private UsbEndpoint mUsbEndpointIn = null;
    private UsbEndpoint mUsbEndpointOut = null;

    private boolean isRequestPermission = false;

    private static int TEST_MSG_NUM = 0;
    private static int TOTAL_TEST_MSG_NUM = 10 * 1024;
    private static final int TEST_MAX_BUFFER_BYTES = 160 * 1024;
    private Thread mReadThread = null;
    private Thread mWriteThread = null;

    private static final int AOA_MAX_BUFFER_BYTES = 16 * 1024;
    private byte[] mDataBuffer = new byte[AOA_MAX_BUFFER_BYTES];

    private int numberProgress = 0;

    private static final int MAX_TIME_CONNECT_RETRY = 10;
    private static final int MAX_TIME_CONNECT_RETRY_NO_PERMISSION = 5;
    private int retryNoPermission = 0;
    private int retryAccessoryMode = 0;

    private AOAHostSetup() {

    }

    public static AOAHostSetup getInstance() {
        if (null == mInstance) {
            synchronized (AOAHostSetup.class) {
                if (null == mInstance) {
                    mInstance = new AOAHostSetup();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mContext = context;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        mPermissionIntent =
                PendingIntent.getBroadcast(mContext, 0, new Intent(AOAAccessoryReceiver.ACTION_USB_PERMISSION), 0);
    }

    public void initParams() {
        numberProgress = 0;
        retryAccessoryMode = 0;
        retryNoPermission = 0;
    }

    public boolean scanUsbDevices() {
        if (mContext == null || mUsbManager == null || mPermissionIntent == null) {
            LogUtil.e(TAG, "scanUsbDevices fail");
            return false;
        }

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        LogUtil.d(TAG, "device count = " + deviceList.size());
        if (deviceList.size() == 0) {
            if (numberProgress > 0) {
                retryNoPermission++;
                if (retryNoPermission >= MAX_TIME_CONNECT_RETRY_NO_PERMISSION) {
                    LogUtil.d(TAG, "can't get devices again after change to AOA mode");
                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL, 0, 0, null);
                    retryNoPermission = 0;
                    return true;
                }
                return false;
            } else {
                retryNoPermission = 0;
            }
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

            if (filterDevice(device)) {
                LogUtil.d(TAG, "ignore non Android device");
                continue;
            }

            LogUtil.d(TAG, device.toString());
            if (numberProgress < 10) {
                if (ConnectClient.getInstance().isCarlifeConnecting()) {
                    LogUtil.d(TAG, "already connecting");
                    return false;
                }
                ConnectManager.getInstance().initConnectType(ConnectManager.CONNECTED_BY_AOA);
                ConnectClient.getInstance().setIsConnecting(true);
                numberProgress = 10;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            mUsbManager.requestPermission(device, mPermissionIntent);
            if (numberProgress < 20) {
                numberProgress = 20;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            if (initUsbDevice(device)) {
                LogUtil.d(TAG, "init device success");
                return true;
            }
        }
        return false;
    }

    public boolean filterDevice(UsbDevice device) {

        if (null == device) {
            return true;
        }

        if (device.getVendorId() == VID_IPHONE) {
            LogUtil.d(TAG, "the device is iPhone");
            return true;
        }

        if (CarlifeUtil.getInstance().isUsbStorageDevice(device)) {
            return true;
        }

        return false;
    }

    public boolean initUsbDevice(UsbDevice device) {
        LogUtil.d(TAG, "initUsbDevice");
        uninitUsbDevice();
        if (device == null) {
            LogUtil.e(TAG, "device is null, initUsbDevice fail");
            return false;
        }
        if (mUsbManager == null) {
            LogUtil.e(TAG, "mUsbManager is null, initUsbDevice fail");
            return false;
        }
        mUsbDevice = device;
        try {
            if (numberProgress < 30) {
                numberProgress = 30;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            mUsbInterface = mUsbDevice.getInterface(0);
            mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
            mUsbDeviceConnection.claimInterface(mUsbInterface, true);
        } catch (Exception ex) {
            LogUtil.e(TAG, "initUsbDevice fail");
            uninitUsbDevice();
            resetUsb();
            return false;
        }

        if (!isAccessoryMode()) {
            if (numberProgress < 40) {
                numberProgress = 40;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            changeToAccessoryMode();
            retryAccessoryMode++;
            if (retryAccessoryMode >= MAX_TIME_CONNECT_RETRY) {
                LogUtil.d(TAG, "can't change to accessory mode");
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_AOA_CHANGE_ACCESSORY_MODE, 0, 0, null);
                retryAccessoryMode = 0;
                return true;
            }
            return false;
        } else {
            retryAccessoryMode = 0;
            if (numberProgress < 50) {
                numberProgress = 50;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            LogUtil.d(TAG, "begin to read/write");
            mUsbEndpointIn = mUsbInterface.getEndpoint(BULK_ENDPOINT_IN_ADDRESS);
            mUsbEndpointOut = mUsbInterface.getEndpoint(BULK_ENDPOINT_OUT_ADDRESS);

            if (numberProgress < 60) {
                numberProgress = 60;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            AOAConnectManager.getInstance().startSocketReadThread();
            if (numberProgress < 80) {
                numberProgress = 80;
                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, numberProgress, 0,
                        null);
            }
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ConnectManager.getInstance().startAllConnectSocket();
            AOAConnectManager.getInstance().startAOAReadThread();
            numberProgress = 0;

            return true;
        }
    }

    public void uninitUsbDevice() {
        LogUtil.d(TAG, "uninitUsbDevice");
        try {
            if (mUsbDeviceConnection != null) {
                mUsbDeviceConnection.releaseInterface(mUsbInterface);
                mUsbDeviceConnection.close();
            }

            mUsbDevice = null;
            mUsbEndpointIn = null;
            mUsbEndpointOut = null;
            mUsbDeviceConnection = null;
        } catch (Exception ex) {
            LogUtil.e(TAG, "uninitUsbDevice fail");
        }
    }

    public int bulkTransferIn(byte[] data, int len) {
        int ret = -1;
        int cnt = len;
        int readLen = -1;
        int dataLen = 0;
        try {
            if (mUsbDeviceConnection == null || mUsbEndpointIn == null) {
                LogUtil.e(TAG, "mUsbDeviceConnection or mUsbEndpointIn is null");
                throw new IOException();
            }

            if (len <= AOA_MAX_BUFFER_BYTES) {
                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, data, len, TIME_OUT);
                if (ret < 0) {
                    LogUtil.e(TAG, "bulkTransferIn error 1: ret = " + ret);
                    throw new IOException();
                } else if (ret == 0) {
                    return 0;
                }
                dataLen = ret;
            } else {
                while (cnt > 0) {
                    readLen = cnt > AOA_MAX_BUFFER_BYTES ? AOA_MAX_BUFFER_BYTES : cnt;
                    ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, mDataBuffer, readLen, TIME_OUT);
                    if (ret < 0) {
                        LogUtil.e(TAG, "bulkTransferIn error 2: ret = " + ret);
                        throw new IOException();
                    } else if (ret == 0) {
                        continue;
                    }
                    System.arraycopy(mDataBuffer, 0, data, dataLen, ret);
                    cnt -= ret;
                    dataLen += ret;
                }
            }

            if (dataLen != len) {
                LogUtil.e(TAG, "bulkTransferIn error 3: dataLen = " + dataLen + ", len = " + len);
                ret = -1;
                throw new IOException();
            }
            return dataLen;
        } catch (Exception e) {
            LogUtil.e(TAG, "bulkTransferIn catch exception");
            ConnectClient.getInstance().setIsConnected(false);
            e.printStackTrace();
            return -1;
        }

    }

    public int bulkTransferOut(byte[] data, int len) {
        int ret = -1;
        int cnt = len;
        int readLen = -1;
        int dataLen = 0;
        try {
            if (mUsbDeviceConnection == null || mUsbEndpointOut == null) {
                LogUtil.e(TAG, "mUsbDeviceConnection or mUsbEndpointIn is null");
                throw new IOException();
            }

            if (len <= AOA_MAX_BUFFER_BYTES) {
                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, data, len, TIME_OUT);
                if (ret <= 0) {
                    LogUtil.e(TAG, "bulkTransferOut error 1: ret = " + ret);
                    throw new IOException();
                }
                dataLen = ret;
            } else {
                while (cnt > 0) {
                    readLen = cnt > AOA_MAX_BUFFER_BYTES ? AOA_MAX_BUFFER_BYTES : cnt;
                    System.arraycopy(data, dataLen, mDataBuffer, 0, readLen);
                    ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, mDataBuffer, readLen, TIME_OUT);
                    if (ret <= 0) {
                        LogUtil.e(TAG, "bulkTransferOut error 2: ret = " + ret);
                        throw new IOException();
                    }
                    cnt -= ret;
                    dataLen += ret;
                }
            }

            if (dataLen != len) {
                LogUtil.e(TAG, "bulkTransferOut error 3: dataLen = " + dataLen + ", len = " + len);
                ret = -1;
                throw new IOException();
            }
            return dataLen;
        } catch (Exception e) {
            LogUtil.e(TAG, "bulkTransferOut catch exception");
            ConnectClient.getInstance().setIsConnected(false);
            e.printStackTrace();
            return -1;
        }
    }

    public synchronized int bulkTransferOut(byte[] head, int lenHead, byte[] msg, int lenMsg) {
        if (bulkTransferOut(head, lenHead) < 0) {
            LogUtil.e(TAG, "bulkTransferOut fail 1");
            return -1;
        }
        if (bulkTransferOut(msg, lenMsg) < 0) {
            LogUtil.e(TAG, "bulkTransferOut fail 2");
            return -1;
        }
        return lenHead + lenMsg;
    }

    public int controlTransferIn(int request, int value, int index, byte[] buffer) {
        if (mUsbDeviceConnection == null) {
            return -1;
        }
        return mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_VENDOR, request,
                value, index, buffer, buffer == null ? 0 : buffer.length, TIME_OUT);
    }

    public int controlTransferOut(int request, int value, int index, byte[] buffer) {
        if (mUsbDeviceConnection == null) {
            return -1;
        }
        return mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR, request,
                value, index, buffer, buffer == null ? 0 : buffer.length, TIME_OUT);
    }

    public boolean isAccessoryMode() {
        LogUtil.d(TAG, "isAccessoryMode");
        boolean flag = false;
        if (mUsbDevice == null) {
            return false;
        }

        int vid = mUsbDevice.getVendorId();
        int pid = mUsbDevice.getProductId();

        if ((vid == VID_ACCESSORY)) {
            switch (pid) {
                case PID_ACCESSORY_ONLY:
                case PID_ACCESSORY_ADB:
                case PID_ACCESSORY_AUDIO:
                case PID_ACCESSORY_AUDIO_ADB:
                case PID_ACCESSORY_AUDIO_BULK:
                case PID_ACCESSORY_AUDIO_ADB_BULK:
                    flag = true;
                    break;
                default:
                    break;
            }
        }
        if (flag) {
            LogUtil.e(TAG, "Android device attached in Accessory Mode");
        } else {
            LogUtil.e(TAG, "Android device attached not in Accessory Mode");
        }

        return flag;
    }

    public boolean changeToAccessoryMode() {
        LogUtil.d(TAG, "changeToAccessoryMode");
        if (mUsbDevice == null) {
            return false;
        }
        if (!getProtocolVersion()) {
            LogUtil.e(TAG, "Change Accessory Mode getProtocolVersion Fail");
            return false;
        }
        if (!sendIdentityStrings()) {
            LogUtil.e(TAG, "Change Accessory Mode sendIdentityStrings Fail");
            return false;
        }
        if (!startAccessoryMode()) {
            LogUtil.e(TAG, "Change Accessory Mode startAccessoryMode Fail");
            return false;
        }
        LogUtil.e(TAG, "Change Accessory Mode Success");
        return true;
    }

    public boolean getProtocolVersion() {
        byte[] buffer = new byte[2];
        if (controlTransferIn(AOA_GET_PROTOCOL, 0, 0, buffer) < 0) {
            LogUtil.d(TAG, "get protocol version fail");
            return false;
        }

        int version = buffer[1] << 8 | buffer[0];
        if (version < 1 || version > 2) {
            LogUtil.e(TAG, "usb device not capable of AOA 1.0 or 2.0, version = " + version);
            return false;
        }
        LogUtil.e(TAG, "usb device AOA version is " + version);
        return true;
    }

    public boolean sendIdentityStrings() {
        if (controlTransferOut(AOA_SEND_IDENT, 0, 0, AOA_MANUFACTURER.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_MANUFACTURER fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 1, AOA_MODEL_NAME.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_MODEL_NAME fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 2, AOA_DESCRIPTION.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_DESCRIPTION fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 3, AOA_VERSION.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_VERSION fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 4, AOA_URI.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_URI fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 5, AOA_SERIAL_NUMBER.getBytes()) < 0) {
            LogUtil.d(TAG, "send identity AOA_SERIAL_NUMBER fail");
            return false;
        }
        LogUtil.e(TAG, "send indentity string success");
        return true;
    }

    public boolean startAccessoryMode() {
        if (controlTransferOut(AOA_START_ACCESSORY, 0, 0, null) < 0) {
            LogUtil.d(TAG, "start accessory mode fail");
            return false;
        }
        LogUtil.e(TAG, "start accessory mode success");
        return true;
    }

    public void resetUsb() {
        LogUtil.d(TAG, "reset usb");
        LogUtil.d(TAG, "reset usb by cmd");
        ConnectNcmDriverClient.getInstance().writeDataToDriver(ConnectNcmDriverClient.USB_RESET);
    }

    private String getTestCarlifeCmdMessage() {
        if (TEST_MSG_NUM >= TOTAL_TEST_MSG_NUM) {
            return null;
        }
        char c = (char) ('a' + (TEST_MSG_NUM % 26));

        String ts = "Msg Num:" + Integer.toString(TEST_MSG_NUM);
        int len = TEST_MAX_BUFFER_BYTES - ts.length();
        StringBuffer sb = new StringBuffer(ts);
        for (int j = 0; j < len; ++j) {
            sb.append(c);
        }
        ++TEST_MSG_NUM;
        return sb.substring(0);
    }

    private void startWriteThread() {
        mWriteThread = new Thread() {
            public void run() {
                int cnt = -1;
                int ret = -1;
                String str = null;
                byte[] data = null;
                long mStartTime = SystemClock.elapsedRealtime();
                while (true) {
                    try {
                        str = getTestCarlifeCmdMessage();
                        if (str == null) {
                            break;
                        }
                        LogUtil.d(TAG, "write data: " + str.substring(0, 20));
                        data = str.getBytes();
                        ret = bulkTransferOut(data, TEST_MAX_BUFFER_BYTES);
                        if (ret != TEST_MAX_BUFFER_BYTES) {
                            LogUtil.e(TAG, "write data error, ret = " + ret);
                            return;
                        }
                        ++cnt;
                    } catch (Exception e) {
                        LogUtil.e(TAG, "get exception when write");
                        LogUtil.e(TAG, e.toString());
                        e.printStackTrace();
                        break;
                    }
                }
                long mEndTime = SystemClock.elapsedRealtime();
                LogUtil.e(TAG, "Cnt = " + cnt);
                LogUtil.e(TAG, "Write Time = " + (mEndTime - mStartTime));
            }
        };

        mWriteThread.setName("WriteThread");
        mWriteThread.start();
    }

    private void startReadThread() {
        mReadThread = new Thread() {
            public void run() {
                try {
                    LogUtil.e(TAG, "sleep 1s before read...");
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] data = new byte[TEST_MAX_BUFFER_BYTES];
                int ret = -1;
                int cnt = -1;
                String str = null;
                long mStartTime = SystemClock.elapsedRealtime();
                while (true) {
                    try {
                        ret = bulkTransferIn(data, TEST_MAX_BUFFER_BYTES);
                        if (ret != TEST_MAX_BUFFER_BYTES) {
                            LogUtil.e(TAG, "read data error, ret = " + ret);
                            return;
                        }
                        ++cnt;
                        if (cnt >= TOTAL_TEST_MSG_NUM) {
                            long mEndTime = SystemClock.elapsedRealtime();
                            LogUtil.e(TAG, "Read Time = " + (mEndTime - mStartTime));
                        }
                        str = new String(data);
                        LogUtil.d(TAG, "read data:" + cnt + ":" + ret + ":" + str.substring(0, 20));
                    } catch (Exception e) {
                        LogUtil.e(TAG, "get exception when read");
                        LogUtil.e(TAG, e.toString());
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };

        mReadThread.setName("ReadThread");
        mReadThread.start();
    }

}
