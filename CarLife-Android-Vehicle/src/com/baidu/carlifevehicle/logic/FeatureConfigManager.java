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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Pair;

import com.baidu.carlife.protobuf.CarlifeFeatureConfigListProto.CarlifeFeatureConfigList;
import com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;

public class FeatureConfigManager {

    private static final String TAG = "FeatureConfigManager";
    public static final String KEY_VOICE_WAKEUP = CarlifeConfUtil.KEY_BOOL_VOICE_WAKEUP;
    public static final String KEY_VOICE_MIC = CarlifeConfUtil.KEY_INT_VOICE_MIC;
    public static final String KEY_FOCUS_UI = CarlifeConfUtil.KEY_BOOL_FOCUS_UI;
    public static final String KEY_MEDIA_SAMPLE_RATE = CarlifeConfUtil.KEY_INT_MEDIA_SAMPLE_RATE;
    public static final String KEY_BLUETOOTH_AUTO_PAIR = CarlifeConfUtil.KEY_BOOL_BLUETOOTH_AUTO_PAIR;
    public static final String KEY_BLUETOOTH_INTERNAL_UI = CarlifeConfUtil.KEY_BOOL_BLUETOOTH_INTERNAL_UI;
    public static final String KEY_AUDIO_TRANSMISSION_MODE = CarlifeConfUtil.KEY_INT_AUDIO_TRANSMISSION_MODE;
    public static final String KEY_CONTENT_ENCRYPTION = CarlifeConfUtil.KEY_CONTENT_ENCRYPTION;
    public static final String KEY_ENGINE_TYPE = CarlifeConfUtil.KEY_ENGINE_TYPE;
    public static final String KEY_INPUT_DISABLE = CarlifeConfUtil.KEY_BOOL_INPUT_DISABLE;

    public static final int VALUE_USE_VEHICLE_MIC = 0;
    public static final int VALUE_USE_MOBILE_MIC = 1;
    public static final int VALUE_NOT_SUPPORTED = 2;
    public static final int VALUE_ENABLE = 1;
    public static final int VALUE_DISABLE = 0;
    public static final int VALUE_ORIGINAL_SAMPLE_RATE = 0;
    public static final int VALUE_CUSTOMIZED_SAMPLE_RATE_48K = 1;

    private static final Object LOCK = new Object();
    private static FeatureConfigManager mInstance;
    private Context mContext;
    private List<Pair<String, Integer>> mConfigList = new ArrayList<Pair<String, Integer>>();

    private FeatureConfigManager() {
    }

    private MsgBaseHandler mHandler = new MsgBaseHandler() {

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CMD_MD_FEATURE_CONFIG_REQUEST);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonParams.MSG_CMD_MD_FEATURE_CONFIG_REQUEST:
                    LogUtil.d(TAG, "----MSG_CMD_MD_FEATURE_CONFIG_REQUEST----------");
                    sendConfigListToMd();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private void sendConfigListToMd() {
        CarlifeFeatureConfigList.Builder builder = CarlifeFeatureConfigList.newBuilder();
        for (Pair<String, Integer> pair : mConfigList) {
            builder.addFeatureConfig(createConfig(pair));
        }
        builder.setCnt(mConfigList.size());
        CarlifeFeatureConfigList list = builder.build();
        if (list != null && list.getFeatureConfigCount() > 0) {
            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_CMD_HU_FEATURE_CONFIG_RESPONSE);
            command.setData(list.toByteArray());
            command.setLength(list.getSerializedSize());
            Message msgTmp =
                    Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
            ConnectClient.getInstance().sendMsgToService(msgTmp);
        }

    }

    private CarlifeFeatureConfig createConfig(Pair<String, Integer> config) {
        if (null == config || TextUtils.isEmpty(config.first)) {
            return null;
        }
        CarlifeFeatureConfig.Builder builder = CarlifeFeatureConfig.newBuilder();
        builder.setKey(config.first);
        builder.setValue(config.second);
        return builder.build();
    }

    private void customFeature() {
        mConfigList.add(new Pair<String, Integer>(KEY_VOICE_WAKEUP, CarlifeConfUtil.getInstance().getBooleanProperty(
                KEY_VOICE_WAKEUP) ? VALUE_ENABLE : VALUE_DISABLE));
        mConfigList.add(new Pair<String, Integer>(KEY_BLUETOOTH_AUTO_PAIR, CarlifeConfUtil.getInstance()
                .getBooleanProperty(KEY_BLUETOOTH_AUTO_PAIR) ? VALUE_ENABLE : VALUE_DISABLE));
        mConfigList.add(new Pair<String, Integer>(KEY_BLUETOOTH_INTERNAL_UI, CarlifeConfUtil.getInstance()
                .getBooleanProperty(KEY_BLUETOOTH_INTERNAL_UI) ? VALUE_ENABLE : VALUE_DISABLE));
        mConfigList.add(new Pair<String, Integer>(KEY_FOCUS_UI, CarlifeConfUtil.getInstance().getBooleanProperty(
                KEY_FOCUS_UI) ? VALUE_ENABLE : VALUE_DISABLE));
        mConfigList.add(new Pair<String, Integer>(KEY_VOICE_MIC, CarlifeConfUtil.getInstance().getIntProperty(
                KEY_VOICE_MIC)));
        mConfigList.add(new Pair<String, Integer>(KEY_MEDIA_SAMPLE_RATE, CarlifeConfUtil.getInstance().getIntProperty(
                KEY_MEDIA_SAMPLE_RATE)));
        mConfigList.add(new Pair<String, Integer>(KEY_AUDIO_TRANSMISSION_MODE, CarlifeConfUtil.getInstance()
                .getIntProperty(KEY_AUDIO_TRANSMISSION_MODE)));
        mConfigList.add(new Pair<String, Integer>(KEY_CONTENT_ENCRYPTION, CarlifeConfUtil.getInstance()
                .getBooleanProperty(KEY_CONTENT_ENCRYPTION) ? VALUE_ENABLE : VALUE_DISABLE));
        mConfigList.add(new Pair<String, Integer>(KEY_ENGINE_TYPE, CarlifeConfUtil.getInstance().getIntProperty
                (KEY_ENGINE_TYPE)));

        mConfigList.add(new Pair<String, Integer>(KEY_INPUT_DISABLE, CarlifeConfUtil.getInstance().getIntProperty
                (KEY_INPUT_DISABLE)));
    }

    public static FeatureConfigManager getInstance() {
        if (null == mInstance) {
            synchronized (LOCK) {
                if (null == mInstance) {
                    mInstance = new FeatureConfigManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        MsgHandlerCenter.registerMessageHandler(mHandler);
        customFeature();
    }

}
