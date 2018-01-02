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

import com.baidu.carlife.protobuf.CarlifeBTHfpConnectionProto.CarlifeBTHfpConnection;
import com.baidu.carlife.protobuf.CarlifeBTHfpIndicationProto.CarlifeBTHfpIndication;
import com.baidu.carlife.protobuf.CarlifeBTHfpResponseProto.CarlifeBTHfpResponse;
import com.baidu.carlife.protobuf.CarlifeBTHfpStatusResponseProto.CarlifeBTHfpStatusResponse;
import com.baidu.carlife.protobuf.CarlifeBTPairInfoProto.CarlifeBTPairInfo;
import com.baidu.carlife.protobuf.CarlifeBTStartIdentifyReqProto.CarlifeBTStartIdentifyReq;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

import android.os.Message;
import android.text.TextUtils;

public class BtHfpProtocolHelper {
    private static final String TAG = BtHfpProtocolHelper.class.getSimpleName();

    public static void btStartIdentify(String address) {
        CarlifeBTStartIdentifyReq btStartIdentifyReq = buildBtStartIdentifyReq(address);
        if (btStartIdentifyReq != null) {
            btStartIdentifyReqToMD(btStartIdentifyReq);
            LogUtil.d(TAG, "MD<---HU: Start Identify,address = " + address);
        }
    }
    
    public static void btOOBInfo(int status) {
        CarlifeBTPairInfo btPairInfo = buildBluetoothInfo(status);
        if (btPairInfo != null) {
            sendBluetoothInfoToMd(btPairInfo);
            LogUtil.d(TAG, "MD<---HU: BT OOB Info,status = " + status);
        }
    }

    public static void sendBluetoothInfoToMd(CarlifeBTPairInfo carlifeBTPairInfo) {
        if (carlifeBTPairInfo == null) {
            return;
        }
        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_HU_BT_OOB_INFO);
        btCommand.setData(carlifeBTPairInfo.toByteArray());
        btCommand.setLength(carlifeBTPairInfo.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);

    }

    
    
    public static CarlifeBTPairInfo buildBluetoothInfo(int status) {
        
        CarlifeBTPairInfo.Builder builder = CarlifeBTPairInfo.newBuilder();
        if (builder == null) {
            return null;
        }
        String address = BtUtils.getBtAddress();
        if (!TextUtils.isEmpty(address)) {
            builder.setAddress(address);
        } else {
            builder.setAddress("");
        }
        
        builder.setName("");
        builder.setStatus(status);
        builder.setUuid("00001101-0000-1000-8000-00805F9B34FB");
        builder.setPassKey("1234");
        builder.setRandomizer("1234");
        CarlifeBTPairInfo bluetoothInfo = builder.build();
        return bluetoothInfo;
    }
    
    public static void btHfpStatusResponse(int type, int status) {
        CarlifeBTHfpStatusResponse btHfpStatusResponse = buildBtHfpStatusResponse(type, status);
        if (btHfpStatusResponse != null) {
            btHfpStatusResponseToMD(btHfpStatusResponse);
            LogUtil.d(TAG, "MD<---HU: HFP Status Response : type = " + type + ",status = " + status);
        }

    }

    public static void btHfpResponse(int cmdID, int status) {
        CarlifeBTHfpResponse btHfpResponse = buildBtHfpResponse(status, cmdID);
        if (btHfpResponse != null) {
            btHfpResponseToMD(btHfpResponse);
            LogUtil.d(TAG, "MD<---HU: HFP Response : status = " + status + ",cmd = " + cmdID);
        }
    }

    public static void btHfpResponse(int cmdID, int status, int code) {
        CarlifeBTHfpResponse btHfpResponse = buildBtHfpResponse(status, cmdID, code);
        if (btHfpResponse != null) {
            btHfpResponseToMD(btHfpResponse);
            LogUtil.d(TAG, "MD<---HU: HFP Response : status = " + status + ",cmd = " + cmdID + ",DTMFcode = " + code);
        }
    }

    public static void btHfpConnStateIndication(int state, String address) {
        CarlifeBTHfpConnection btHFPConnection = buildBtHfpConnection(state, address);
        if (btHFPConnection != null) {
            btHfpConnectionToMD(btHFPConnection);
            LogUtil.d(TAG, "MD<---HU: HFP Connection Status Indication : " + state);
        }
    }

    public static void btHfpCallStateIndication(int state) {
        CarlifeBTHfpIndication btHFPIndication = bulidBTHfpIndication(state);
        if (btHFPIndication != null) {
            btHfpIndicationToMD(btHFPIndication);
            LogUtil.d(TAG, "MD<---HU: HFP Call Status Indication : " + state);
        }
    }

    public static void btHfpCallStateIndication(int state, String phoneNum) {
        CarlifeBTHfpIndication btHFPIndication = bulidBTHfpIndication(state, phoneNum);
        if (btHFPIndication != null) {
            btHfpIndicationToMD(btHFPIndication);
            LogUtil.d(TAG, "MD<---HU: HFP Call Status Indication : " + state);
        }
    }

    private static CarlifeBTHfpStatusResponse buildBtHfpStatusResponse(int type, int status) {
        CarlifeBTHfpStatusResponse.Builder builder = CarlifeBTHfpStatusResponse.newBuilder();
        if (builder == null) {
            return null;
        }

        builder.setType(type);
        builder.setStatus(status);
        return builder.build();
    }

    private static CarlifeBTHfpResponse buildBtHfpResponse(int status, int cmdID) {
        CarlifeBTHfpResponse.Builder builder = CarlifeBTHfpResponse.newBuilder();
        if (builder == null) {
            return null;
        }

        builder.setCmd(cmdID);
        builder.setStatus(status);
        return builder.build();
    }

    private static CarlifeBTHfpResponse buildBtHfpResponse(int status, int cmdID, int code) {
        CarlifeBTHfpResponse.Builder builder = CarlifeBTHfpResponse.newBuilder();
        if (builder == null) {
            return null;
        }

        builder.setCmd(cmdID);
        builder.setStatus(status);
        builder.setDtmfCode(code);
        return builder.build();
    }

    private static CarlifeBTHfpIndication bulidBTHfpIndication(int state) {
        CarlifeBTHfpIndication.Builder builder = CarlifeBTHfpIndication.newBuilder();
        if (builder == null) {
            return null;
        }

        builder.setState(state);
        return builder.build();
    }

    private static CarlifeBTHfpIndication bulidBTHfpIndication(int state, String phoneNum) {
        CarlifeBTHfpIndication.Builder builder = CarlifeBTHfpIndication.newBuilder();
        if (builder == null) {
            return null;
        }
        if (!TextUtils.isEmpty(phoneNum)) {
            builder.setPhoneNum(phoneNum);
        } else {
            builder.setPhoneNum("");
        }
        builder.setState(state);
        return builder.build();
    }

    private static CarlifeBTHfpConnection buildBtHfpConnection(int state, String address) {
        CarlifeBTHfpConnection.Builder builder = CarlifeBTHfpConnection.newBuilder();

        if (builder == null) {
            return null;
        }
        if (!TextUtils.isEmpty(address)) {
            builder.setAddress(address);
        } else {
            builder.setAddress("");
        }

        builder.setState(state);
        return builder.build();
    }

    private static CarlifeBTStartIdentifyReq buildBtStartIdentifyReq(String address) {
        CarlifeBTStartIdentifyReq.Builder builder = CarlifeBTStartIdentifyReq
                .newBuilder();

        if (builder == null) {
            return null;
        }
        if (!TextUtils.isEmpty(address)) {
            builder.setAddress(address);
        } else {
            builder.setAddress("unknown");
        }

        return builder.build();
    }
    
    private static void btHfpResponseToMD(CarlifeBTHfpResponse btHfpResponse) {
        if (btHfpResponse == null) {
            return;
        }

        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_BT_HFP_RESPONSE);
        btCommand.setData(btHfpResponse.toByteArray());
        btCommand.setLength(btHfpResponse.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);
    }

    private static void btHfpIndicationToMD(CarlifeBTHfpIndication btHFPIndication) {
        if (btHFPIndication == null) {
            return;
        }
        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_BT_HFP_INDICATION);
        btCommand.setData(btHFPIndication.toByteArray());
        btCommand.setLength(btHFPIndication.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);
    }

    private static void btHfpConnectionToMD(CarlifeBTHfpConnection btHFPConnection) {
        if (btHFPConnection == null) {
            return;
        }

        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_BT_HFP_CONNECTION);
        btCommand.setData(btHFPConnection.toByteArray());
        btCommand.setLength(btHFPConnection.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);

    }

    private static void btHfpStatusResponseToMD(CarlifeBTHfpStatusResponse btHfpStatusResponse) {
        if (btHfpStatusResponse == null) {
            return;
        }

        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_BT_HFP_STATUS_RESPONSE);
        btCommand.setData(btHfpStatusResponse.toByteArray());
        btCommand.setLength(btHfpStatusResponse.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);
    }
    
    private static void btStartIdentifyReqToMD(CarlifeBTStartIdentifyReq btStartIdentifyReq) {
        if (btStartIdentifyReq == null) {
            return;
        }

        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_BT_START_IDENTIFY_REQ);
        btCommand.setData(btStartIdentifyReq.toByteArray());
        btCommand.setLength(btStartIdentifyReq.getSerializedSize());
        Message msgBt = Message.obtain(null, btCommand.getServiceType(),
                CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);
    }
 
}
