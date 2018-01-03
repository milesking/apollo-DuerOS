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
package com.baidu.carlifevehicle.encryption;

import com.baidu.carlife.protobuf.CarlifeHuRsaPublicKeyResponseProto;
import com.baidu.carlife.protobuf.CarlifeMdAesKeyRequestProto;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.google.protobuf.InvalidProtocolBufferException;

import android.os.Message;

/**
 * Created by liucaiquan on 2017/2/21.
 */

public class EncryptSetupManager {
    // AES encryption is turned on
    private Boolean mIsEnableEncryption = false;
    private static EncryptSetupManager mInstance;
    private RSAManager mRSAManager = new RSAManager();
    // The default AES key
    private String mAESKey = "1234567890123456";

    public static EncryptSetupManager getInstance() {
        if (mInstance == null) {
            mInstance = new EncryptSetupManager();
        }

        return mInstance;
    }

    // Receive mobile request, return Rsa public key
    public void sendRsaPublicKey() {
        CarlifeHuRsaPublicKeyResponseProto.CarlifeHuRsaPublicKeyResponse.Builder
                builder = CarlifeHuRsaPublicKeyResponseProto.CarlifeHuRsaPublicKeyResponse.newBuilder();

        builder.setRsaPublicKey(mRSAManager.getPublicKeyString());
        CarlifeHuRsaPublicKeyResponseProto.CarlifeHuRsaPublicKeyResponse
                rsaPublicKeyResponse = builder.build();

        CarlifeCmdMessage msg = new CarlifeCmdMessage(true);
        msg.setServiceType(CommonParams.MSG_CMD_HU_RSA_PUBLIC_KEY_RESPONSE);
        msg.setData(rsaPublicKeyResponse.toByteArray());
        msg.setLength(rsaPublicKeyResponse.getSerializedSize());

        Message msgSend = Message.obtain(null, msg.getServiceType(),
                CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, msg);
        ConnectClient.getInstance().sendMsgToService(msgSend);
    }

    // Receive Aes public key of mobile, return Done news
    public void getAESKey(Message msg) {
        CarlifeCmdMessage aesKey = (CarlifeCmdMessage) msg.obj;

        CarlifeMdAesKeyRequestProto.CarlifeMdAesKeyRequest aesKeyRequest = null;

        try {
            aesKeyRequest = CarlifeMdAesKeyRequestProto.CarlifeMdAesKeyRequest.parseFrom(aesKey.getData());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        if (aesKeyRequest == null) {
            return;
        }

        String aesKeyStr = mRSAManager.decrypt(aesKeyRequest.getAesKey(), mRSAManager.getPrivateKey());
        setAesKey(aesKeyStr);
        DebugLogUtil.getInstance().println("AES key: " + getAesKey());
    }

    public boolean isEncryptEnable() {
        if (!EncryptConfig.DEBUG_ENABLE) {
            return false;
        }

        if (EncryptConfig.AES_ENCRYPT_AS_BEGINE) {
            return true;
        }
        return mIsEnableEncryption;
    }

    public void setEncryptSwitch(boolean isEnable) {
        mIsEnableEncryption = isEnable;
    }

    public String getAesKey() {
        return mAESKey;
    }

    public void setAesKey(String key) {
        mAESKey = key;
    }

    public void onDisConnection() {
        setEncryptSwitch(false);
    }

    private EncryptSetupManager() {

    }
}
