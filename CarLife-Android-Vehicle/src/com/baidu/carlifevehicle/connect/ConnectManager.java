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

import android.content.Context;
import android.os.Message;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;

import com.baidu.carlife.protobuf.CarlifeModuleStatusProto.CarlifeModuleStatus;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.ErrorCodeReport;
import com.baidu.carlifevehicle.util.ErrorCodeType;
import com.baidu.carlifevehicle.util.LogUtil;
import com.baidu.carlifevehicle.util.PreferenceUtil;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Connection Manager
 */
public class ConnectManager {

    public static final String TAG = "ConnectManager";
    private static ConnectManager mInstance = null;
    private Context mContext = null;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private int numOfHeartBeat = 0;
    private int oldNumOfHeartBeat = -1;
    public static final int HEART_BEAT_CHECK_MS = 1000;
    private int mBeatCount = 0;

    public static final int CONNECTED_BY_AOA = 0x0002;
    public static final int CONNECTED_BY_NCM_ANDROID = 0x0003;
    public static final int CONNECTED_BY_NCM_IOS = 0x0004;
    public static final int CONNECTED_BY_WIFI = 0x0005;
    public static final int CONNECTED_BY_USB_TETHERING = 0x0006;
    public static final int CONNECTED_BY_EAN = 0x0007;
    public static final int CONNECTED_BY_USBMUXD = 0x0008;

    public static int CONNECTED_TYPE = CONNECTED_BY_AOA;

    public static String SERVER_NCM_URL = null;
    public static String SERVER_WIFI_URL = null;
    public static String SERVER_USBMUXD_URL = CommonParams.SERVER_LOCALHOST_URL;
    public static String SERVER_USB_TETHERING_URL = null;
    public static String SERVER_URL = null;
    public static int SERVER_SOCKET_PORT = -1;
    public static int SERVER_SOCKET_VIDEO_PORT = -1;
    public static int SERVER_SOCKET_AUDIO_PORT = -1;
    public static int SERVER_SOCKET_AUDIO_TTS_PORT = -1;
    public static int SERVER_SOCKET_AUDIO_VR_PORT = -1;
    public static int SERVER_SOCKET_TOUCH_PORT = -1;
    private ConnectSocket mConnectSocket = null;
    private ConnectSocket mVideoConnectSocket = null;
    private ConnectSocket mAudioConnectSocket = null;
    private ConnectSocket mAudioTTSConnectSocket = null;
    private ConnectSocket mAudioVRConnectSocket = null;
    private ConnectSocket mTouchConnectSocket = null;
    private int numOfSocket = 0;
    private static int TOTAL_SOCKET = 6;
    private boolean isProtocolVersionMatch = false;

    public static ConnectManager getInstance() {
        if (null == mInstance) {
            synchronized (ConnectManager.class) {
                if (null == mInstance) {
                    mInstance = new ConnectManager();
                }
            }
        }
        return mInstance;
    }

    private ConnectManager() {
    }

    public void init(Context context) {
        mContext = context;
        UsbMuxdConnect.getInstance().init(context);
    }

    public void uninit() {
        stopConnectThread();
    }

    public int getConnectType() {
        return CONNECTED_TYPE;
    }

    public void initConnectType(int type) {
        LogUtil.d(TAG, "initConnectType");

        CONNECTED_TYPE = type;

        switch (CONNECTED_TYPE) {
            case CONNECTED_BY_AOA:
                SERVER_URL = CommonParams.SERVER_LOCALHOST_URL;
                SERVER_SOCKET_PORT = CommonParams.SOCKET_LOCALHOST_PORT;
                SERVER_SOCKET_VIDEO_PORT = CommonParams.SOCKET_VIDEO_LOCALHOST_PORT;
                SERVER_SOCKET_AUDIO_PORT = CommonParams.SOCKET_AUDIO_LOCALHOST_PORT;
                SERVER_SOCKET_AUDIO_TTS_PORT = CommonParams.SOCKET_AUDIO_TTS_LOCALHOST_PORT;
                SERVER_SOCKET_AUDIO_VR_PORT = CommonParams.SOCKET_AUDIO_VR_LOCALHOST_PORT;
                SERVER_SOCKET_TOUCH_PORT = CommonParams.SOCKET_TOUCH_LOCALHOST_PORT;
                break;
            case CONNECTED_BY_WIFI:
                SERVER_URL = SERVER_WIFI_URL;
                SERVER_SOCKET_PORT = CommonParams.SOCKET_WIFI_PORT;
                SERVER_SOCKET_VIDEO_PORT = CommonParams.SOCKET_VIDEO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_PORT = CommonParams.SOCKET_AUDIO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_TTS_PORT = CommonParams.SOCKET_AUDIO_TTS_WIFI_PORT;
                SERVER_SOCKET_AUDIO_VR_PORT = CommonParams.SOCKET_AUDIO_VR_WIFI_PORT;
                SERVER_SOCKET_TOUCH_PORT = CommonParams.SOCKET_TOUCH_WIFI_PORT;
                break;
            case CONNECTED_BY_USB_TETHERING:
                SERVER_URL = SERVER_USB_TETHERING_URL;
                SERVER_SOCKET_PORT = CommonParams.SOCKET_WIFI_PORT;
                SERVER_SOCKET_VIDEO_PORT = CommonParams.SOCKET_VIDEO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_PORT = CommonParams.SOCKET_AUDIO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_TTS_PORT = CommonParams.SOCKET_AUDIO_TTS_WIFI_PORT;
                SERVER_SOCKET_AUDIO_VR_PORT = CommonParams.SOCKET_AUDIO_VR_WIFI_PORT;
                SERVER_SOCKET_TOUCH_PORT = CommonParams.SOCKET_TOUCH_WIFI_PORT;
                break;
            case CONNECTED_BY_EAN:
                SERVER_URL = CommonParams.SERVER_LOCALHOST_URL;
                SERVER_SOCKET_PORT = CommonParams.SOCKET_WIFI_PORT;
                SERVER_SOCKET_VIDEO_PORT = CommonParams.SOCKET_VIDEO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_PORT = CommonParams.SOCKET_AUDIO_WIFI_PORT;
                SERVER_SOCKET_AUDIO_TTS_PORT = CommonParams.SOCKET_AUDIO_TTS_WIFI_PORT;
                SERVER_SOCKET_AUDIO_VR_PORT = CommonParams.SOCKET_AUDIO_VR_WIFI_PORT;
                SERVER_SOCKET_TOUCH_PORT = CommonParams.SOCKET_TOUCH_WIFI_PORT;
                break;
            case CONNECTED_BY_USBMUXD:
                SERVER_URL = SERVER_USBMUXD_URL;
                SERVER_SOCKET_PORT = UsbMuxdConnect.SOCKET_USBMUXD_PORT;
                SERVER_SOCKET_VIDEO_PORT = UsbMuxdConnect.SOCKET_VIDEO_USBMUXD_PORT;
                SERVER_SOCKET_AUDIO_PORT = UsbMuxdConnect.SOCKET_AUDIO_USBMUXD_PORT;
                SERVER_SOCKET_AUDIO_TTS_PORT = UsbMuxdConnect.SOCKET_AUDIO_TTS_USBMUXD_PORT;
                SERVER_SOCKET_AUDIO_VR_PORT = UsbMuxdConnect.SOCKET_AUDIO_VR_USBMUXD_PORT;
                SERVER_SOCKET_TOUCH_PORT = UsbMuxdConnect.SOCKET_TOUCH_USBMUXD_PORT;
                break;
            default:
                break;
        }

        LogUtil.e(TAG, "Current Connect Type: " + CONNECTED_TYPE);
        LogUtil.e(TAG, "Current Server IP：" + SERVER_URL);
    }

    public int getIphoneConnectType() {
        int iphoneConnectType = 0;
        int connectTypeIphoneProperty =
                CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_CONNECT_TYPE_IPHONE);
        if (connectTypeIphoneProperty == 0) {
            iphoneConnectType = CONNECTED_BY_NCM_IOS;
        } else if (connectTypeIphoneProperty == 1) {
            iphoneConnectType = CONNECTED_BY_WIFI;
        } else if (connectTypeIphoneProperty == 3) {
            iphoneConnectType = CONNECTED_BY_USB_TETHERING;
        } else if (connectTypeIphoneProperty == 4) {
            iphoneConnectType = CONNECTED_BY_EAN;
        } else if (connectTypeIphoneProperty == 5) {
            iphoneConnectType = CONNECTED_BY_USBMUXD;
        }
        return iphoneConnectType;

    }
    public void startConnectThread() {
        try {
            int connectTypeIphoneValue = 0;
            connectTypeIphoneValue = getIphoneConnectType();
            LogUtil.e(TAG, "ConnectTypeIphoneValue = " + connectTypeIphoneValue);
            switch (connectTypeIphoneValue) {
                case CONNECTED_BY_WIFI:
                    WifiConnectManager.getInstance().startWifiConnectThread();
                    break;
                case CONNECTED_BY_USB_TETHERING:
                    IphoneUsbTethering.getInstance().startUsbTetheringConnectThread();
                    break;
                case CONNECTED_BY_EAN:
                    EanConnectManager.getInstance().startEanConnectThread();
                    break;
                case CONNECTED_BY_USBMUXD:
                    UsbMuxdConnect.getInstance().startUsbMuxdConnectThread();
                    break;
                default:
                    LogUtil.e(TAG, "get ios connect type error");
                    EanConnectManager.getInstance().startEanConnectThread();
                    break;
            }
            int connectTypeAndroidProperty = CarlifeConfUtil.getInstance().getIntProperty(
                    CarlifeConfUtil.KEY_INT_CONNECT_TYPE_ANDROID);
            if (connectTypeAndroidProperty == 1) {
                AOAConnectManager.getInstance().startAOAConnectThread();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Start ConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void stopConnectThread() {
        try {
            int connectTypeIphoneValue = 0;
            int connectTypeAndroidProperty =
                    CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_CONNECT_TYPE_ANDROID);

            if (connectTypeAndroidProperty == 1) {
                AOAConnectManager.getInstance().stopAOAConnectThread();
                AOAConnectManager.getInstance().uninit();
            }

            connectTypeIphoneValue = getIphoneConnectType();
            LogUtil.e(TAG, "ConnectTypeIphoneValue = " + connectTypeIphoneValue);
            switch (connectTypeIphoneValue) {
                case CONNECTED_BY_WIFI:
                    WifiConnectManager.getInstance().stopWifiConnectThread();
                    break;
                case CONNECTED_BY_USB_TETHERING:
                    IphoneUsbTethering.getInstance().stopUsbTetheringConnectThread();
                    break;
                case CONNECTED_BY_EAN:
                    EanConnectManager.getInstance().stopEanConnectThread();
                    EanConnectManager.getInstance().eanUinit();
                    break;
                case CONNECTED_BY_USBMUXD:
                    UsbMuxdConnect.getInstance().stopUsbMuxdConnectThread();
                    break;
                default:
                    LogUtil.e(TAG, "get ios connect type error");
                    EanConnectManager.getInstance().stopEanConnectThread();
                    EanConnectManager.getInstance().eanUinit();
                    break;
            }
            ConnectClient.getInstance().setIsConnecting(false);
        } catch (Exception e) {
            LogUtil.e(TAG, "Stop ConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void switchIOSConnectThread() {
        try {
            WifiConnectManager.getInstance().stopWifiConnectThread();
            
            int connectTypeIphoneValue = CONNECTED_BY_WIFI;
            int connectTypeIphoneProperty =
                    CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_CONNECT_TYPE_IPHONE);
            if (connectTypeIphoneProperty == 0) {
                connectTypeIphoneValue = CONNECTED_BY_NCM_IOS;
            } else if (connectTypeIphoneProperty == 1) {
                connectTypeIphoneValue = CONNECTED_BY_WIFI;
            } else if (connectTypeIphoneProperty == 2) {
                connectTypeIphoneValue =
                        PreferenceUtil.getInstance().getInt(PreferenceUtil.CONNECT_TYPE_KEY, CONNECTED_BY_NCM_IOS);
            }
            LogUtil.e(TAG, "ConnectTypeIphoneValue = " + connectTypeIphoneValue);
            switch (connectTypeIphoneValue) {
                case CONNECTED_BY_WIFI:
                    WifiConnectManager.getInstance().startWifiConnectThread();
                    break;
                default:
                    LogUtil.e(TAG, "switch ios connect type error");
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "switchIOSConnectThread Start ConnectThread Fail");
            e.printStackTrace();
        }
    }

    public void startAllConnectSocket() {
        InetAddress serveraddr = null;
        Socket mSocketMain = null;
        Socket mSocketVideo = null;
        Socket mSocketAudio = null;
        Socket mSocketAudioTTS = null;
        Socket mSocketAudioVR = null;
        Socket mSocketTouch = null;

        try {
            serveraddr = InetAddress.getByName(SERVER_URL);
            mSocketMain = new Socket(serveraddr, SERVER_SOCKET_PORT);
            if (null != mSocketMain) {
                LogUtil.d(TAG, "Connected to: " + mSocketMain.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_NAME, mSocketMain);
                connectSocket.startConmunication();
            }

            mSocketVideo = new Socket(serveraddr, SERVER_SOCKET_VIDEO_PORT);
            if (null != mSocketVideo) {
                LogUtil.d(TAG, "Connected to: " + mSocketVideo.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_VIDEO_NAME, mSocketVideo);
                connectSocket.startConmunication();
            }

            mSocketAudio = new Socket(serveraddr, SERVER_SOCKET_AUDIO_PORT);
            if (null != mSocketAudio) {
                LogUtil.d(TAG, "Connected to: " + mSocketAudio.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_NAME, mSocketAudio);
                connectSocket.startConmunication();
            }

            mSocketAudioTTS = new Socket(serveraddr, SERVER_SOCKET_AUDIO_TTS_PORT);
            if (null != mSocketAudioTTS) {
                LogUtil.d(TAG, "Connected to: " + mSocketAudioTTS.toString());
                ConnectSocket connectSocket =
                        new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME, mSocketAudioTTS);
                connectSocket.startConmunication();
            }

            mSocketAudioVR = new Socket(serveraddr, SERVER_SOCKET_AUDIO_VR_PORT);
            if (null != mSocketAudioVR) {
                LogUtil.d(TAG, "Connected to: " + mSocketAudioVR.toString());
                ConnectSocket connectSocket =
                        new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_VR_NAME, mSocketAudioVR);
                connectSocket.startConmunication();
            }

            mSocketTouch = new Socket(serveraddr, SERVER_SOCKET_TOUCH_PORT);
            if (null != mSocketTouch) {
                LogUtil.d(TAG, "Connected to: " + mSocketTouch.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_TOUCH_NAME, mSocketTouch);
                connectSocket.startConmunication();
            }
        } catch (Exception ex) {
            LogUtil.d(TAG, "start ConnectSocket fail");
            int connectedType = getConnectType();
            switch (connectedType) {
                case CONNECTED_BY_AOA:
                    ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.AOA_ERROR_SOCKET_EXCEPTION);
                    break;
                case CONNECTED_BY_WIFI:
                    ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.IOS_WIFI_ERROR_SOCKET_EXCEPTION);
                    break;
                default:
                    break;
            }

            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL);
            ex.printStackTrace();
        }

    }

    public void startAllConnectIPV6Socket() {
        Inet6Address serveraddr = null;
        Socket mSocketMain = null;
        Socket mSocketVideo = null;
        Socket mSocketAudio = null;
        Socket mSocketAudioTTS = null;
        Socket mSocketAudioVR = null;
        Socket mSocketTouch = null;

        try {
            serveraddr = (Inet6Address) InetAddress.getByName(SERVER_URL);
            mSocketMain = new Socket(serveraddr, SERVER_SOCKET_PORT);
            if (null != mSocketMain) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketMain.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_NAME, mSocketMain);
                connectSocket.startConmunication();
            }

            mSocketVideo = new Socket(serveraddr, SERVER_SOCKET_VIDEO_PORT);
            if (null != mSocketVideo) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketVideo.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_VIDEO_NAME, mSocketVideo);
                connectSocket.startConmunication();
            }

            mSocketAudio = new Socket(serveraddr, SERVER_SOCKET_AUDIO_PORT);
            if (null != mSocketAudio) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketAudio.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_NAME, mSocketAudio);
                connectSocket.startConmunication();
            }

            mSocketAudioTTS = new Socket(serveraddr, SERVER_SOCKET_AUDIO_TTS_PORT);
            if (null != mSocketAudioTTS) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketAudioTTS.toString());
                ConnectSocket connectSocket =
                        new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME, mSocketAudioTTS);
                connectSocket.startConmunication();
            }

            mSocketAudioVR = new Socket(serveraddr, SERVER_SOCKET_AUDIO_VR_PORT);
            if (null != mSocketAudioVR) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketAudioVR.toString());
                ConnectSocket connectSocket =
                        new ConnectSocket(CommonParams.SERVER_SOCKET_AUDIO_VR_NAME, mSocketAudioVR);
                connectSocket.startConmunication();
            }

            mSocketTouch = new Socket(serveraddr, SERVER_SOCKET_TOUCH_PORT);
            if (null != mSocketTouch) {
                LogUtil.d(TAG, "IPV6 Connected to: " + mSocketTouch.toString());
                ConnectSocket connectSocket = new ConnectSocket(CommonParams.SERVER_SOCKET_TOUCH_NAME, mSocketTouch);
                connectSocket.startConmunication();
            }
        } catch (Exception ex) {
            LogUtil.d(TAG, "IPV6 start ConnectSocket fail");
            ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.IOS_NCM_ERROR_CONNECT_SOCKET);
            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_FAIL);
            ex.printStackTrace();
        }

    }

    public void stopAllConnectSocket() {
        try {
            numOfSocket = 0;
            isProtocolVersionMatch = false;
            if (mConnectSocket != null) {
                mConnectSocket.stopConnunication();
                mConnectSocket = null;
            }
            if (mVideoConnectSocket != null) {
                mVideoConnectSocket.stopConnunication();
                mVideoConnectSocket = null;
            }
            if (mAudioConnectSocket != null) {
                mAudioConnectSocket.stopConnunication();
                mAudioConnectSocket = null;
            }
            if (mAudioTTSConnectSocket != null) {
                mAudioTTSConnectSocket.stopConnunication();
                mAudioTTSConnectSocket = null;
            }
            if (mAudioVRConnectSocket != null) {
                mAudioVRConnectSocket.stopConnunication();
                mAudioVRConnectSocket = null;
            }
            if (mTouchConnectSocket != null) {
                mTouchConnectSocket.stopConnunication();
                mTouchConnectSocket = null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "stop ConnectSocket fail");
            e.printStackTrace();
        }
    }

    public synchronized void addConnectSocket(ConnectSocket connectSocket) {
        numOfSocket++;
        if (numOfSocket >= TOTAL_SOCKET) {
            if (CONNECTED_TYPE != CONNECTED_BY_USBMUXD) {
                ConnectClient.getInstance().setIsConnected(true);
            }
        }
        try {
            if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_NAME)) {
                mConnectSocket = connectSocket;
            } else if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_VIDEO_NAME)) {
                mVideoConnectSocket = connectSocket;
            } else if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_AUDIO_NAME)) {
                mAudioConnectSocket = connectSocket;
            } else if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_AUDIO_TTS_NAME)) {
                mAudioTTSConnectSocket = connectSocket;
            } else if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_AUDIO_VR_NAME)) {
                mAudioVRConnectSocket = connectSocket;
            } else if (connectSocket.getConnectSocketName().equals(CommonParams.SERVER_SOCKET_TOUCH_NAME)) {
                mTouchConnectSocket = connectSocket;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "add ConnectSocket fail");
            e.printStackTrace();
        }

    }

    public boolean getIsProtocolVersionMatch() {
        return isProtocolVersionMatch;
    }

    public void setIsProtocolVersionMatch(boolean is) {
        isProtocolVersionMatch = is;
    }

    public int writeCarlifeCmdMessage(CarlifeCmdMessage msg) {
        if (null == mConnectSocket) {
            LogUtil.e(TAG, "write error: connectSocket is null");
            return -1;
        }
        return mConnectSocket.writeData(msg);
    }

    public int writeVideoData(byte[] buffer, int len) {
        if (null == mVideoConnectSocket) {
            LogUtil.e(TAG, "write error: video connectSocket is null");
            return -1;
        }
        return mVideoConnectSocket.writeData(buffer, len);
    }

    public int readVideoData(byte[] buffer, int len) {
        ++numOfHeartBeat;
        if (null == mVideoConnectSocket) {
            LogUtil.e(TAG, "read error: video connectSocket is null");
            return -1;
        }
        return mVideoConnectSocket.readData(buffer, len);
    }

    public int writeAudioData(byte[] buffer, int len) {
        if (null == mAudioConnectSocket) {
            LogUtil.e(TAG, "write error: audio connectSocket is null");
            return -1;
        }
        return mAudioConnectSocket.writeData(buffer, len);
    }
    public int readAudioData(byte[] buffer, int len) {
        if (null == mAudioConnectSocket) {
            LogUtil.e(TAG, "read error: audio connectSocket is null");
            return -1;
        }
        return mAudioConnectSocket.readData(buffer, len);
    }

    public int writeAudioTTSData(byte[] buffer, int len) {
        if (null == mAudioTTSConnectSocket) {
            LogUtil.e(TAG, "write error: tts connectSocket is null");
            return -1;
        }
        return mAudioTTSConnectSocket.writeData(buffer, len);
    }

    public int readAudioTTSData(byte[] buffer, int len) {
        if (null == mAudioTTSConnectSocket) {
            LogUtil.e(TAG, "read error: tts connectSocket is null");
            return -1;
        }
        return mAudioTTSConnectSocket.readData(buffer, len);
    }

    public int writeAudioVRData(byte[] buffer, int len) {
        if (null == mAudioVRConnectSocket) {
            LogUtil.e(TAG, "write error: VR connectSocket is null");
            return -1;
        }
        return mAudioVRConnectSocket.writeData(buffer, len);
    }

    public int readAudioVRData(byte[] buffer, int len) {
        if (null == mAudioVRConnectSocket) {
            LogUtil.e(TAG, "read error: VR connectSocket is null");
            return -1;
        }
        return mAudioVRConnectSocket.readData(buffer, len);
    }

    public int writeCarlifeTouchMessage(CarlifeCmdMessage msg) {
        if (null == mTouchConnectSocket) {
            LogUtil.e(TAG, "write error: touch connectSocket is null");
            return -1;
        }
        return mTouchConnectSocket.writeData(msg);
    }

    public void startHeartBeatTimer() {
        try {
            LogUtil.d(TAG, "start heart beat timer");
            numOfHeartBeat = 0;
            oldNumOfHeartBeat = -1;
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    LogUtil.i(TAG, "start heart beat timer: numOfHeartBeat = " + numOfHeartBeat
                            + ", oldNumOfHeartBeat = " + oldNumOfHeartBeat);
                    if (oldNumOfHeartBeat == numOfHeartBeat) {
                        if ((CONNECTED_BY_WIFI == getConnectType())
                                || (CONNECTED_BY_USB_TETHERING == getConnectType())
                                || (CONNECTED_BY_USBMUXD == getConnectType())) {
                            if (mBeatCount >= 2) {
                                if (mTimer != null) {
                                    LogUtil.e(TAG, "start heart beat timer 1");
                                    ConnectClient.getInstance().setIsConnected(false);
                                    ConnectManager.getInstance().stopConnectThread();
                                    stopHeartBeatTimer();
                                    return;
                                }
                            }
                        } else {
                            if (mBeatCount >= 5) {
                                if (mTimer != null) {
                                    LogUtil.e(TAG, "start heart beat timer 2");
                                    ConnectClient.getInstance().setIsConnected(false);
                                    ConnectManager.getInstance().stopConnectThread();
                                    stopHeartBeatTimer();
                                    return;
                                }
                            }
                        }
                        
                        mBeatCount++;
                    } else {
                        mBeatCount = 0;
                    }
                    oldNumOfHeartBeat = numOfHeartBeat;
                }
            };
            mTimer.schedule(mTimerTask, 5 * HEART_BEAT_CHECK_MS, HEART_BEAT_CHECK_MS);
        } catch (Exception ex) {
            LogUtil.d(TAG, "startTimer get exception");
            ex.printStackTrace();
        }
    }

    public void stopHeartBeatTimer() {
        LogUtil.d(TAG, "stop heart beat timer");
        mBeatCount = 0;
        numOfHeartBeat = 0;
        oldNumOfHeartBeat = -1;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void sendVedioTransMsg() {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_VIDEO_ENCODER_START);
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public void sendConnectTypeToMd(int moduleId, int statusId) {
        CarlifeModuleStatus.Builder moduleStatusBuilder = CarlifeModuleStatus.newBuilder();
        moduleStatusBuilder.setModuleID(moduleId);
        moduleStatusBuilder.setStatusID(statusId);
        CarlifeModuleStatus carlifeModuleStatus = moduleStatusBuilder.build();
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_MODULE_CONTROL);
        command.setData(carlifeModuleStatus.toByteArray());
        command.setLength(carlifeModuleStatus.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }


    public boolean isADBDeviceIn() {
        final int usbClassAdb = 255;
        final int usbSubClassAdb = 66;
        final int usbProtocolAdb = 1;


        UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (null == mUsbManager) {
            return  false;
        }

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.d( TAG, "device count=" + deviceList.size() );

        int nInedex = 0;
        boolean bGetADBDevice = false;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            int interfaceCount = device.getInterfaceCount();
            Log.d( TAG, "Device Info index ::" + nInedex + "Interface Count ::" + interfaceCount );
            for (int interfaceIndex = 0; interfaceIndex < interfaceCount; interfaceIndex++) {
                UsbInterface usbInterface = device.getInterface(interfaceIndex);
                Log.d( TAG, "Interface  ::[Class=" + usbInterface.getInterfaceClass() + "][Sub Class="
                        + usbInterface.getInterfaceSubclass()
                        + "][Protocol=" + usbInterface.getInterfaceProtocol() + "]");

                if ((usbClassAdb == usbInterface.getInterfaceClass())
                        && (usbSubClassAdb == usbInterface.getInterfaceSubclass())
                        && (usbProtocolAdb == usbInterface.getInterfaceProtocol())) {
                    Log.d( TAG, "GetADB Initeface !!!!!!" );
                    bGetADBDevice  = true;
                    break;
                }
            }
            ++nInedex;
        }
        return  bGetADBDevice;
    }

    public boolean isMobileDeviceIn() {
        final int storageInterfaceConut = 1;
        final int storageInterfaceId = 0;
        final int storageInterfaceClass = 8;
        int storageInterfaceSubclass = 6;
        int storageInterfaceProtocol = 80;

        UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (null == mUsbManager) {
            Log.d( TAG, "############## UsbManager Error!");
            return  false;
        }

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        Log.d( TAG, "############################");
        Log.d( TAG, "device count=" + deviceList.size() );

        int nInedex = 0;
        boolean bGetDevice = false;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (storageInterfaceConut == device.getInterfaceCount()) {
                UsbInterface usbInter = device.getInterface(storageInterfaceId);
                if ((storageInterfaceClass == usbInter.getInterfaceClass())
                        && (storageInterfaceSubclass == usbInter.getInterfaceSubclass())
                        && (storageInterfaceProtocol == usbInter.getInterfaceProtocol())) {
                    LogUtil.e(TAG, "This is mass storage 1");
                    break;
                }
            }

            int interfaceCount = device.getInterfaceCount();
            Log.d( TAG, "Device Info [PID:" + device.getProductId() + "][VID:"
                    + device.getDeviceId()
                    + "] Interface Count ::" + interfaceCount );
            if ( interfaceCount > 0 ) {
                bGetDevice = true;
            }
            ++nInedex;
        }
        return  bGetDevice;
    }

}
