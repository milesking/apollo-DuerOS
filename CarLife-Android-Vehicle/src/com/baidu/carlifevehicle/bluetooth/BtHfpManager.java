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
package com.baidu.carlifevehicle.bluetooth;

import com.baidu.carlife.protobuf.CarlifeBTHfpRequestProto.CarlifeBTHfpRequest;
import com.baidu.carlife.protobuf.CarlifeBTHfpStatusRequestProto.CarlifeBTHfpStatusRequest;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

public class BtHfpManager {
    private static final String TAG = BtHfpManager.class.getSimpleName();
    private static final String ACTION_BT_CARLIEF_CONN_STATE = "com.baidu.carlife.connection";
    private static final String EXTRA_BT_CARLIFE_CONN_STATE = "com.baidu.carlife.connection.state";
    private static final String ACTION_BT_HFT_CLIENT = "com.baidu.carlifevehicle.bluetoothHfpClient";

    public static final int BT_HFP_NEW_CALL = 1;
    public static final int BT_HFP_OUT_CALL = 2;
    public static final int BT_HFP_CALL_ACTIVE = 3;
    public static final int BT_HFP_NO_CALL_ACTIVE = 4;

    public static final int BT_HFP_CONNECTED = 2;
    public static final int BT_HFP_CONNECTING = 1;
    public static final int BT_HFP_DISCONNECTED = 0;

    public static final int BT_HFP_IDENTIFY_SUCCEED = 1;
    public static final int BT_HFP_IDENTIFY_FAILED = 0;

    public static final int BT_HFP_START_CALL = 1;
    public static final int BT_HFP_TERMINATE_CALL = 2;
    public static final int BT_HFP_ANSWER_CALL = 3;
    public static final int BT_HFP_REJECT_CALL = 4;
    public static final int BT_HFP_DTMF_CODE = 5;
    public static final int BT_HFP_MUTE_MIC = 6;
    public static final int BT_HFP_UNMUTE_MIC = 7;

    public static final int BT_HFP_TYPE_MIC_STATUS = 1;

    public static final int HU_MIC_MUTE = 1;
    public static final int HU_MIC_UNMUTE = 0;

    public static final int BT_HFP_STATUS_INVALID_PARAM = -1;
    public static final int BT_HFP_STATUS_FAILURE = 0;
    public static final int BT_HFP_STATUS_SUCCESS = 1;

    private Context mContext = null;
    private static BtHfpManager mInstance = null;
    private IHfpClient mBinder;
    private MsgBtHfpHandler mHandler = null;
    public boolean isServiceRunning = false;

    public BtHfpManager() {
        mHandler = new MsgBtHfpHandler();
    }

    public static BtHfpManager getInstance() {
        if (null == mInstance) {
            synchronized (BtHfpManager.class) {
                if (null == mInstance) {
                    mInstance = new BtHfpManager();
                }
            }
        }
        return mInstance;
    }

    public boolean isServiceAvailable() {
        boolean ret = false;
        try {
            Intent intent = new Intent(ACTION_BT_HFT_CLIENT);
            if (mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)) {
                ret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean init(Context context) {
        mContext = context;
        if (isServiceAvailable()) {
            return true;
        }
        return false;
    }

    public void uninit() {
        if (mContext != null) {
            try {
                if (mBinder != null && mCallback != null) {
                    LogUtil.d(TAG, "unregister bt hfp client callback");
                    mBinder.unregisterHfpCallback(mCallback);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                if (mServiceConnection != null) {
                    LogUtil.d(TAG, "unbind bt hfp service");
                    mContext.unbindService(mServiceConnection);
                }
            } catch (IllegalArgumentException e) {
                return;
            }

        }

    }

    public void start() {
        LogUtil.d(TAG, "Start Bluetooth Hfp service");
        if (mHandler != null) {
            MsgHandlerCenter.registerMessageHandler(mHandler);
        }

        disableNativeTelephoneApp();
        isServiceRunning = true;
    }

    public void stop() {
        LogUtil.d(TAG, "Stop Bluetooth Hfp service");
        if (mHandler != null) {
            MsgHandlerCenter.unRegisterMessageHandler(mHandler);
        }

        enableNativeTelephoneApp();
        isServiceRunning = false;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "bind bt hfp service successfully,and regisiter callback");
            mBinder = IHfpClient.Stub.asInterface(service);
            try {
                if (mBinder != null && mCallback != null) {
                    mBinder.registerHfpCallback(mCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private IHfpClientCallback.Stub mCallback = new IHfpClientCallback.Stub() {

        @Override
        public void onOutgoingCall(String phoneNumber, String name) throws RemoteException {
            if (mBinder != null) {
                LogUtil.d(TAG, "onOutgoingCall,Number : " + phoneNumber);
                BtDeviceManager.getInstance().onOutgoingCall(phoneNumber);
            }

        }

        @Override
        public void onIncomingCall(String phoneNumber, String name) throws RemoteException {
            if (mBinder != null) {
                LogUtil.d(TAG, "onIncomingCall,Number : " + phoneNumber);
                BtDeviceManager.getInstance().onIncomingCall(phoneNumber);
            }

        }

        @Override
        public void onConnectionStateChanged(int state, String address) throws RemoteException {
            if (mBinder != null) {
                if (state == BT_HFP_CONNECTED) {
                    LogUtil.d(TAG, "HFP Connected with device : " + address);
                } else if (state == BT_HFP_DISCONNECTED) {
                    LogUtil.d(TAG, "HFP Disconnected with device : " + address);

                } else if (state == BT_HFP_CONNECTING) {
                    LogUtil.d(TAG, "HFP Connecting with device : " + address);
                }
                BtHfpProtocolHelper.btHfpConnStateIndication(state, address);
            }

        }

        @Override
        public void onCallInactive() throws RemoteException {
            if (mBinder != null) {
                LogUtil.d(TAG, "onCallInactive");
                BtDeviceManager.getInstance().onCallInactive();
            }

        }

        @Override
        public void onCallActive() throws RemoteException {
            if (mBinder != null) {
                LogUtil.d(TAG, "onCallActive");
                BtDeviceManager.getInstance().onCallActive();
            }

        }
    };

    private class MsgBtHfpHandler extends MsgBaseHandler {

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_BT_HFP_REQUEST);
            addMsg(CommonParams.MSG_CMD_BT_HFP_STATUS_REQUEST);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonParams.MSG_CMD_BT_HFP_STATUS_REQUEST:
                    CarlifeCmdMessage hfpStatusRequestMessage = (CarlifeCmdMessage) msg.obj;
                    CarlifeBTHfpStatusRequest btStatusRequest;
                    try {
                        btStatusRequest = CarlifeBTHfpStatusRequest.parseFrom(hfpStatusRequestMessage.getData());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (btStatusRequest == null) {
                        return;
                    }
                    int type = btStatusRequest.getType();
                    switch (type) {
                        case BT_HFP_TYPE_MIC_STATUS:
                            if (mBinder != null) {
                                try {
                                    boolean micStatus = mBinder.getMic();
                                    if (micStatus) {
                                        LogUtil.d(TAG, "Mic status is muted");
                                        BtHfpProtocolHelper.btHfpStatusResponse(BT_HFP_TYPE_MIC_STATUS, HU_MIC_MUTE);
                                    } else {
                                        LogUtil.d(TAG, "Mic status is unmuted");
                                        BtHfpProtocolHelper.btHfpStatusResponse(BT_HFP_TYPE_MIC_STATUS, HU_MIC_UNMUTE);
                                    }

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    break;
                case CommonParams.MSG_CMD_BT_HFP_REQUEST:
                    CarlifeCmdMessage hfpCarlifeCmdMessage = (CarlifeCmdMessage) msg.obj;
                    CarlifeBTHfpRequest btRequest;
                    try {
                        btRequest = CarlifeBTHfpRequest.parseFrom(hfpCarlifeCmdMessage.getData());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (btRequest == null) {
                        return;
                    }

                    int cmdId = btRequest.getCommand();
                    switch (cmdId) {
                        case BT_HFP_START_CALL:
                            String phoneNum = btRequest.getPhoneNum();
                            if (!TextUtils.isEmpty(phoneNum)) {
                                if (mBinder != null) {
                                    try {
                                        if (mBinder.dial(phoneNum)) {
                                            BtHfpProtocolHelper.btHfpResponse(BT_HFP_START_CALL, BT_HFP_STATUS_SUCCESS);
                                            LogUtil.d(TAG, "MD--->HU: dial in success : " + phoneNum);

                                        } else {
                                            BtHfpProtocolHelper.btHfpResponse(BT_HFP_START_CALL, BT_HFP_STATUS_FAILURE);
                                            LogUtil.d(TAG, "MD--->HU: dial in failure: " + phoneNum);
                                        }
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                LogUtil.d(TAG, "Invalid Phone Number");
                                BtHfpProtocolHelper.btHfpResponse(BT_HFP_START_CALL, BT_HFP_STATUS_INVALID_PARAM);
                            }
                            break;
                        case BT_HFP_ANSWER_CALL:
                            if (mBinder != null) {
                                try {
                                    if (mBinder.acceptCall()) {
                                        LogUtil.d(TAG, "MD--->HU: Answer call in success");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_ANSWER_CALL, BT_HFP_STATUS_SUCCESS);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Answer call in failure");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_ANSWER_CALL, BT_HFP_STATUS_FAILURE);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case BT_HFP_TERMINATE_CALL:
                            if (mBinder != null) {
                                try {
                                    if (mBinder.terminateCall()) {
                                        LogUtil.d(TAG, "MD--->HU: Terminate call in success");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_TERMINATE_CALL, BT_HFP_STATUS_SUCCESS);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Terminate call in failure");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_TERMINATE_CALL, BT_HFP_STATUS_FAILURE);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case BT_HFP_REJECT_CALL:
                            if (mBinder != null) {
                                try {
                                    if (mBinder.rejectCall()) {
                                        LogUtil.d(TAG, "MD--->HU: Reject call in success");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_REJECT_CALL, BT_HFP_STATUS_SUCCESS);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Reject call in failure");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_REJECT_CALL, BT_HFP_STATUS_FAILURE);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case BT_HFP_DTMF_CODE:
                            int dtmfCode = btRequest.getDtmfCode();
                            if (mBinder != null) {
                                try {
                                    if (mBinder.sendDTMF((byte) dtmfCode)) {
                                        LogUtil.d(TAG, "MD--->HU: Send DTMF code in success,code = " + (byte) dtmfCode);
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_DTMF_CODE, BT_HFP_STATUS_SUCCESS,
                                                dtmfCode);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Send DTMF code in failure,code = " + (byte) dtmfCode);
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_DTMF_CODE, BT_HFP_STATUS_FAILURE,
                                                dtmfCode);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case BT_HFP_MUTE_MIC:
                            if (mBinder != null) {
                                try {
                                    if (mBinder.setMic(false)) {
                                        LogUtil.d(TAG, "MD--->HU: Mute mic in success");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_MUTE_MIC, BT_HFP_STATUS_SUCCESS);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Mute mic in failure");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_MUTE_MIC, BT_HFP_STATUS_FAILURE);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case BT_HFP_UNMUTE_MIC:
                            if (mBinder != null) {
                                try {
                                    if (mBinder.setMic(true)) {
                                        LogUtil.d(TAG, "MD--->HU: Unmute mic in success");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_UNMUTE_MIC, BT_HFP_STATUS_SUCCESS);
                                    } else {
                                        LogUtil.d(TAG, "MD--->HU: Unmute mic in failure");
                                        BtHfpProtocolHelper.btHfpResponse(BT_HFP_UNMUTE_MIC, BT_HFP_STATUS_FAILURE);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

    }

    public boolean disableNativeTelephoneApp() {
        boolean ret = false;
        if (mBinder != null) {
            try {
                if (mBinder.blockNativeTelephone(true)) {
                    LogUtil.d(TAG, "Block native telephone application");
                    ret = true;
                } else {
                    LogUtil.d(TAG, "Failed in block native telephone application");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean enableNativeTelephoneApp() {
        boolean ret = false;
        if (mBinder != null) {
            try {
                if (mBinder.blockNativeTelephone(false)) {
                    LogUtil.d(TAG, "Unblock native telephone application");
                    ret = true;
                } else {
                    LogUtil.d(TAG, "Failed in unblock native telephone application");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public boolean answerCallNative() {
        boolean ret = false;
        if (mBinder != null) {
            try {
                if (mBinder.acceptCall()) {
                    LogUtil.d(TAG, "Accept call in success");
                    ret = true;
                } else {
                    LogUtil.d(TAG, "Accept call in failure");
                    ret = false;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public boolean rejectCallNative() {
        boolean ret = false;
        if (mBinder != null) {
            try {
                if (mBinder.rejectCall()) {
                    LogUtil.d(TAG, "Reject call in success");
                    ret = true;
                } else {
                    LogUtil.d(TAG, "Reject call in failure");

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

}
