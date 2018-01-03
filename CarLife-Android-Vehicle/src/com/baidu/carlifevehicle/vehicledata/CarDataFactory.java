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

/**
 * Created by yangjie11 on 2016/11/14.
 */

public class CarDataFactory {

    public CarDataBase createCarData(int module) {
        CarDataBase carDataObject = null;
        switch(module) {
            case CarDataManager.MODULE_GPS_DATA:
                carDataObject = GpsDataReporter.getInstance();
                break;
            case CarDataManager.MODULE_CAR_VELOCITY:
                carDataObject = CarVelocityReporter.getInstance();
                break;
            default:
                break;
        }

        return carDataObject;
    }

    public CarDataBase createCarData(Class<? extends CarDataBase> clazz) {
        CarDataBase carDataObject = null;
        try {
            carDataObject = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return carDataObject;
    }
}
