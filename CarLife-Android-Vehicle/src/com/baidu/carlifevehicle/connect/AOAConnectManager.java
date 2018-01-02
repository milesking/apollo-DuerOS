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
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.util.ByteConvert;
import com.baidu.carlifevehicle.util.LogUtil;
import android.content.Context;

/**
 * AOA Connection Manager, Responsible for the AOA connection process.
 */
public class AOAConnectManager {

    private static final String TAG = "AOAConnectManager";
    private static AOAConnectManager mInstance = null;
    private Context mContext = null;
    private static final String AOA_CONNECT_THREAD_NAME = "AOAConnectThread";
    private static final String AOA_READ_THREAD_NAME = "AOAReadThread";
    private static final String SOCKET_READ_THREAD_NAME = "SocketReadThread";
    private static final int SLEEP_TIME_MS = 1000;
    private static final int LEN_OF_MSG_HEAD = 8;

    private static final int SEND_BUFFER_SIZE = 320 * 1024;
    private static final int RECEIVE_BUFFER_SIZE = 320 * 1024;
    
    private static final int AOA_MAX_BYTES = 64 * 1024 * 1024;

    private AOAConnectThread mAOAConnectThread = null;
    private AOAReadThread mAOAReadThread = null;

    private SocketReadThread mSocketReadThread = null;
    private SocketReadThread mSocketReadVideoThread = null;
    private SocketReadThread mSocketReadAudioThread = null;
    private SocketReadThread mSocketReadAudioTTSThread = null;
    private SocketReadThread mSocketReadAudioVRThread = null;
    private SocketReadThread mSocketReadTouchThread = null;

    public static AOAConnectManager getInstance() {
        if (null == mInstance) {
            synchronized (AOAConnectManager.class) {
                if (null == mInstance) {
                    mInstance = new AOAConnectManager();
                }
            }
        }
        return mInstance;
    }

    private AOAConnectManager() {
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mContext = context;

        AOAHostSetup.getInstance().init(mContext);
    }

    public void uninit() {
        LogUtil.d(TAG, "uninit");
        stopAOAReadThread();
        stopSocketReadThread();
        AOAHostSetup.getInstance().uninitUsbDevice();
        if (ConnectManager.getInstance().getConnectType() == ConnectManager.CONNECTED_BY_AOA) {
            AOAHostSetup.getInstance().resetUsb();
        }
    }

    public void startAOAConnectThread() {
        try {
            mAOAConnectThread = new AOAConnectThread();
            mAOAConnectThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "Start AOAConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void stopAOAConnectThread() {
        LogUtil.d(TAG, "stopAOAConnectThread");
        try {
            if (null != mAOAConnectThread) {
                mAOAConnectThread.cancel();
                mAOAConnectThread = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Stop AOAConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void startAOAReadThread() {
        try {
            mAOAReadThread = new AOAReadThread();
            mAOAReadThread.start();
        } catch (Exception e) {
            LogUtil.e(TAG, "Start AOAReadThread Fail");
            e.printStackTrace();
        }
    }

    public void stopAOAReadThread() {
        try {
            if (null != mAOAReadThread) {
                mAOAReadThread.cancel();
                mAOAReadThread = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Stop AOAReadThread Fail");
            e.printStackTrace();
        }
    }

    public void startSocketReadThread() {
        try {
            mSocketReadThread =
                    new SocketReadThread(CommonParams.SOCKET_LOCALHOST_PORT, CommonParams.SERVER_SOCKET_NAME);
            mSocketReadThread.start();

            mSocketReadVideoThread = new SocketReadThread(CommonParams.SOCKET_VIDEO_LOCALHOST_PORT,
                    CommonParams.SERVER_SOCKET_VIDEO_NAME);
            mSocketReadVideoThread.start();

            mSocketReadAudioThread = new SocketReadThread(CommonParams.SOCKET_AUDIO_LOCALHOST_PORT,
                    CommonParams.SERVER_SOCKET_AUDIO_NAME);
            mSocketReadAudioThread.start();

            mSocketReadAudioTTSThread = new SocketReadThread(CommonParams.SOCKET_AUDIO_TTS_LOCALHOST_PORT,
                    CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME);
            mSocketReadAudioTTSThread.start();

            mSocketReadAudioVRThread = new SocketReadThread(CommonParams.SOCKET_AUDIO_VR_LOCALHOST_PORT,
                    CommonParams.SERVER_SOCKET_AUDIO_VR_NAME);
            mSocketReadAudioVRThread.start();

            mSocketReadTouchThread = new SocketReadThread(CommonParams.SOCKET_TOUCH_LOCALHOST_PORT,
                    CommonParams.SERVER_SOCKET_TOUCH_NAME);
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

    private class AOAConnectThread extends Thread {
        private boolean isRunning = false;

        public AOAConnectThread() {
            LogUtil.d(TAG, "AOAConnectThread Created");
            setName(AOA_CONNECT_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            isRunning = true;
            LogUtil.d(TAG, "Begin to connect carlife by AOA");
            AOAHostSetup.getInstance().initParams();
            try {
                while (true) {
                    if (!isRunning) {
                        LogUtil.e(TAG, "Carlife Connect Cancled");
                        return;
                    }
                    if (AOAHostSetup.getInstance().scanUsbDevices()) {
                        break;
                    }
                    sleep(SLEEP_TIME_MS);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Exception when connect carlife by AOA");
                e.printStackTrace();
            }
        }
    }

    private class AOAReadThread extends Thread {
        private boolean isRunning = false;

        private byte[] msg = new byte[LEN_OF_MSG_HEAD];
        private byte[] msgHead = new byte[LEN_OF_MSG_HEAD];
        private int typeMsg = -1;
        private int lenMsg = -1;
        private int ret = -1;

        public AOAReadThread() {
            LogUtil.d(TAG, "ReadThread Created");
            setName(AOA_READ_THREAD_NAME);
        }

        public void cancel() {
            isRunning = false;
        }

        @Override
        public void run() {
            isRunning = true;
            LogUtil.d(TAG, "Begin to read data by AOA");
            try {
                while (isRunning) {
                    if (!isRunning) {
                        LogUtil.e(TAG, "read data cancled");
                        return;
                    }

                    ret = AOAHostSetup.getInstance().bulkTransferIn(msgHead, LEN_OF_MSG_HEAD);
                    if (ret < 0) {
                        LogUtil.e(TAG, "bulkTransferIn fail 1");
                        break;
                    } else if (ret == 0) {
                        continue;
                    }
                    typeMsg = ByteConvert.bytesToInt(new byte[] { msgHead[0], msgHead[1], msgHead[2], msgHead[3] });
                    lenMsg = ByteConvert.bytesToInt(new byte[] { msgHead[4], msgHead[5], msgHead[6], msgHead[7] });
                    LogUtil.d(TAG, "typeMsg = " + typeMsg + ", lenMsg = " + lenMsg);
                    
                    if (typeMsg < 1 || typeMsg > 6 || lenMsg < 8 || lenMsg > AOA_MAX_BYTES) {
                        LogUtil.e(TAG, "typeMsg or lenMsg is error");
                        ConnectClient.getInstance().setIsConnected(false);
                        break;
                    }

                    if (msg.length < lenMsg) {
                        msg = new byte[lenMsg];
                    }

                    if (AOAHostSetup.getInstance().bulkTransferIn(msg, lenMsg) < 0) {
                        LogUtil.e(TAG, "bulkTransferIn fail 2");
                        break;
                    }

                    switch (typeMsg) {
                        case CommonParams.MSG_CHANNEL_ID:
                            mSocketReadThread.writeData(msg, 0, lenMsg);
                            break;
                        case CommonParams.MSG_CHANNEL_ID_VIDEO:
                            mSocketReadVideoThread.writeData(msg, 0, lenMsg);
                            break;
                        case CommonParams.MSG_CHANNEL_ID_AUDIO:
                            mSocketReadAudioThread.writeData(msg, 0, lenMsg);
                            break;
                        case CommonParams.MSG_CHANNEL_ID_AUDIO_TTS:
                            mSocketReadAudioTTSThread.writeData(msg, 0, lenMsg);
                            break;
                        case CommonParams.MSG_CHANNEL_ID_AUDIO_VR:
                            mSocketReadAudioVRThread.writeData(msg, 0, lenMsg);
                            break;
                        case CommonParams.MSG_CHANNEL_ID_TOUCH:
                            mSocketReadTouchThread.writeData(msg, 0, lenMsg);
                            break;
                        default:
                            LogUtil.e(TAG, "AOAReadThread typeMsg error");
                            break;
                    }
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Exception when read data by AOA");
                e.printStackTrace();
            }
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
                        break;
                    }

                    if (AOAHostSetup.getInstance().bulkTransferOut(head, LEN_OF_MSG_HEAD, msg, lenMsg) < 0) {
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

}
