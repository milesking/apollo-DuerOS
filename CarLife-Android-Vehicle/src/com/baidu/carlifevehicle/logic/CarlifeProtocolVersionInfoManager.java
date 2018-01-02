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
package com.baidu.carlifevehicle.logic;

import com.baidu.carlife.protobuf.CarlifeProtocolVersionMatchStatusProto.CarlifeProtocolVersionMatchStatus;
import com.baidu.carlife.protobuf.CarlifeProtocolVersionProto.CarlifeProtocolVersion;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

import android.os.Message;

public class CarlifeProtocolVersionInfoManager {

    public static final String TAG = "CarlifeProtocolInfoManager";

    private static CarlifeProtocolVersionInfoManager mInstance = null;
    private static CarlifeProtocolVersion mMdProtocolVersion = null;
    private static CarlifeProtocolVersion mHuProtocolVersion = null;
    private static CarlifeProtocolVersionMatchStatus mProtocolMatchStatus = null;

    private CarlifeProtocolVersionInfoManager() {

    }

    public static CarlifeProtocolVersionInfoManager getInstance() {
        if (null == mInstance) {
            synchronized (CarlifeProtocolVersionInfoManager.class) {
                if (null == mInstance) {
                    mInstance = new CarlifeProtocolVersionInfoManager();
                }
            }
        }
        return mInstance;
    }

    public void init() {
        CarlifeProtocolVersion.Builder builder = CarlifeProtocolVersion.newBuilder();
        builder.setMajorVersion(CommonParams.PROTOCOL_VERSION_MAJOR_VERSION);
        builder.setMinorVersion(CommonParams.PROTOCOL_VERSION_MINOR_VERSION);

        mHuProtocolVersion = builder.build();
    }

    public CarlifeProtocolVersion getHuProtocolVersion() {
        return mHuProtocolVersion;
    }

    public void setMdProtocolVersion(CarlifeProtocolVersion info) {
        mMdProtocolVersion = info;
    }

    public CarlifeProtocolVersion getMdProtocolVersion() {
        return mMdProtocolVersion;
    }

    public void setProtocolMatchStatus(CarlifeProtocolVersionMatchStatus info) {
        mProtocolMatchStatus = info;
    }

    public CarlifeProtocolVersionMatchStatus getProtocolMatchStatus() {
        return mProtocolMatchStatus;
    }

    public void sendProtocolMatchStatus() {
        try {
            CarlifeCmdMessage protocolM = new CarlifeCmdMessage(true);
            protocolM.setServiceType(CommonParams.MSG_CMD_HU_PROTOCOL_VERSION);
            protocolM.setData(mHuProtocolVersion.toByteArray());
            protocolM.setLength(mHuProtocolVersion.getSerializedSize());
            Message protocolMsg = Message.obtain(null, protocolM.getServiceType(),
                    CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, protocolM);
            ConnectClient.getInstance().sendMsgToService(protocolMsg);
        } catch (Exception ex) {
            LogUtil.e(TAG, "sendProtocolMatchStatus fail");
            ex.printStackTrace();
        }
    }
}
