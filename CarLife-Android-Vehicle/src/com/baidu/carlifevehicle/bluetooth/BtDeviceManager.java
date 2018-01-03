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

import com.baidu.carlife.protobuf.CarlifeBTIdentifyResultIndProto.CarlifeBTIdentifyResultInd;
import com.baidu.carlife.protobuf.CarlifeBTStartPairReqProto.CarlifeBTStartPairReq;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.bluetooth.BtPairStateMachine.BtPairCallback;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

public class BtDeviceManager extends StateMachine {
    private static final String TAG = BtDeviceManager.class.getSimpleName();

    private IdleState mIdleState;
    private CarlifeConnectingState mCarlifeConnectingState;
    private CarlifeConnectedState mCarlifeConnectedState;
    private CarlifePairingState mCarlifePairingState;
    private CarlifeBtTeleState mCarlifeBtTeleState;

    public static final int EVENT_BASE = 0;
    public static final int EVENT_USB_CONNECTED = EVENT_BASE + 1;
    public static final int EVENT_USB_DISCONNECTED = EVENT_BASE + 2;
    public static final int EVENT_CARLIFE_AUTHENTICATED = EVENT_BASE + 3;
    public static final int EVENT_START_PAIR = EVENT_BASE + 4;
    public static final int EVENT_FINISH_PAIR = EVENT_BASE + 5;
    public static final int EVENT_START_HFPCLIENT = EVENT_BASE + 6;
    public static final int EVENT_STOP_HFPCLIENT = EVENT_BASE + 7;
    public static final int EVENT_PAIR_DONE = EVENT_BASE + 8;
    public static final int EVENT_START_IDENTIFY = EVENT_BASE + 9;
    public static final int EVENT_CARLIFE_EXIT = EVENT_BASE + 10;
    public static final int EVENT_FINISH_FEATURE_CONFIG = EVENT_BASE + 11;
    public static final int EVENT_OUTGOING_CALL = EVENT_BASE + 12;
    public static final int EVENT_INCOMING_CALL = EVENT_BASE + 13;
    public static final int EVENT_CALL_ACTIVE = EVENT_BASE + 14;
    public static final int EVENT_CALL_INACTIVE = EVENT_BASE + 15;
    public static final int EVENT_MULTICALL_ACTIVE = EVENT_BASE + 16;

    private String btMdAddress = "";
    private static BtDeviceManager mInstance = null;

    private BtPairStateMachine mBtPairStateMachine;
    private DeviceMgrHandler mgrHandler = null;
    private Context mContext = null;

    public static boolean isBtTeleUISupport = false;
    public static boolean isBtAutoPairingSupport = false;

    public BtDeviceManager(String name) {
        super(TAG, Looper.getMainLooper());

        mIdleState = new IdleState();
        mCarlifeConnectingState = new CarlifeConnectingState();
        mCarlifeConnectedState = new CarlifeConnectedState();
        mCarlifePairingState = new CarlifePairingState();
        mCarlifeBtTeleState = new CarlifeBtTeleState();

        addState(mIdleState);
        addState(mCarlifeConnectingState, mIdleState);
        addState(mCarlifeConnectedState, mIdleState);
        addState(mCarlifePairingState, mCarlifeConnectedState);
        addState(mCarlifeBtTeleState, mCarlifeConnectedState);

        mgrHandler = new DeviceMgrHandler();
        MsgHandlerCenter.registerMessageHandler(mgrHandler);

        setInitialState(mIdleState);
    }

    public static BtDeviceManager getInstance() {
        if (null == mInstance) {
            synchronized (BtDeviceManager.class) {
                if (null == mInstance) {
                    mInstance = new BtDeviceManager(TAG);
                }
            }
        }
        return mInstance;
    }

    public void init(Context cx) {
        mContext = cx;
        LogUtil.d(TAG, "BT device manager initialization");

        if (BtHfpManager.getInstance().init(mContext)) {
            LogUtil.d(TAG, "Bind to Bluetooth HFP serivce");
            isBtTeleUISupport = true;

        }
        start();
    }

    public void uninit() {

        quit();

        if (mgrHandler != null) {
            MsgHandlerCenter.unRegisterMessageHandler(mgrHandler);
        }
        /**
         *  User exit Carlife when in BT Telephone state
         */
        if (getCurrentState() == mCarlifeBtTeleState) {
            BtHfpManager.getInstance().stop();
        }

        if (isBtTeleUISupport) {
            BtHfpManager.getInstance().uninit();
            isBtTeleUISupport = false;
        }

    }

    private class DeviceMgrHandler extends MsgBaseHandler {
        
        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_BT_IDENTIFY_RESULT_IND);
            addMsg(CommonParams.MSG_CMD_START_BT_AUTOPAIR_REQUEST);
            addMsg(CommonParams.MSG_CMD_MD_FEATURE_CONFIG_REQUEST);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonParams.MSG_CMD_MD_FEATURE_CONFIG_REQUEST:
                    onFeatureConfigRequest();
                    break;
                case CommonParams.MSG_CMD_BT_IDENTIFY_RESULT_IND:
                    CarlifeCmdMessage identifyResultMessage = (CarlifeCmdMessage) msg.obj;
                    CarlifeBTIdentifyResultInd btIdentifyResultInd = null;
                    try {
                        btIdentifyResultInd = CarlifeBTIdentifyResultInd.parseFrom(identifyResultMessage.getData());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (btIdentifyResultInd == null) {
                        return;
                    }

                    int connStatus = btIdentifyResultInd.getStatus();
                    String addr = btIdentifyResultInd.getAddress();
                    String adapterAddr = BtUtils.getBtAddress();
                    LogUtil.d(TAG, "MD--->HU: IDENTIFY_RESULT_IND, address = " + addr + ",adapter address = "
                            + adapterAddr);
                    if (!TextUtils.isEmpty(addr) && !TextUtils.isEmpty(adapterAddr)) {
                        if (addr.equals(adapterAddr)) {
                            if (connStatus == BtHfpManager.BT_HFP_IDENTIFY_SUCCEED) {
                                LogUtil.d(TAG, "Carlife MD connected with HU");
                                if (isBtTeleUISupport && !BtHfpManager.getInstance().isServiceRunning) {
                                    onHfpIdentified(true);
                                }

                            } else if (connStatus == BtHfpManager.BT_HFP_IDENTIFY_FAILED) {
                                LogUtil.d(TAG, "Carlife MD disconnected with HU");
                                if (isBtTeleUISupport && BtHfpManager.getInstance().isServiceRunning) {
                                    onHfpIdentified(false);
                                }
                            }
                        }
                    }
                    break;
                case CommonParams.MSG_CMD_START_BT_AUTOPAIR_REQUEST:
                    CarlifeCmdMessage startPairMessage = (CarlifeCmdMessage) msg.obj;
                    CarlifeBTStartPairReq btStartPairReq = null;
                    try {
                        btStartPairReq = CarlifeBTStartPairReq.parseFrom(startPairMessage.getData());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (btStartPairReq == null) {
                        return;
                    }

                    String pairAddr = btStartPairReq.getAddress();
                    LogUtil.d(TAG, "MD--->HU: START_PAIR_REQUEST, address = " + pairAddr);
                    break;

                default:
                    break;
            }
        }
    }

    private class IdleState extends State {

        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter Idle State");
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave Idle State");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_USB_CONNECTED:
                    transitionTo(mCarlifeConnectingState);
                    return HANDLED;
                case EVENT_USB_DISCONNECTED:
                    transitionTo(mIdleState);
                    return HANDLED;
                case EVENT_CARLIFE_AUTHENTICATED:
                case EVENT_START_PAIR:
                case EVENT_FINISH_PAIR:
                case EVENT_START_HFPCLIENT:
                case EVENT_STOP_HFPCLIENT:
                default:
                    return NOT_HANDLED;
            }
        }

    }

    private class CarlifeConnectingState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter CarlifeConnecting State");
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave CarlifeConnecting State");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_USB_DISCONNECTED:
                    transitionTo(mIdleState);
                    return HANDLED;
                case EVENT_CARLIFE_AUTHENTICATED:
                    transitionTo(mCarlifeConnectedState);
                    return HANDLED;
                case EVENT_USB_CONNECTED:
                case EVENT_START_PAIR:
                case EVENT_FINISH_PAIR:
                case EVENT_START_HFPCLIENT:
                case EVENT_STOP_HFPCLIENT:
                default:
                    return NOT_HANDLED;
            }
        }
    }

    private class CarlifeConnectedState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter CarlifeConnected State");

        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave CarlifeConnected State");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_START_PAIR:
                    deferMessage(msg);
                    transitionTo(mCarlifePairingState);
                    return HANDLED;
                case EVENT_START_HFPCLIENT:
                    transitionTo(mCarlifeBtTeleState);
                    return HANDLED;
                case EVENT_FINISH_FEATURE_CONFIG:
                    if (CarlifeConfUtil.getInstance()
                            .getBooleanProperty(CarlifeConfUtil.KEY_BOOL_BLUETOOTH_INTERNAL_UI) && isBtTeleUISupport) {
                        LogUtil.d(TAG, "Read Config: Internal Tele UI is supported");
                        BtHfpProtocolHelper.btStartIdentify(BtUtils.getBtAddress());

                    } else {
                        LogUtil.d(TAG, "Read Config: Internal Tele UI is NOT supported");
                    }
                    return HANDLED;
                case EVENT_START_IDENTIFY:
                    return HANDLED;
                case EVENT_CARLIFE_AUTHENTICATED:
                    LogUtil.d(TAG, "Received Authenticated event in Carlife Connected State");
                    return HANDLED;
                case EVENT_USB_DISCONNECTED:
                case EVENT_USB_CONNECTED:

                case EVENT_FINISH_PAIR:

                case EVENT_STOP_HFPCLIENT:
                default:
                    return NOT_HANDLED;
            }
        }
    }

    private class CarlifePairingState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter CarlifePairing State");

        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave CarlifePairing State");
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_START_PAIR:
                    String address = (String) msg.obj;
                    mBtPairStateMachine = BtPairStateMachine.make(address, mContext, mPairCallback);
                    return HANDLED;
                case EVENT_PAIR_DONE:
                    int pairStatus = msg.arg1;
                    if (pairStatus == BtPairStateMachine.NO_ERROR) {
                        LogUtil.d(TAG, "Auto pair done in success");

                    } else {
                        LogUtil.d(TAG, "Auto pair done in failure, err = " + pairStatus);

                    }
                    if (mBtPairStateMachine != null) {
                        mBtPairStateMachine.doQuit();
                        mBtPairStateMachine.cleanup();
                    }
                    transitionTo(mCarlifeConnectedState);
                    return HANDLED;
                case EVENT_USB_DISCONNECTED:
                case EVENT_CARLIFE_AUTHENTICATED:
                case EVENT_USB_CONNECTED:

                case EVENT_FINISH_PAIR:
                case EVENT_START_HFPCLIENT:
                case EVENT_STOP_HFPCLIENT:
                default:
                    return NOT_HANDLED;
            }
        }
    }

    private class CarlifeBtTeleState extends State {
        @Override
        public void enter() {
            LogUtil.d(TAG, "Enter CarlifeBtTele State");
            BtHfpManager.getInstance().start();
        }

        @Override
        public void exit() {
            LogUtil.d(TAG, "Leave CarlifeBtTele State");
            BtHfpManager.getInstance().stop();
        }

        @Override
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case EVENT_STOP_HFPCLIENT:
                    transitionTo(mCarlifeConnectedState);
                    return HANDLED;
                case EVENT_OUTGOING_CALL:
                    String outNumber = (String) msg.obj;
                    BtHfpProtocolHelper.btHfpCallStateIndication(BtHfpManager.BT_HFP_OUT_CALL, outNumber);
                    return HANDLED;
                case EVENT_INCOMING_CALL:
                    String inNumber = (String) msg.obj;
                    BtHfpProtocolHelper.btHfpCallStateIndication(BtHfpManager.BT_HFP_NEW_CALL, inNumber);
                    return HANDLED;
                case EVENT_CALL_INACTIVE:
                    BtHfpProtocolHelper.btHfpCallStateIndication(BtHfpManager.BT_HFP_NO_CALL_ACTIVE);
                    return HANDLED;
                case EVENT_CALL_ACTIVE:
                    BtHfpProtocolHelper.btHfpCallStateIndication(BtHfpManager.BT_HFP_CALL_ACTIVE);
                    return HANDLED;
                case EVENT_MULTICALL_ACTIVE:
                    return HANDLED;
                default:
                    return NOT_HANDLED;
            }
        }
    }

    public void onUsbConnected() {
        LogUtil.d(TAG, "USB connected");
        sendMessage(EVENT_USB_CONNECTED);
    }

    public void onUsbDisconnected() {
        LogUtil.d(TAG, "USB Disconnected");
        sendMessage(EVENT_USB_DISCONNECTED);
    }

    public void onCarlifeAuthenticated() {
        LogUtil.d(TAG, "Carlife Authenticated");
        sendMessage(EVENT_CARLIFE_AUTHENTICATED);
    }

    public void onStartPair(String address) {
        LogUtil.d(TAG, "Start auto pairing");
        if (!TextUtils.isEmpty(address)) {
            btMdAddress = address;
            Message msg = obtainMessage(EVENT_START_PAIR, address);
            sendMessage(msg);
        }
    }

    public void onPairDone(int status) {
        LogUtil.d(TAG, "Auto pairing Done");
        sendMessage(obtainMessage(EVENT_PAIR_DONE, status, 0));
    }

    public void onHfpIdentified(boolean isIdentified) {
        if (isIdentified) {
            sendMessage(EVENT_START_HFPCLIENT);
        } else {
            sendMessage(EVENT_STOP_HFPCLIENT);
        }
    }

    public void onFeatureConfigRequest() {
        LogUtil.d(TAG, "Received Feature Config Request");
        sendMessageDelayed(EVENT_FINISH_FEATURE_CONFIG, 200);
    }

    public void onOutgoingCall(String phoneNum) {
        LogUtil.d(TAG, "Outgoing call indication from Native BT module");
        Message msg = obtainMessage(EVENT_OUTGOING_CALL, phoneNum);
        sendMessage(msg);

    }

    public void onIncomingCall(String phoneNum) {
        LogUtil.d(TAG, "Incoming call indication from Native BT module");
        Message msg = obtainMessage(EVENT_INCOMING_CALL, phoneNum);
        sendMessage(msg);
    }

    public void onCallActive() {
        LogUtil.d(TAG, "Call Active indication from Native BT Module");
        sendMessage(EVENT_CALL_ACTIVE);
    }

    public void onCallInactive() {
        LogUtil.d(TAG, "Call ");
        sendMessage(EVENT_CALL_INACTIVE);
    }

    private BtPairCallback mPairCallback = new BtPairCallback() {

        @Override
        public void onPairDone(int state) {
            onPairDone(state);
        }
    };
}
