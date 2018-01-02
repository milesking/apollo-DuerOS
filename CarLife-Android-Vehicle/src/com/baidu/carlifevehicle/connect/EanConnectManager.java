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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.util.ByteConvert;
import com.baidu.carlifevehicle.util.LogUtil;

/**
 * Eap Connection Manager
 */
public class EanConnectManager {

    private static final String TAG = "EanConnectManager";
    private static final String EAN_READ_THREAD_NAME = "eanReadThread";
    private static final String EAN_CONNECT_THREAD_NAME = "eanConnectThread";
    private static final String SOCKET_READ_THREAD_NAME = "eanSocketReadThread";
    public static final int SLEEP_TIME_500MS = 500;

    private static final int NAGATIVE = -1;
    private static final int LEN_OF_MSG_HEAD = 8;
    private static final int SEND_BUFFER_SIZE = 320 * 1024;
    private static final int RECEIVE_BUFFER_SIZE = 320 * 1024;
    private static final int EAN_BULK_BUFFER_SIZE = 4096;
    private static final int CLIFE_DATA_BUFFER_SIZE = 64 * 1024;
    private static final int CLIFE_DATA_BUFFER_MAX_SIZE = 500 * 1024;
    private volatile int eaNativeFd = -1;
    private static final int ENODATA = 61;
    private static final int ENOTCONN = 107;

    private static EanConnectManager mInstance = null;

    private EanReadThread mEanReadThread = null;
    private EanConnectThread mEanConnectThread = null;

    private SocketReadThread mSocketReadThread = null;
    private SocketReadThread mSocketReadVideoThread = null;
    private SocketReadThread mSocketReadAudioThread = null;
    private SocketReadThread mSocketReadAudioTTSThread = null;
    private SocketReadThread mSocketReadAudioVRThread = null;
    private SocketReadThread mSocketReadTouchThread = null;

    public static EanConnectManager getInstance() {
        if (null == mInstance) {
            synchronized (EanConnectManager.class) {
                if (null == mInstance) {
                    mInstance = new EanConnectManager();
                }
            }
        }
        return mInstance;
    }

    private EanConnectManager() {

    }

    public boolean eanInit() {
        boolean result = false;
        int devStatus = NAGATIVE;
        devStatus = JniEanMethod.openEan();
        if (NAGATIVE == devStatus) {
            LogUtil.e(TAG, "Open ean port /dev/mfi-ean failed devStatus = " + devStatus);
        } else {
            result = true;
            LogUtil.e(TAG, "Open ean port /dev/mfi-ean success");
        }
        eaNativeFd = devStatus;
        return result;

    }

    public int eaOpenSessionState() {
        int result = -1;
        int status = -1;
        status = JniEanMethod.eanIoctl();
        if (0 <= status) {
            LogUtil.e(TAG, "ea Open Session: status =" + status);
            result = status;
        } else {
            LogUtil.e(TAG, "ea Open Session failed!: status =" + status);
        }
        return result;
    }

    public void eanUinit() {
        LogUtil.d(TAG, "uninit");
        eaNativeFd = NAGATIVE;
        stopEanReadThread();
        stopSocketReadThread();
        JniEanMethod.closeEan();
        LogUtil.e(TAG, "Close ean port /dev/mfi-ean");
    }

    public synchronized int eanDataWrite(byte[] head, int lenHead, byte[] msg, int lenMsg) {
        int dataLength = 0;
        try {
            if (JniEanMethod.eanWrite(head, lenHead) < 0) {
                LogUtil.e(TAG, "ean write head fail");
                throw new IOException();
            }
            while (0 < lenMsg) {
                if (EAN_BULK_BUFFER_SIZE < lenMsg) {
                    dataLength = JniEanMethod.eanWrite(msg, EAN_BULK_BUFFER_SIZE);
                    LogUtil.d(TAG, "ean write data1 lenMsg = " + lenMsg + "dataLength = " + dataLength);
                    if (0 < dataLength) {
                        lenMsg = lenMsg - dataLength;
                        System.arraycopy(msg, dataLength, msg, 0, lenMsg);
                    } else {
                        if (0 > dataLength) {
                            LogUtil.e(TAG, "ean write msg retrun -1");
                            throw new IOException();
                        }
                    }

                } else {
                    dataLength = JniEanMethod.eanWrite(msg, lenMsg);
                    LogUtil.d(TAG, "ean write data2 lenMsg = " + lenMsg);
                    if (0 < dataLength) {
                        lenMsg = lenMsg - dataLength;
                        if (0 < lenMsg) {
                            System.arraycopy(msg, dataLength, msg, 0, lenMsg);
                        }
                    } else {
                        if (0 > dataLength) {
                            LogUtil.e(TAG, "ean write msg retrun -1");
                            throw new IOException();
                        }
                    }
                }

            }
            return lenHead + lenMsg;
        } catch (Exception e) {
            LogUtil.e(TAG, "IOException, ean write msg fail");
            e.printStackTrace();
            return -1;
        }
    }

    public void startEanConnectThread() {
        try {
            mEanConnectThread = new EanConnectThread();
            mEanConnectThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "Start eanConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void stopEanConnectThread() {
        LogUtil.d(TAG, "stopEanConnectThread");
        try {
            if (null != mEanConnectThread) {
                mEanConnectThread.cancel();
                mEanConnectThread = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Stop eanConnectThread Fail");
            e.printStackTrace();
        }
    }

    private class EanConnectThread extends Thread {
        private boolean isRunning = false;

        public EanConnectThread() {
            LogUtil.d(TAG, "eanConnectThread Created");
            setName(EAN_CONNECT_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            isRunning = true;
            boolean devStatus = false;
            int eaStatus = -1;
            LogUtil.d(TAG, "Begin to connect carlife by EAN");
            try {
                if (!isRunning) {
                    LogUtil.e(TAG, "Carlife Connect Cancled");
                    return;
                }
                ConnectNcmDriverClient.getInstance().writeDataToDriver(ConnectNcmDriverClient.PULL_UP_CARLIFE);
                sleep(SLEEP_TIME_500MS * 2);
                if (NAGATIVE == eaNativeFd) {
                    devStatus = eanInit();
                    if (!devStatus) {
                        return;
                    }
                    eaStatus = eaOpenSessionState();
                    if ((-1 == eaStatus) || (ENODATA == eaStatus) || (ENOTCONN == eaStatus)) {
                        LogUtil.e(TAG, "do not open session eaStatus = " + eaStatus);
                        JniEanMethod.closeEan();
                        eaNativeFd = NAGATIVE;
                        return;
                    }
                    if (ConnectClient.getInstance().isCarlifeConnecting()) {
                        LogUtil.d(TAG, "already connecting");
                        return;
                    }
                    sleep(SLEEP_TIME_500MS * 2);
                    if (!isRunning) {
                        LogUtil.e(TAG, "Carlife Connect Cancled 01");
                        return;
                    }
                    ConnectClient.getInstance().setIsConnecting(true);
                    ConnectManager.getInstance().initConnectType(ConnectManager.CONNECTED_BY_EAN);
                    startEanReadThread();
                    startSocketReadThread();
                    ConnectManager.getInstance().startAllConnectSocket();
                } else {
                    LogUtil.e(TAG, "EAN device file had opened");
                }

            } catch (Exception e) {
                LogUtil.e(TAG, "Exception when connect carlife by EAN");
                e.printStackTrace();
            }
        }
    }

    public void startSocketReadThread() {
        try {
            mSocketReadThread = new SocketReadThread(CommonParams.SOCKET_WIFI_PORT, CommonParams.SERVER_SOCKET_NAME);
            mSocketReadThread.start();

            mSocketReadVideoThread =
                    new SocketReadThread(CommonParams.SOCKET_VIDEO_WIFI_PORT, CommonParams.SERVER_SOCKET_VIDEO_NAME);
            mSocketReadVideoThread.start();

            mSocketReadAudioThread =
                    new SocketReadThread(CommonParams.SOCKET_AUDIO_WIFI_PORT, CommonParams.SERVER_SOCKET_AUDIO_NAME);
            mSocketReadAudioThread.start();

            mSocketReadAudioTTSThread =
                    new SocketReadThread(CommonParams.SOCKET_AUDIO_TTS_WIFI_PORT,
                            CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME);
            mSocketReadAudioTTSThread.start();

            mSocketReadAudioVRThread =
                    new SocketReadThread(CommonParams.SOCKET_AUDIO_VR_WIFI_PORT,
                            CommonParams.SERVER_SOCKET_AUDIO_VR_NAME);
            mSocketReadAudioVRThread.start();

            mSocketReadTouchThread =
                    new SocketReadThread(CommonParams.SOCKET_TOUCH_WIFI_PORT, CommonParams.SERVER_SOCKET_TOUCH_NAME);
            mSocketReadTouchThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "Start SocketRead Thread Fail");
            e.printStackTrace();
        }
    }

    public void stopSocketReadThread() {
        try {
            if (null != mSocketReadThread) {
                mSocketReadThread.cancel();
                mSocketReadThread = null;
            }

            if (null != mSocketReadVideoThread) {
                mSocketReadVideoThread.cancel();
                mSocketReadVideoThread = null;
            }

            if (null != mSocketReadAudioThread) {
                mSocketReadAudioThread.cancel();
                mSocketReadAudioThread = null;
            }

            if (null != mSocketReadAudioTTSThread) {
                mSocketReadAudioTTSThread.cancel();
                mSocketReadAudioTTSThread = null;
            }

            if (null != mSocketReadAudioVRThread) {
                mSocketReadAudioVRThread.cancel();
                mSocketReadAudioVRThread = null;
            }

            if (null != mSocketReadTouchThread) {
                mSocketReadTouchThread.cancel();
                mSocketReadTouchThread = null;
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "Stop SocketRead Thread Fail");
            e.printStackTrace();
        }
    }

    private class SocketReadThread extends Thread {
        private ServerSocket mServerSocket = null;
        private boolean isRunning = false;
        private int mSocketPort = -1;
        private String mSocketName = null;
        private String mThreadName = null;

        private Socket mSocket = null;
        private BufferedInputStream mInputStream = null;
        private BufferedOutputStream mOutputStream = null;

        private int lenMsgHead = -1;
        private int lenMsgData = -1;
        private int lenMsg = -1;

        private byte[] msg = new byte[CommonParams.MSG_VIDEO_HEAD_SIZE_BYTE];
        private byte[] head = new byte[LEN_OF_MSG_HEAD];

        public SocketReadThread(int port, String name) {
            try {
                mThreadName = name + SOCKET_READ_THREAD_NAME;
                setName(mThreadName);
                LogUtil.d(TAG, "Create " + mThreadName);

                mSocketPort = port;
                mSocketName = name;
                mServerSocket = new ServerSocket(mSocketPort);
                isRunning = true;

                if (mSocketName.equals(CommonParams.SERVER_SOCKET_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID), 0, head, 0, 4);
                } else if (mSocketName.equals(CommonParams.SERVER_SOCKET_VIDEO_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID_VIDEO), 0, head, 0, 4);
                } else if (mSocketName.equals(CommonParams.SERVER_SOCKET_AUDIO_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID_AUDIO), 0, head, 0, 4);
                } else if (mSocketName.equals(CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID_AUDIO_TTS), 0, head, 0, 4);
                } else if (mSocketName.equals(CommonParams.SERVER_SOCKET_AUDIO_VR_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID_AUDIO_VR), 0, head, 0, 4);
                } else if (mSocketName.equals(CommonParams.SERVER_SOCKET_TOUCH_NAME)) {
                    System.arraycopy(ByteConvert.intToBytes(CommonParams.MSG_CHANNEL_ID_TOUCH), 0, head, 0, 4);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Create " + mThreadName + " fail");
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                if (null != mServerSocket) {
                    mServerSocket.close();
                }
                if (null != mSocket) {
                    mSocket.close();
                    mSocket = null;
                }
                if (null != mInputStream) {
                    mInputStream.close();
                    mInputStream = null;
                }
                if (null != mOutputStream) {
                    mOutputStream.close();
                    mOutputStream = null;
                }

                isRunning = false;
            } catch (Exception e) {
                LogUtil.e(TAG, "Close " + mThreadName + " fail");
                e.printStackTrace();
            }
        }

        public int readData(byte[] buffer, int offset, int len) {
            int r = -1;
            try {
                if (null != mInputStream) {
                    int cnt;

                    cnt = len;
                    int dataLen = 0;
                    while (cnt > 0) {
                        r = mInputStream.read(buffer, offset + dataLen, cnt);
                        if (r > 0) {
                            cnt -= r;
                            dataLen += r;
                        } else {
                            LogUtil.e(TAG, mSocketName + " Receive Data Error: ret = " + r);
                            throw new IOException();
                        }
                    }
                    if (dataLen != len) {
                        LogUtil.e(TAG, mSocketName + " Receive Data Error: dataLen = " + dataLen);
                        throw new IOException();
                    }
                    return dataLen;
                } else {
                    LogUtil.e(TAG, mSocketName + " Receive Data Fail, mInputStream is null");
                    throw new IOException();
                }
            } catch (Exception e) {
                LogUtil.e(TAG, mSocketName + " IOException, Receive Data Fail");
                ConnectClient.getInstance().setIsConnected(false);
                e.printStackTrace();
                return r;
            }
        }

        public int writeData(byte[] buffer, int offset, int len) {
            try {
                if (null != mOutputStream) {
                    mOutputStream.write(buffer, offset, len);
                    mOutputStream.flush();
                    return len;
                } else {
                    LogUtil.e(TAG, mSocketName + " Send Data Fail, mOutputStream is null");
                    throw new IOException();
                }
            } catch (Exception e) {
                LogUtil.e(TAG, mSocketName + " IOException, Send Data Fail");
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        public void run() {
            LogUtil.d(TAG, "Begin to listen in " + mThreadName);
            try {
                if (null != mServerSocket && isRunning) {
                    mSocket = mServerSocket.accept();
                    if (null == mSocket) {
                        LogUtil.d(TAG, "One client connected fail: " + mThreadName);
                    }
                    LogUtil.d(TAG, "One client connected in " + mThreadName);
                    mSocket.setTcpNoDelay(true);
                    mSocket.setSendBufferSize(SEND_BUFFER_SIZE);
                    mSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);

                    mInputStream = new BufferedInputStream(mSocket.getInputStream());
                    mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Get Exception in " + mThreadName);
                e.printStackTrace();
                return;
            }
            try {
                while (mSocket != null && isRunning) {
                    if (!mSocket.isConnected()) {
                        LogUtil.e(TAG, "socket is disconnected when read data");
                        break;
                    }

                    if (mSocketName.equals(CommonParams.SERVER_SOCKET_NAME)
                            || mSocketName.equals(CommonParams.SERVER_SOCKET_TOUCH_NAME)) {
                        if (readData(msg, 0, CommonParams.MSG_CMD_HEAD_SIZE_BYTE) < 0) {
                            break;
                        }
                        lenMsgHead = CommonParams.MSG_CMD_HEAD_SIZE_BYTE;
                        lenMsgData = (int) ByteConvert.bytesToShort(new byte[] { msg[0], msg[1] });
                    } else {
                        if (readData(msg, 0, CommonParams.MSG_VIDEO_HEAD_SIZE_BYTE) < 0) {
                            break;
                        }
                        lenMsgHead = CommonParams.MSG_VIDEO_HEAD_SIZE_BYTE;
                        lenMsgData = ByteConvert.bytesToInt(new byte[] { msg[0], msg[1], msg[2], msg[3] });
                    }
                    LogUtil.d(TAG, "Channel = " + mSocketName + ", lenMsgHead = " + lenMsgHead + ", lenMsgData = "
                            + lenMsgData);
                    System.arraycopy(ByteConvert.intToBytes(lenMsgHead + lenMsgData), 0, head, 4, 4);

                    lenMsg = lenMsgHead + lenMsgData;
                    if (msg.length < lenMsg) {
                        byte[] tmpMsg = msg;
                        msg = new byte[lenMsg];
                        System.arraycopy(tmpMsg, 0, msg, 0, lenMsgHead);
                    }
                    if (readData(msg, lenMsgHead, lenMsgData) < 0) {
                        LogUtil.e(TAG, "read len msg data fail");
                        break;
                    }
                    if (eanDataWrite(head, LEN_OF_MSG_HEAD, msg, lenMsg) < 0) {
                        LogUtil.e(TAG, "bulkTransferOut fail");
                        break;
                    }
                }

            } catch (Exception ex) {
                LogUtil.e(TAG, "get Exception in ReadThread");
                ex.printStackTrace();
            }
        }
    }

    public void startEanReadThread() {
        try {
            mEanReadThread = new EanReadThread();
            mEanReadThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "Start eanReadThread Fail");
            e.printStackTrace();
        }
    }

    public void stopEanReadThread() {
        try {
            if (null != mEanReadThread) {
                mEanReadThread.cancel();
                mEanReadThread = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Stop eanReadThread Fail");
            e.printStackTrace();
        }
    }

    private class EanReadThread extends Thread {
        private boolean isRunning = false;
        private byte[] eanData = new byte[EAN_BULK_BUFFER_SIZE];
        private byte[] clifeData = new byte[CLIFE_DATA_BUFFER_SIZE];
        private int flag = 0;
        private int clifeDataIndex = 0;
        private int typeMsg = -1;
        private int lenMsg = -1;

        public EanReadThread() {
            LogUtil.d(TAG, "Ean ReadThread Created");
            setName(EAN_READ_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        public void writeDataToSocket() {
            LogUtil.d(TAG, "write data to socket lenMsg = " + lenMsg);
            switch (typeMsg) {
                case CommonParams.MSG_CHANNEL_ID:
                    mSocketReadThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                case CommonParams.MSG_CHANNEL_ID_VIDEO:
                    mSocketReadVideoThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                case CommonParams.MSG_CHANNEL_ID_AUDIO:
                    mSocketReadAudioThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                case CommonParams.MSG_CHANNEL_ID_AUDIO_TTS:
                    mSocketReadAudioTTSThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                case CommonParams.MSG_CHANNEL_ID_AUDIO_VR:
                    mSocketReadAudioVRThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                case CommonParams.MSG_CHANNEL_ID_TOUCH:
                    mSocketReadTouchThread.writeData(clifeData, LEN_OF_MSG_HEAD, lenMsg);
                    break;
                default:
                    LogUtil.e(TAG, "eanReadThread typeMsg error");
                    break;
            }
        }

        public void analyzeData(int dataLen) {
            int dataLength = 0;
            System.arraycopy(eanData, 0, clifeData, clifeDataIndex, dataLen);
            clifeDataIndex += dataLen;
            while (true) {
                if (0 == flag) {
                    if (clifeDataIndex >= LEN_OF_MSG_HEAD) {
                        typeMsg =
                                ByteConvert.bytesToInt(new byte[] { clifeData[0], clifeData[1], clifeData[2],
                                        clifeData[3] });
                        lenMsg =
                                ByteConvert.bytesToInt(new byte[] { clifeData[4], clifeData[5], clifeData[6],
                                        clifeData[7] });

                        LogUtil.d(TAG, "There is msg head typeMsg = " + typeMsg + "lenMsg =" + lenMsg);
                        dataLength = lenMsg + LEN_OF_MSG_HEAD + EAN_BULK_BUFFER_SIZE + 1;

                        if (dataLength > clifeData.length) {
                            try {
                                if ((dataLength > CLIFE_DATA_BUFFER_MAX_SIZE)
                                        || (dataLength < 0)) {
                                    throw new IOException();
                                }
                                byte[] clifeDataBak = clifeData;
                                clifeData = new byte[dataLength];
                                LogUtil.d(TAG, "clifeDataBak length = " + clifeDataBak.length + "clifeData length = "
                                        + clifeData.length);
                                System.arraycopy(clifeDataBak, 0, clifeData, 0, clifeDataBak.length);
                            } catch (Exception e) {
                                LogUtil.e(TAG, "data is wrong");
                                ConnectClient.getInstance().setIsConnected(false);
                                e.printStackTrace();
                            }
                        }
                        flag = 1;
                    } else {
                        break;
                    }
                }
                if (1 == flag) {
                    if ((clifeDataIndex - LEN_OF_MSG_HEAD) >= lenMsg) {
                        int len = 0;
                        writeDataToSocket();
                        len = clifeDataIndex - LEN_OF_MSG_HEAD - lenMsg;
                        if (0 == len) {
                            LogUtil.d(TAG, "There is no data in clifeData ");
                            Arrays.fill(clifeData, (byte) 0);
                        } else {
                            System.arraycopy(clifeData, (LEN_OF_MSG_HEAD + lenMsg), clifeData, 0, len);
                        }
                        clifeDataIndex = len;
                        flag = 0;
                    } else {
                        break;
                    }
                }
                LogUtil.d(TAG, "analyzeData 02 flag = " + flag);
            }
        }

        @Override
        public void run() {
            isRunning = true;
            int eanDataLen = 0;
            int devStatus = NAGATIVE;
            Arrays.fill(eanData, (byte) 0);
            LogUtil.d(TAG, "Begin to read data by EAN");
            try {
                while (isRunning) {
                    if (!isRunning) {
                        LogUtil.e(TAG, "read data cancled");
                        return;
                    }
                    eanDataLen = JniEanMethod.eanRead(eanData, EAN_BULK_BUFFER_SIZE);
                    if (eanDataLen < 0) {
                        JniEanMethod.closeEan();
                        devStatus = JniEanMethod.openEan();
                        if (NAGATIVE == devStatus) {
                            LogUtil.e(TAG, "read thread Open ean port /dev/mfi-ean failed devStatus = " + devStatus);
                            isRunning = false;
                            ConnectClient.getInstance().setIsConnected(false);
                        } else {
                            LogUtil.e(TAG, "read thread Open ean port /dev/mfi-ean success");
                        }
                    } else if (eanDataLen == 0) {
                        continue;
                    }
                    if (ConnectClient.getInstance().isCarlifeConnected()) {
                        analyzeData(eanDataLen);
                    }
                    Arrays.fill(eanData, (byte) 0);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Exception when read data by EAN");
                ConnectClient.getInstance().setIsConnected(false);
                e.printStackTrace();
            }
        }
    }
}
