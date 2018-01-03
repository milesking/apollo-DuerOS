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

import com.baidu.carlife.protobuf.CarlifeBTPairInfoProto.CarlifeBTPairInfo;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

public class BtPairStateMachine extends StateMachine {
    private static final String TAG = BtPairStateMachine.class.getSimpleName();
    
    private static final int STATE_IDLE = 0;
    private static final int STATE_ERROR = -1;
    private static final int STATE_READY = 1;
    private static final int STATE_DONE = 2;

    
    
    private PairIdleState mPairIdleState;
    private PairEnableState mPairEnableState;
    private PairDisconnectedState mPairDisconnectedState;
    private PairConnectedState mPairConnectedState;
    private PairErrorState mPairErrorState;

    public static final int NO_ERROR = 0;
    public static final int ERROR_BIND_SERVICE = 1;
    public static final int ERROR_ENABLE_BT = 2;
    public static final int ERROR_CHECK_CONNECTION = 3;
    public static final int ERROR_DISCONNECT = 4;
    
    
    
    private static final int EVENT_BASE = 10;
    private static final int EVENT_START_SERVICE = EVENT_BASE + 1;
    private static final int EVENT_ENABLE_BT = EVENT_BASE + 2;
    private static final int EVENT_START_SERVICE_SUCCEED = EVENT_BASE + 3;
    private static final int EVENT_START_SERVICE_FAILED = EVENT_BASE + 4;
    private static final int EVENT_START_BLUETOOTH_SUCCEED = EVENT_BASE + 5;
    private static final int EVENT_START_BLUETOOTH_FAILED = EVENT_BASE + 6;
    private static final int EVENT_MD_READY = EVENT_BASE + 7;
    private static final int EVENT_BOND_STATE_CHANGED = EVENT_BASE + 8;
    private static final int EVENT_CONNECTION_STATE_CHANGED = EVENT_BASE + 9;
    private static final int EVENT_PAIRING_REQUEST = EVENT_BASE + 10;
    private static final int EVENT_DISCONNECT = EVENT_BASE + 10;
    private static final int EVENT_CHECK_HFP_CONNECTION = EVENT_BASE + 11;
    private static final int EVENT_ON_ERROR = EVENT_BASE + 12;
    
    private String targetAddress = "";
    private static BtPairStateMachine mInstance = null;
    
    private BtNativeInterface mBtInterface = null;
    
    private static PairHandler mPairHandler;
    private BtPairCallback mBtPairCallback;
    
    private BtPairStateMachine(String remoteAddress, Context cx, BtPairCallback cb) {
        super(TAG, Looper.getMainLooper());
        targetAddress = remoteAddress;
        mBtPairCallback = cb;
        mPairIdleState = new PairIdleState();
        mPairEnableState = new PairEnableState();
        mPairDisconnectedState = new PairDisconnectedState();
        mPairConnectedState = new PairConnectedState();
        
        addState(mPairIdleState);
        addState(mPairErrorState);
        addState(mPairEnableState, mPairIdleState);
        addState(mPairDisconnectedState, mPairEnableState);
        addState(mPairConnectedState, mPairEnableState);
        
        mBtInterface = new BtNativeInterface(cx);
        mPairHandler = new PairHandler();
        setInitialState(mPairIdleState);
    }
    
    
    public static BtPairStateMachine make(String remoteAddress, Context cx, BtPairCallback callback) {
        BtPairStateMachine psm = new BtPairStateMachine(remoteAddress, cx, callback);
        
        MsgHandlerCenter.registerMessageHandler(mPairHandler);
        psm.start();
        
        return psm;
    }
    
    public void doQuit() {
        MsgHandlerCenter.unRegisterMessageHandler(mPairHandler);
        quit();
    }
    
    public void cleanup() {
        targetAddress = "";
        mBtPairCallback = null;
    }
    
    public interface BtPairCallback {
        void onPairDone(int state);
    }

    private int getHfpConnectionState() {
        if (mBtInterface != null) {
            return mBtInterface.getHfpConnectionState();
        } else {
            return -1;
        }
    }

    private boolean isHfpConnected() {
        boolean ret = false;
        if (mBtInterface != null) {
            String connectedAddr = mBtInterface.getConnectedDeviceAddress();
            if (!TextUtils.isEmpty(connectedAddr) && connectedAddr.contains(targetAddress)) {
                return true;
            }
        }
        return ret;
    }
    
    private class PairHandler extends MsgBaseHandler {

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_MD_BT_OOB_INFO);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case CommonParams.MSG_CMD_MD_BT_OOB_INFO:
                    CarlifeCmdMessage btCarlifeCmdMessage = (CarlifeCmdMessage) msg.obj;
                    CarlifeBTPairInfo btInfo = null;
                    try {
                        btInfo = CarlifeBTPairInfo.parseFrom(btCarlifeCmdMessage.getData());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (btInfo == null) {
                        return;
                    }
                    int status = btInfo.getStatus();
                    String address = btInfo.getAddress();
                    LogUtil.d(TAG, "MD--->HU: BT_OOB_INFO, state = " + status);
                    switch(status) {
                        case 1:
                            onMDReady();
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
    
    private BtInterfaceBase.BtCallback mBtCallback = new BtInterfaceBase.BtCallback() {
        
        @Override
        public void onReceivedPairingRequest(int mode, final BluetoothDevice device) {
            onPairingRequest(mode, device);
        }
        
        @Override
        public void onReceivedHfpConnectionStateChanged(int state, final BluetoothDevice device) {
            onConnectionStateChanged(state, device);    
        }
        
        @Override
        public void onReceivedBondStateChange(int state, final BluetoothDevice device) {
            onBondStateChanged(state, device);
        }

        @Override
        public void onServiceStart(boolean started) {
            onServiceStarted(started);
        }
    };
    
    private class PairIdleState extends State {

        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Pairing IDLE state");
            sendMessage(EVENT_START_SERVICE);
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Pairing IDLE state");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch(msg.what) {
                case EVENT_START_SERVICE:
                    startService();
                    return HANDLED;
                case EVENT_START_SERVICE_SUCCEED:
                    LogUtil.d(TAG, "bind service in success,and try to enable bt adapter");
                    enableBluetooth();
                    return HANDLED;
                case EVENT_START_SERVICE_FAILED:
                    deferMessage(obtainMessage(EVENT_ON_ERROR, ERROR_BIND_SERVICE, 0));
                    transitionTo(mPairErrorState);
                    return HANDLED;
                case EVENT_START_BLUETOOTH_SUCCEED:
                    transitionTo(mPairEnableState);
                    return HANDLED;
                case EVENT_START_BLUETOOTH_FAILED:
                    deferMessage(obtainMessage(EVENT_ON_ERROR, ERROR_ENABLE_BT, 0));
                    transitionTo(mPairErrorState);
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
        
    }
    
    private class PairEnableState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Bt enabled state");
            BtHfpProtocolHelper.btOOBInfo(STATE_IDLE);
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Bt enabled state");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch(msg.what) {
                case EVENT_MD_READY:
                    int connState = getHfpConnectionState();
                    if (connState == BluetoothProfile.STATE_DISCONNECTED) {
                        transitionTo(mPairDisconnectedState);
                    } else if (connState == BluetoothProfile.STATE_CONNECTED) {
                        deferMessage(obtainMessage(EVENT_CHECK_HFP_CONNECTION));
                        transitionTo(mPairConnectedState);
                    } else if (connState == BluetoothProfile.STATE_DISCONNECTING) {
                        sendMessageDelayed(EVENT_MD_READY, 2000);
                    } else if (connState == BluetoothProfile.STATE_CONNECTING) {
                        sendMessageDelayed(EVENT_MD_READY, 2000);
                    } else {
                        deferMessage(obtainMessage(EVENT_ON_ERROR, ERROR_CHECK_CONNECTION, 0));
                        transitionTo(mPairErrorState);
                    }
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }
    
    private class PairConnectedState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Bt Connected state");
            
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Bt Connected state");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch(msg.what) {
                case EVENT_CHECK_HFP_CONNECTION:
                    if (isHfpConnected()) {
                        BtHfpProtocolHelper.btOOBInfo(STATE_DONE);
                        if (mBtPairCallback != null) {
                            mBtPairCallback.onPairDone(NO_ERROR);
                        }
                    } else {
                        sendMessage(EVENT_DISCONNECT);
                    }
                    return HANDLED;
                case EVENT_DISCONNECT:
                    if (disconnectHfp()) {
                        transitionTo(mPairDisconnectedState);
                    } else {
                        deferMessage(obtainMessage(EVENT_ON_ERROR, ERROR_DISCONNECT, 0));
                        transitionTo(mPairErrorState);
                    }
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }
    
    private class PairDisconnectedState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Bt Disconnected state");
            BtHfpProtocolHelper.btOOBInfo(STATE_READY);
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Bt Disconnected state");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch(msg.what) {
                case EVENT_BOND_STATE_CHANGED:
                    int bondState = msg.arg1;
                    BluetoothDevice bondDevice = (BluetoothDevice) msg.obj;
                    if (bondDevice != null) {
                        String bondAddress = bondDevice.getAddress();
                        if (bondAddress != null && bondAddress.equals(targetAddress)) {
                            switch (bondState) {
                                case BluetoothDevice.BOND_NONE:
                                    LogUtil.d(TAG, "HU is not Bonded");
                                    break;
                                case BluetoothDevice.BOND_BONDED:
                                    LogUtil.d(TAG, "HU is Bonded");
                                    break;
                                case BluetoothDevice.BOND_BONDING:
                                    LogUtil.d(TAG, "HU is Bonding");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    return HANDLED;
                case EVENT_CONNECTION_STATE_CHANGED:
                    int connState = msg.arg1;
                    BluetoothDevice connDevice = (BluetoothDevice) msg.obj;
                    if (connDevice != null) {
                        String connAddress = connDevice.getAddress();
                        if (connAddress != null && connAddress.equals(targetAddress)) {
                            switch (connState) {
                                case BluetoothProfile.STATE_CONNECTED:
                                    LogUtil.d(TAG, "HU is connected");
                                    transitionTo(mPairConnectedState);
                                    break;
                                case BluetoothProfile.STATE_CONNECTING:
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    return HANDLED;
                 
                case EVENT_PAIRING_REQUEST:
                    int pairMode = msg.arg1;
                    final BluetoothDevice pairDevice = (BluetoothDevice) msg.obj;
                    if (pairDevice != null) {
                        String pairAddress = pairDevice.getAddress();
                        if (pairAddress != null && pairAddress.equals(targetAddress)) {
                            switch(pairMode) {
                                case BtUtils.PAIRING_VARIANT_PIN:
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (mBtInterface != null) {
                                                    LogUtil.d(TAG, "mBtInterface != null");
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            
                                        }
                                    }, 1000);
                                    
                                    break;
                                case BtUtils.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (mBtInterface != null) {
                                                    boolean isConfirmed =
                                                            mBtInterface.setPairingConfirmation(true, pairDevice);
                                                    LogUtil.d(TAG, "Passkey Confirmation result = " + isConfirmed);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            
                                            }  
                                        }
                                    }, 300);
                                    break;
                                default:
                                    break;
                            }
                        } 
                    }

                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }
    
    private class PairErrorState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Bt Error state");
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Bt Error state");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch(msg.what) {
                case EVENT_ON_ERROR:
                    int errCode = msg.arg1;
                    BtHfpProtocolHelper.btOOBInfo(STATE_ERROR);
                    if (mBtPairCallback != null) {
                        mBtPairCallback.onPairDone(errCode);
                    }
                    return NOT_HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }
    
    public void startService() {
        if (mBtInterface != null) {
            mBtInterface.init(mBtCallback);
        }
    }
    
    public void enableBluetooth() {
        if (mBtInterface != null) {
            if (mBtInterface.enable()) {
                sendMessage(EVENT_START_BLUETOOTH_SUCCEED);
            } else {
                sendMessage(EVENT_START_BLUETOOTH_FAILED);
            }
        }
    }
    
    public boolean disconnectHfp() {
        return false;
    }
    
    public void onServiceStarted(boolean isStarted) {
        if (isStarted) {
            LogUtil.d(TAG, "Bt Interface service started");
            sendMessage(EVENT_START_SERVICE_SUCCEED);
        } else {
            LogUtil.d(TAG, "Bt Interface service failed");
            sendMessage(EVENT_START_SERVICE_FAILED);
        }
    }
    
    
    
    public void onMDReady() {
        LogUtil.d(TAG, "Received MD ready");
        sendMessage(EVENT_MD_READY);
    }
    
    public void onBondStateChanged(int status, BluetoothDevice device) {
        sendMessage(obtainMessage(EVENT_BOND_STATE_CHANGED, status, 0, device));
    }
    
    public void onConnectionStateChanged(int status, BluetoothDevice device) {
        sendMessage(obtainMessage(EVENT_CONNECTION_STATE_CHANGED, status, 0, device));
    }
    
    public void onPairingRequest(int mode, BluetoothDevice device) {
        sendMessage(obtainMessage(EVENT_PAIRING_REQUEST, mode, 0, device));
    }
}
