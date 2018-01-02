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
package com.baidu.carlifevehicle.vehicledata;

import com.baidu.carlife.protobuf.CarlifeVehicleInfoListProto;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.os.Message;

/**
 * Created by yangjie11 on 2016/11/14.
 */

public class CarDataManager implements ICarlifeCmdParser {
    public static final String TAG = "CarData";
    public static final int MODULE_GPS_DATA = 0;
    public static final int MODULE_CAR_VELOCITY = 1;
    private static CarDataManager mInstance = null;

    private GpsDataReporter mGpsDataReportor;
    private CarVelocityReporter mCarVelocityReporter;
    private Context mContext = null;


    private CarDataManager() {
        LogUtil.d(TAG, "Construct CarDataManager");
    }

    public static CarDataManager getInstance() {
        if (mInstance == null) {
            synchronized (CarDataManager.class) {
                if (mInstance == null) {
                    mInstance = new CarDataManager();
                }
            }
        }

        return mInstance;
    }

    public boolean init(Context cx) {
        LogUtil.d(TAG, "init CarDataManager");
        mContext = cx;
        return true;
    }

    public boolean uninit() {
        LogUtil.d(TAG, "uninit CarDataManager");
        if (mGpsDataReportor != null) {
            mGpsDataReportor.stopReport();
        }

        if (mCarVelocityReporter != null) {
            mCarVelocityReporter.stopReport();
        }

        return true;
    }

    @Override
    public void onRequest(int module, int option, int freq) {
        LogUtil.d(TAG, "on reception of Car Data Subscribe request");
        CarDataFactory factory = new CarDataFactory();
        if (GpsDataReporter.isReportable()) {
            mGpsDataReportor = (GpsDataReporter) factory.createCarData(MODULE_GPS_DATA);
            mGpsDataReportor.init(mContext);
        }

        if (CarVelocityReporter.isReportable()) {
            mCarVelocityReporter = (CarVelocityReporter) factory.createCarData(MODULE_CAR_VELOCITY);
            mCarVelocityReporter.init();
        }
        sendResponse();
    }

    @Override
    public void onStart(int module, int option, int freq) {
        LogUtil.d(TAG, "on reception of Car Data Subscribe start");
        // Parameter checking

        // start reporting process
        if (module == MODULE_GPS_DATA) {
            GpsDataReporter.getInstance().startReport(freq);
        } else if (module == MODULE_CAR_VELOCITY) {
            LogUtil.d(TAG, "on reception of Car Data Subscribe start module == MODULE_CAR_VELOCITY");
        }
    }

    @Override
    public void onStop(int module, int option, int freq) {
        LogUtil.d(TAG, "on reception of Car Data Subscribe stop");
        if (module == MODULE_GPS_DATA) {
            GpsDataReporter.getInstance().stopReport();
        } else if (module == MODULE_CAR_VELOCITY) {
            LogUtil.d(TAG, "on reception of Car Data Subscribe stop :module == MODULE_CAR_VELOCITY");
        }
    }

    private CarlifeVehicleInfoListProto.CarlifeVehicleInfoList buildResponse() {
        CarlifeVehicleInfoListProto.CarlifeVehicleInfoList.Builder builder =
                CarlifeVehicleInfoListProto.CarlifeVehicleInfoList.newBuilder();
        if (mGpsDataReportor != null) {
            builder.addVehicleInfo(mGpsDataReportor.buildResponse());
        }

        if (mCarVelocityReporter != null) {
            builder.addVehicleInfo(mCarVelocityReporter.buildResponse());
        }

        builder.setCnt(builder.getVehicleInfoCount());
        CarlifeVehicleInfoListProto.CarlifeVehicleInfoList list = builder.build();
        if (list != null && list.getVehicleInfoCount() > 0) {
            return list;
        } else {
            return null;
        }
    }

    private void sendResponse() {
        CarlifeVehicleInfoListProto.CarlifeVehicleInfoList list = buildResponse();
        if (list == null) {
            return;
        }
        if (!ConnectClient.getInstance().isCarlifeConnected()) {
            return;
        }
        LogUtil.d(TAG, "Send out Car Data Response");
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_CAR_DATA_SUBSCRIBE_RSP);
        command.setData(list.toByteArray());
        command.setLength(list.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

}
