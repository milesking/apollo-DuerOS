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

import android.bluetooth.BluetoothAdapter;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.carlife.protobuf.CarlifeDeviceInfoProto.CarlifeDeviceInfo;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

public class CarlifeDeviceInfoManager {

    public static final String TAG = "CarlifeDeviceInfoManager";

    private static CarlifeDeviceInfoManager mInstance = null;
    private static CarlifeDeviceInfo mDeviceInfo = null;
    private static CarlifeDeviceInfo mPhoneDeviceInfo = null;

    private CarlifeDeviceInfoManager() {

    }

    public static CarlifeDeviceInfoManager getInstance() {
        if (null == mInstance) {
            synchronized (CarlifeDeviceInfoManager.class) {
                if (null == mInstance) {
                    mInstance = new CarlifeDeviceInfoManager();
                }
            }
        }
        return mInstance;
    }

    public void init() {
        try {
            CarlifeDeviceInfo.Builder builder = CarlifeDeviceInfo.newBuilder();
            builder.setOs(CommonParams.TYPE_OF_OS);
            builder.setBoard(android.os.Build.BOARD);
            builder.setBootloader(android.os.Build.BOOTLOADER);
            builder.setBrand(android.os.Build.BRAND);
            builder.setCpuAbi(android.os.Build.CPU_ABI);
            builder.setCpuAbi2(android.os.Build.CPU_ABI2);
            builder.setDevice(android.os.Build.DEVICE);
            builder.setDisplay(android.os.Build.DISPLAY);
            builder.setFingerprint(android.os.Build.FINGERPRINT);
            builder.setHardware(android.os.Build.HARDWARE);
            builder.setHost(android.os.Build.HOST);
            builder.setCid(android.os.Build.ID);
            builder.setManufacturer(android.os.Build.MANUFACTURER);
            builder.setModel(android.os.Build.MODEL);
            builder.setProduct(android.os.Build.PRODUCT);
            builder.setSerial(android.os.Build.SERIAL);

            builder.setCodename(android.os.Build.VERSION.CODENAME);
            builder.setIncremental(android.os.Build.VERSION.INCREMENTAL);
            builder.setRelease(android.os.Build.VERSION.RELEASE);
            builder.setSdk(android.os.Build.VERSION.SDK);
            builder.setSdkInt(android.os.Build.VERSION.SDK_INT);

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                String addrString = mBluetoothAdapter.getAddress();
                if (!TextUtils.isEmpty(addrString)) {
                    builder.setBtaddress(addrString);
                } else {
                    builder.setBtaddress("unknow");
                }
                
            } else {
                builder.setBtaddress("unknow");
            }
            mDeviceInfo = builder.build();
        } catch (Exception ex) {
            LogUtil.e(TAG, "init error");
            ex.printStackTrace();
        }
    }

    public CarlifeDeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    public void setPhoneDeviceInfo(CarlifeDeviceInfo info) {
        mPhoneDeviceInfo = info;
    }

    public CarlifeDeviceInfo getPhoneDeviceInfo() {
        return mPhoneDeviceInfo;
    }

    public void sendCarlifeDeviceInfo() {
        try {
            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_CMD_HU_INFO);
            command.setData(mDeviceInfo.toByteArray());
            command.setLength(mDeviceInfo.getSerializedSize());
            Message msgTmp =
                    Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
            ConnectClient.getInstance().sendMsgToService(msgTmp);
        } catch (Exception ex) {
            LogUtil.e(TAG, "send hu info error");
            ex.printStackTrace();
        }
    }
}
