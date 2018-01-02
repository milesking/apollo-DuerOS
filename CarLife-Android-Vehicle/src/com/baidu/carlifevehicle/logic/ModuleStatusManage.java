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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.util.LogUtil;
import com.baidu.carlifevehicle.util.PreferenceUtil;

public class ModuleStatusManage {
    private static final String TAG = "ModuleStatusManage";
    public static final int CARLIFE_HOME_MODULE_ID = 0;
    public static final int CARLIFE_PHONE_MODULE_ID = 1;
    public static final int CARLIFE_NAVI_MODULE_ID = 2;
    public static final int CARLIFE_MUSIC_MODULE_ID = 3;
    public static final int CARLIFE_RECORD_MODULE_ID = 4;
    public static final String CARLIFE_HOME_MODULE = "carlife_home";
    public static final String CARLIFE_PHONE_MODULE = "carlife_phone";
    public static final String CARLIFE_NAVI_MODULE = "carlife_navi";
    public static final String CARLIFE_MUSIC_MODULE = "carlife_music";
    public static final String CARLIFE_RECORD_MODULE = "carlife_record";

    public static void initModuleStatus() {
        try {
            PreferenceUtil.getInstance().putInt(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    CARLIFE_PHONE_MODULE, -1);
            PreferenceUtil.getInstance().putInt(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    CARLIFE_NAVI_MODULE, -1);
            PreferenceUtil.getInstance().putInt(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    CARLIFE_MUSIC_MODULE, -1);
            PreferenceUtil.getInstance().putInt(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    CARLIFE_RECORD_MODULE, -1);
        } catch (Exception ex) {
            LogUtil.e(TAG, "save module status error");
            ex.printStackTrace();
        }
    }

    public static void saveModuleStatus(int moduleId, int statusId) {
        try {
            PreferenceUtil.getInstance().putInt(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    ModuleStatusManage.getKeyById(moduleId), statusId);
        } catch (Exception ex) {
            LogUtil.e(TAG, "save module status error");
            ex.printStackTrace();
        }
    }

    /**
     * get key according to module Id
     *
     * @param moduleId module id, can be one of follows: <br/>
     *                 {@link ModuleStatusManage#CARLIFE_HOME_MODULE} <br/>
     *                 {@link ModuleStatusManage#CARLIFE_RECORD_MODULE} <br/>
     *                 {@link ModuleStatusManage#CARLIFE_PHONE_MODULE} <br/>
     *                 {@link ModuleStatusManage#CARLIFE_MUSIC_MODULE} <br/>
     *                 {@link ModuleStatusManage#CARLIFE_NAVI_MODULE}
     * @return key as string
     */
    public static String getKeyById(int moduleId) {
        String key = null;
        switch (moduleId) {
            case CARLIFE_HOME_MODULE_ID:
                key = CARLIFE_HOME_MODULE;
                break;
            case CARLIFE_PHONE_MODULE_ID:
                key = CARLIFE_PHONE_MODULE;
                break;
            case CARLIFE_NAVI_MODULE_ID:
                key = CARLIFE_NAVI_MODULE;
                break;
            case CARLIFE_MUSIC_MODULE_ID:
                key = CARLIFE_MUSIC_MODULE;
                break;
            case CARLIFE_RECORD_MODULE_ID:
                key = CARLIFE_RECORD_MODULE;
                break;

            default:
                break;
        }
        return key;
    }
}
