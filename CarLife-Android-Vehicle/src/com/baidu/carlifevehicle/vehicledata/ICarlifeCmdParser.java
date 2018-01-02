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
 * Interface definition for a callback to be invoked on reception of Car Data Subscribe
 * Created by yangjie11 on 2016/11/14.
 */
public interface ICarlifeCmdParser {
    /**
     * On reception of Car_Data_Subscribe_Req from mobile device
     * HU should construct corresponding Car_Data reporter and feed back with response
     * @param module module id, can be one of follows: <br/>
     *               {@link CarDataManager#MODULE_GPS_DATA} <br/>
     *               {@link CarDataManager#MODULE_CAR_VELOCITY}
     * @param option flag
     * @param freq frequency, in hz
     */
    public void onRequest(int module, int option, int freq);

    /**
     * on reception of Car Data Subscribe start
     * @param module module id, can be one of follows: <br/>
     *               {@link CarDataManager#MODULE_GPS_DATA} <br/>
     *               {@link CarDataManager#MODULE_CAR_VELOCITY}
     * @param option flag
     * @param freq frequency, in hz
     */
    public void onStart(int module, int option, int freq);

    /**
     * on reception of Car Data Subscribe stop
     * @param module module id, can be one of follows: <br/>
     *               {@link CarDataManager#MODULE_GPS_DATA} <br/>
     *               {@link CarDataManager#MODULE_CAR_VELOCITY}
     * @param option flag
     * @param freq frequency, in hz
     */
    public void onStop(int module, int option, int freq);
}
