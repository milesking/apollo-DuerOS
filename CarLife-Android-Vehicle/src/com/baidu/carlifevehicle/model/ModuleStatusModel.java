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
package com.baidu.carlifevehicle.model;

public class ModuleStatusModel {

    public static final int CARLIFE_PHONE_MODULE_ID = 1;
    public static final int PHONE_STATUS_IDLE = 0;
    public static final int PHONE_STATUS_INCOMING = 1;
    public static final int PHONE_STATUS_OUTGOING = 2;
    

    public static final int CARLIFE_NAVI_MODULE_ID = 2;
    public static final int NAVI_STATUS_IDLE = 0;
    public static final int NAVI_STATUS_START = 1;
    public static final int NAVI_STATUS_STOP = 2;

    public static final int CARLIFE_MUSIC_MODULE_ID = 3;
    public static final int MUSIC_STATUS_IDLE = 0;
    public static final int MUSIC_STATUS_RUNNING = 1;

    public static final int CARLIFE_VR_MODULE_ID = 4;
    public static final int VR_STATUS_IDLE = 0;
    public static final int VR_STATUS_RUNNING = 1;
    @Deprecated
    public static final int VR_STATUS_MIC_NOT_SUPPORTED = 2;
    /**
     * connect type, only used for ios mobile devices
     * */
    public static final int CARLIFE_CONNECT_MODULE_ID = 5;

    public static final int CARLIFE_MIC_MODULE_ID = 6;
    public static final int MIC_STATUS_USE_VEHICLE_MIC = 0;
    public static final int MIC_STATUS_USE_MOBILE_MIC = 1;
    public static final int MIC_STATUS_NOT_SUPPORTED = 2;

    private int moduleId;
    private int statusId;

    public ModuleStatusModel(int module, int status) {
        this.moduleId = module;
        this.statusId = status;
    }

    public int getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(int value) {
        this.moduleId = value;
    }

    public int getStatusId() {
        return this.statusId;
    }

    public void setStatusId(int value) {
        this.statusId = value;
    }
}
