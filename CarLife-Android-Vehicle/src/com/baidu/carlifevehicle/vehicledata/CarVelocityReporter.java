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

import static com.baidu.carlifevehicle.vehicledata.CarDataManager.MODULE_CAR_VELOCITY;

import com.baidu.carlife.protobuf.CarlifeVehicleInfoProto;

/**
 * Created by yangjie11 on 2016/11/14.
 */

public class CarVelocityReporter extends CarDataBase {

    private static CarVelocityReporter mInstance = null;

    public static CarVelocityReporter getInstance() {

        if (mInstance == null) {
            synchronized(CarVelocityReporter.class) {
                if (null == mInstance) {
                    mInstance = new CarVelocityReporter();
                }
            }
        }
        return mInstance;

    }

    public void init() {

    }

    public static boolean isReportable() {
        return false;
    }

    public CarlifeVehicleInfoProto.CarlifeVehicleInfo buildResponse() {
        CarlifeVehicleInfoProto.CarlifeVehicleInfo.Builder gpsCarDataBuilder =
                CarlifeVehicleInfoProto.CarlifeVehicleInfo.newBuilder();

        gpsCarDataBuilder.setModuleID(MODULE_CAR_VELOCITY);
        gpsCarDataBuilder.setFlag(0);
        gpsCarDataBuilder.setFrequency(1);
        return gpsCarDataBuilder.build();

    }

    @Override
    public void startReport(int freq) {

    }

    @Override
    public void stopReport() {

    }
}
