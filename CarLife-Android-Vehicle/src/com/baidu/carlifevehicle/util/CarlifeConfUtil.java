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
package com.baidu.carlifevehicle.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.touch.TouchListenerManager;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;

public class CarlifeConfUtil {

    private static final String TAG = "CarlifeConfUtil";
    private static final String CONF_FILE_DIR = "/data/local/tmp";
    private static final String CONF_FILE = "bdcf";

    private static final int CONF_FILE_READ_SUCCESS = 0;
    private static final int CONF_FILE_NOT_EXISTS = 1;
    private static final int CONF_FILE_READ_EXCEPTION = 2;

    private static boolean isReadConfSuccess = false;

    private static final int MAX_TIME_READ_CONF = 5;
    private int readConfCnt = 0;

    /**
     *  How many audio tracks Carlife Vehicle will apply for Music and TTS playback
     */
    public static int VALUE_INT_AUDIO_TRACK_NUM = 2;
    /**
     * Audio track stream type for TTS channel
     */
    public static int VALUE_INT_AUDIO_TRACK_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    /**
     * Whether or not require audio focus when playing back TTS
     */
    public static boolean VALUE_BOOL_AUDIO_FOCUS_REQUIRED = false;
    /**
     * 0 normal stereo(luchang, huayang, feige) <br/>
     * 1 mono(zhangxun) <br/>
     * 2 customized stereo(BYD, yuanfeng) <br/>
     * 3 customized stereo(changan) <br/>
     * 4 baidu stereo <br/>
     */
    public static int VALUE_INT_AUDIO_TRACK_TYPE = 0;
    /**
     * 0 head unit mic <br/>
     * 1 mobile device mic<br/>
     * 2 not supported <br/>
     */
    public static int VALUE_INT_VOICE_MIC = 0;
    /**
     * whether support voice wake up
     */
    public static boolean VALUE_BOOL_VOICE_WAKEUP = true;
    /**
     * if set to true, the wait time for input2Decoder is 1000 ms
     */
    public static boolean VALUE_BOOL_NEED_MORE_DECODE_TIME = false;
    /**
     * whether support internal ui
     */
    public static boolean VALUE_BOOL_BLUETOOTH_INTERNAL_UI = false;
    /**
     * whether support bluetooth auto pair
     */
    public static boolean VALUE_BOOL_BLUETOOTH_AUTO_PAIR = false;
    /**
     * whether send touch event to mobile device
     */
    public static boolean VALUE_BOOL_TRANSPARENT_SEND_TOUCH_EVENT = true;
    /**
     * whether send {@link android.view.MotionEvent#ACTION_DOWN} as the first touch event
     */
    public static boolean VALUE_BOOL_SEND_ACTION_DOWN = false;
    /**
     * whether use vehicle gps
     */
    public static boolean VALUE_BOOL_VEHICLE_GPS = false;
    /**
     * gps format
     */
    public static int VALUE_INT_GPS_FORMAT = 1;
    /**
     * whether support focus ui
     */
    public static boolean VALUE_BOOL_FOCUS_UI = false;
    /**
     * 0 according to mobile device <br/>
     * 1 48k
     */
    public static int VALUE_INT_MEDIA_SAMPLE_RATE = 0;
    /**
     * android device connect type: <br/>
     * 0 adb <br/>
     * 1 aoa
     */
    public static int VALUE_INT_CONNECT_TYPE_ANDROID = 1;
    /**
     * apple device connect type: <br/>
     * 0 ncm <br/>
     * 1 wifi <br/>
     * 2 ncm & wifi
     */
    public static int VALUE_INT_CONNECT_TYPE_IPHONE = 4;
    /**
     * apple device usb connect type: <br/>
     * 0 ipv6 <br/>
     * 1 ipv4 <br/>
     * 2 usb sharing
     */
    public static int VALUE_INT_IPHONE_USB_CONNECT_TYPE = 0;

    public static String VALUE_STRING_IPHONE_NCM_ETHERNET_NAME = "usb0";

    /**
     * media stream type: <br/>
     * 0 specific tcp channel <br/>
     * 1 bluetooth
     */
    public static int VALUE_INT_AUDIO_TRANSMISSION_MODE = 0;

    /**
     * whether support encryption
     */
    public static boolean VALUE_CONTENT_ENCRYPTION = false;

    /**
     * car power type: <br/>
     * 0 oil <br/>
     * 1 electric
     */
    public static int VALUE_ENGINE_TYPE = 0;

    /**
     * whether to disable input, 1 for disable
     */
    public static int VALUE_IS_INPUT_DISABLE = 1;


    public static final String KEY_INT_AUDIO_TRACK_NUM = "AUDIO_TRACK_NUM";
    public static final String KEY_INT_AUDIO_TRACK_STREAM_TYPE = "AUDIO_TRACK_STREAM_TYPE";
    public static final String KEY_BOOL_TTS_REQUEST_AUDIO_FOCUS = "AUDIO_TTS_REQUEST_FOCUS";
    public static final String KEY_INT_AUDIO_TRACK_TYPE = "AUDIO_TRACK_TYPE";
    public static final String KEY_INT_VOICE_MIC = "VOICE_MIC";
    public static final String KEY_BOOL_VOICE_WAKEUP = "VOICE_WAKEUP";
    public static final String KEY_BOOL_NEED_MORE_DECODE_TIME = "NEED_MORE_DECODE_TIME";
    public static final String KEY_BOOL_BLUETOOTH_INTERNAL_UI = "BLUETOOTH_INTERNAL_UI";
    public static final String KEY_BOOL_BLUETOOTH_AUTO_PAIR = "BLUETOOTH_AUTO_PAIR";
    public static final String KEY_BOOL_TRANSPARENT_SEND_TOUCH_EVENT = "TRANSPARENT_SEND_TOUCH_EVENT";
    public static final String KEY_BOOL_SEND_ACTION_DOWN = "SEND_ACTION_DOWN";
    public static final String KEY_BOOL_VEHICLE_GPS = "VEHICLE_GPS";
    public static final String KEY_INT_GPS_FORMAT = "GPS_FORMAT";
    public static final String KEY_BOOL_FOCUS_UI = "FOCUS_UI";
    public static final String KEY_INT_MEDIA_SAMPLE_RATE = "MEDIA_SAMPLE_RATE";
    public static final String KEY_INT_CONNECT_TYPE_ANDROID = "CONNECT_TYPE_ANDROID";
    public static final String KEY_INT_CONNECT_TYPE_IPHONE = "CONNECT_TYPE_IPHONE";
    public static final String KEY_INT_IPHONE_USB_CONNECT_TYPE = "IPHONE_USB_CONNECT_TYPE";
    public static final String KEY_STRING_IPHONE_NCM_ETHERNET_NAME = "IPHONE_NCM_ETHERNET_NAME";
    public static final String KEY_INT_AUDIO_TRANSMISSION_MODE = "AUDIO_TRANSMISSION_MODE";
    public static final String KEY_CONTENT_ENCRYPTION = "CONTENT_ENCRYPTION";
    public  static final String KEY_BOOL_INPUT_DISABLE = "INPUT_DISABLE";
    public static final String KEY_ENGINE_TYPE = "ENGINE_TYPE";

    /**
     * key code supported
     */
    public static final String KEY_PREFIX_HARD_KEY = "KEYCODE_";
    public static final String KEY_KEYCODE_HOME = "KEYCODE_HOME";
    public static final String KEY_KEYCODE_PHONE_CALL = "KEYCODE_PHONE_CALL";
    public static final String KEY_KEYCODE_PHONE_END = "KEYCODE_PHONE_END";
    public static final String KEY_KEYCODE_PHONE_END_MUTE = "KEYCODE_PHONE_END_MUTE";
    public static final String KEY_KEYCODE_HFP = "KEYCODE_HFP";
    public static final String KEY_KEYCODE_SELECTOR_NEXT = "KEYCODE_SELECTOR_NEXT";
    public static final String KEY_KEYCODE_SELECTOR_PREVIOUS = "KEYCODE_SELECTOR_PREVIOUS";
    public static final String KEY_KEYCODE_SETTING = "KEYCODE_SETTING";
    public static final String KEY_KEYCODE_MEDIA = "KEYCODE_MEDIA";
    public static final String KEY_KEYCODE_RADIO = "KEYCODE_RADIO";
    public static final String KEY_KEYCODE_NAVI = "KEYCODE_NAVI";
    public static final String KEY_KEYCODE_SRC = "KEYCODE_SRC";
    public static final String KEY_KEYCODE_MODE = "KEYCODE_MODE";
    public static final String KEY_KEYCODE_BACK = "KEYCODE_BACK";
    public static final String KEY_KEYCODE_SEEK_SUB = "KEYCODE_SEEK_SUB";
    public static final String KEY_KEYCODE_SEEK_ADD = "KEYCODE_SEEK_ADD";
    public static final String KEY_KEYCODE_VOLUME_SUB = "KEYCODE_VOLUME_SUB";
    public static final String KEY_KEYCODE_VOLUME_ADD = "KEYCODE_VOLUME_ADD";
    public static final String KEY_KEYCODE_MUTE = "KEYCODE_MUTE";
    public static final String KEY_KEYCODE_OK = "KEYCODE_OK";
    public static final String KEY_KEYCODE_MOVE_LEFT = "KEYCODE_MOVE_LEFT";
    public static final String KEY_KEYCODE_MOVE_RIGHT = "KEYCODE_MOVE_RIGHT";
    public static final String KEY_KEYCODE_MOVE_UP = "KEYCODE_MOVE_UP";
    public static final String KEY_KEYCODE_MOVE_DOWN = "KEYCODE_MOVE_DOWN";
    public static final String KEY_KEYCODE_MOVE_UP_LEFT = "KEYCODE_MOVE_UP_LEFT";
    public static final String KEY_KEYCODE_MOVE_UP_RIGHT = "KEYCODE_MOVE_UP_RIGHT";
    public static final String KEY_KEYCODE_MOVE_DOWN_LEFT = "KEYCODE_MOVE_DOWN_LEFT";
    public static final String KEY_KEYCODE_MOVE_DOWN_RIGHT = "KEYCODE_MOVE_DOWN_RIGHT";
    public static final String KEY_KEYCODE_TEL = "KEYCODE_TEL";
    public static final String KEY_KEYCODE_MAIN = "KEYCODE_MAIN";
    public static final String KEY_KEYCODE_MEDIA_START = "KEYCODE_MEDIA_START";
    public static final String KEY_KEYCODE_MEDIA_STOP = "KEYCODE_MEDIA_STOP";
    public static final String KEY_KEYCODE_VR_START = "KEYCODE_VR_START";
    public static final String KEY_KEYCODE_VR_STOP = "KEYCODE_VR_STOP";
    public static final String KEY_KEYCODE_NUMBER_0 = "KEYCODE_NUMBER_0";
    public static final String KEY_KEYCODE_NUMBER_1 = "KEYCODE_NUMBER_1";
    public static final String KEY_KEYCODE_NUMBER_2 = "KEYCODE_NUMBER_2";
    public static final String KEY_KEYCODE_NUMBER_3 = "KEYCODE_NUMBER_3";
    public static final String KEY_KEYCODE_NUMBER_4 = "KEYCODE_NUMBER_4";
    public static final String KEY_KEYCODE_NUMBER_5 = "KEYCODE_NUMBER_5";
    public static final String KEY_KEYCODE_NUMBER_6 = "KEYCODE_NUMBER_6";
    public static final String KEY_KEYCODE_NUMBER_7 = "KEYCODE_NUMBER_7";
    public static final String KEY_KEYCODE_NUMBER_8 = "KEYCODE_NUMBER_8";
    public static final String KEY_KEYCODE_NUMBER_9 = "KEYCODE_NUMBER_9";
    public static final String KEY_KEYCODE_NUMBER_STAR = "KEYCODE_NUMBER_STAR";
    public static final String KEY_KEYCODE_NUMBER_POUND = "KEYCODE_NUMBER_POUND";
    public static final String KEY_KEYCODE_NUMBER_ADD = "KEYCODE_NUMBER_ADD";
    public static final String KEY_KEYCODE_NUMBER_DEL = "KEYCODE_NUMBER_DEL";
    public static final String KEY_KEYCODE_NUMBER_CLEAR = "KEYCODE_NUMBER_CLEAR";

    private static CarlifeConfUtil mInstance = null;
    private Context mContext = null;

    private String channelId = null;
    private HashMap<String, String> propertyMap = null;

    public static CarlifeConfUtil getInstance() {
        if (null == mInstance) {
            synchronized (CarlifeConfUtil.class) {
                if (null == mInstance) {
                    mInstance = new CarlifeConfUtil();
                }
            }
        }
        return mInstance;
    }

    public CarlifeConfUtil() {
    }

    public void init() {
        readConfCnt++;
        new Thread() {
            @Override
            public void run() {
                LogUtil.e(TAG, "begin to read bdcf");
                isReadConfSuccess = false;
                int readStatus = readBdcf();
                if (readStatus == CONF_FILE_READ_SUCCESS) {
                    LogUtil.e(TAG, "read conf form bdcf success");
                } else if (readStatus == CONF_FILE_NOT_EXISTS) {
                    LogUtil.e(TAG, "read conf form bdcf fail: file not exist");
                    isReadConfSuccess = false;
                    return;
                } else if (readStatus == CONF_FILE_READ_EXCEPTION) {
                    LogUtil.e(TAG, "read conf form bdcf fail: get exception");
                    isReadConfSuccess = false;
                    return;
                }
                if (!TextUtils.isEmpty(channelId)) {
                    LogUtil.e(TAG, "read channel id form bdcf: " + channelId);
                    CommonParams.VEHICLE_CHANNEL = channelId;
                } else {
                    LogUtil.e(TAG, "read channel id form bdcf fail");
                    isReadConfSuccess = false;
                    return;
                }
                LogUtil.e(TAG, "channel = " + CommonParams.VEHICLE_CHANNEL);

                if (updateProperty()) {
                    LogUtil.d(TAG, "update property success");
                } else {
                    LogUtil.d(TAG, "update property fail");
                    isReadConfSuccess = false;
                    return;
                }
                isReadConfSuccess = true;
                LogUtil.e(TAG, "end to read bdcf");
            }
        }.start();
    }

    private int readBdcf() {
        int readStatus = CONF_FILE_READ_SUCCESS;
        File bdcf = null;
        BufferedReader reader = null;
        String line = null;
        String[] keyValue = null;
        int linenum = 1;

        try {
            bdcf = new File(CONF_FILE_DIR + "/" + CONF_FILE);
            reader = new BufferedReader(new FileReader(bdcf));
            propertyMap = new HashMap<String, String>();

            while ((line = reader.readLine()) != null) {
                LogUtil.d(TAG, "line " + linenum + ": " + line);
                linenum++;
                if (!line.startsWith("#")) {
                    channelId = line.trim();
                    break;
                }
            }
            while ((line = reader.readLine()) != null) {
                LogUtil.d(TAG, "line " + linenum + ": " + line);
                linenum++;
                line = line.trim().replace(" ", "");
                if (!line.isEmpty() && !line.startsWith("#")) {
                    keyValue = line.split("=");
                    propertyMap.put(keyValue[0], keyValue[1]);
                }
            }
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, "[ERROR]read to file exception 1");
            e.printStackTrace();
            readStatus = CONF_FILE_NOT_EXISTS;
        } catch (Exception e) {
            LogUtil.e(TAG, "[ERROR]read file exception");
            e.printStackTrace();
            readStatus = CONF_FILE_READ_EXCEPTION;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return readStatus;
    }

    private boolean updateProperty() {

        if (propertyMap == null) {
            LogUtil.e(TAG, "propertyMap is null");
            return false;
        }

        try {
            if (propertyMap.containsKey(KEY_INT_CONNECT_TYPE_ANDROID)) {
                LogUtil.e(TAG, "Android phones only support AOA connections");
            }
            if (propertyMap.containsKey(KEY_INT_CONNECT_TYPE_IPHONE)) {
                VALUE_INT_CONNECT_TYPE_IPHONE = getIntFromMap(KEY_INT_CONNECT_TYPE_IPHONE);
                LogUtil.e(TAG, "VALUE_INT_CONNECT_TYPE_IPHONE = " + VALUE_INT_CONNECT_TYPE_IPHONE);
            }
            if (propertyMap.containsKey(KEY_INT_IPHONE_USB_CONNECT_TYPE)) {
                VALUE_INT_IPHONE_USB_CONNECT_TYPE = getIntFromMap(KEY_INT_IPHONE_USB_CONNECT_TYPE);
                LogUtil.e(TAG, "VALUE_INT_IPHONE_USB_CONNECT_TYPE = " + VALUE_INT_IPHONE_USB_CONNECT_TYPE);
            }
            if (propertyMap.containsKey(KEY_STRING_IPHONE_NCM_ETHERNET_NAME)) {
                VALUE_STRING_IPHONE_NCM_ETHERNET_NAME = getStringFromMap(KEY_STRING_IPHONE_NCM_ETHERNET_NAME);
                LogUtil.e(TAG, "VALUE_STRING_IPHONE_NCM_ETHERNET_NAME = " + VALUE_STRING_IPHONE_NCM_ETHERNET_NAME);
            }
            if (propertyMap.containsKey(KEY_INT_AUDIO_TRACK_NUM)) {
                VALUE_INT_AUDIO_TRACK_NUM = getIntFromMap(KEY_INT_AUDIO_TRACK_NUM);
                LogUtil.d(TAG, "VALUE_INT_AUDIO_TRACK_NUM = " + VALUE_INT_AUDIO_TRACK_NUM);
            }

            if (propertyMap.containsKey(KEY_INT_AUDIO_TRACK_TYPE)) {
                VALUE_INT_AUDIO_TRACK_TYPE = getIntFromMap(KEY_INT_AUDIO_TRACK_TYPE);
                LogUtil.d(TAG, "VALUE_INT_AUDIO_TRACK_TYPE = " + VALUE_INT_AUDIO_TRACK_TYPE);
            }
            if (propertyMap.containsKey(KEY_INT_AUDIO_TRACK_NUM)) {
                VALUE_INT_AUDIO_TRACK_NUM = getIntFromMap(KEY_INT_AUDIO_TRACK_NUM);
                LogUtil.d(TAG, "VALUE_INT_AUDIO_TRACK_NUM = " + VALUE_INT_AUDIO_TRACK_NUM);
            }
            if (propertyMap.containsKey(KEY_INT_AUDIO_TRACK_STREAM_TYPE)) {
                VALUE_INT_AUDIO_TRACK_STREAM_TYPE = getIntFromMap(KEY_INT_AUDIO_TRACK_STREAM_TYPE);
                LogUtil.d(TAG, "VALUE_INT_AUDIO_TRACK_STREAM_TYPE = " + VALUE_INT_AUDIO_TRACK_STREAM_TYPE);
            }
            if (propertyMap.containsKey(KEY_BOOL_TTS_REQUEST_AUDIO_FOCUS)) {
                VALUE_BOOL_AUDIO_FOCUS_REQUIRED = getBooleanFromMap(KEY_BOOL_TTS_REQUEST_AUDIO_FOCUS);
                LogUtil.d(TAG, "VALUE_BOOL_AUDIO_FOCUS_REQUIRED = " + VALUE_BOOL_AUDIO_FOCUS_REQUIRED);
            }

            if (propertyMap.containsKey(KEY_INT_VOICE_MIC)) {
                VALUE_INT_VOICE_MIC = getIntFromMap(KEY_INT_VOICE_MIC);
                LogUtil.d(TAG, "VALUE_INT_VOICE_MIC = " + VALUE_INT_VOICE_MIC);
            }
            if (propertyMap.containsKey(KEY_BOOL_VOICE_WAKEUP)) {
                VALUE_BOOL_VOICE_WAKEUP = getBooleanFromMap(KEY_BOOL_VOICE_WAKEUP);
                LogUtil.d(TAG, "VALUE_BOOL_VOICE_WAKEUP = " + VALUE_BOOL_VOICE_WAKEUP);
            }
            if (propertyMap.containsKey(KEY_BOOL_NEED_MORE_DECODE_TIME)) {
                VALUE_BOOL_NEED_MORE_DECODE_TIME = getBooleanFromMap(KEY_BOOL_NEED_MORE_DECODE_TIME);
                LogUtil.d(TAG, "VALUE_BOOL_NEED_MORE_DECODE_TIME = " + VALUE_BOOL_NEED_MORE_DECODE_TIME);
            }
            if (propertyMap.containsKey(KEY_BOOL_BLUETOOTH_INTERNAL_UI)) {
                VALUE_BOOL_BLUETOOTH_INTERNAL_UI = getBooleanFromMap(KEY_BOOL_BLUETOOTH_INTERNAL_UI);
                LogUtil.d(TAG, "VALUE_BOOL_BLUETOOTH_INTERNAL_UI = " + VALUE_BOOL_BLUETOOTH_INTERNAL_UI);
            }
            if (propertyMap.containsKey(KEY_BOOL_BLUETOOTH_AUTO_PAIR)) {
                VALUE_BOOL_BLUETOOTH_AUTO_PAIR = getBooleanFromMap(KEY_BOOL_BLUETOOTH_AUTO_PAIR);
                LogUtil.d(TAG, "VALUE_BOOL_BLUETOOTH_AUTO_PAIR = " + VALUE_BOOL_BLUETOOTH_AUTO_PAIR);
            }
            if (propertyMap.containsKey(KEY_BOOL_TRANSPARENT_SEND_TOUCH_EVENT)) {
                VALUE_BOOL_TRANSPARENT_SEND_TOUCH_EVENT = getBooleanFromMap(KEY_BOOL_TRANSPARENT_SEND_TOUCH_EVENT);
                LogUtil.d(TAG, "VALUE_BOOL_TRANSPARENT_SEND_TOUCH_EVENT = " + VALUE_BOOL_TRANSPARENT_SEND_TOUCH_EVENT);
            }
            if (propertyMap.containsKey(KEY_BOOL_SEND_ACTION_DOWN)) {
                VALUE_BOOL_SEND_ACTION_DOWN = getBooleanFromMap(KEY_BOOL_SEND_ACTION_DOWN);
                LogUtil.d(TAG, "VALUE_BOOL_SEND_ACTION_DOWN = " + VALUE_BOOL_SEND_ACTION_DOWN);
            }
            if (propertyMap.containsKey(KEY_BOOL_VEHICLE_GPS)) {
                VALUE_BOOL_VEHICLE_GPS = getBooleanFromMap(KEY_BOOL_VEHICLE_GPS);
                LogUtil.d(TAG, "VALUE_BOOL_VEHICLE_GPS = " + VALUE_BOOL_VEHICLE_GPS);
            }

            if (propertyMap.containsKey(KEY_INT_GPS_FORMAT)) {
                VALUE_INT_GPS_FORMAT = getIntFromMap(KEY_INT_GPS_FORMAT);
                LogUtil.d(TAG, "VALUE_INT_GPS_FORMAT = " + VALUE_INT_GPS_FORMAT);
            }

            if (propertyMap.containsKey(KEY_BOOL_FOCUS_UI)) {
                VALUE_BOOL_FOCUS_UI = getBooleanFromMap(KEY_BOOL_FOCUS_UI);
                LogUtil.d(TAG, "VALUE_BOOL_FOCUS_UI = " + VALUE_BOOL_FOCUS_UI);
            }
            if (propertyMap.containsKey(KEY_INT_MEDIA_SAMPLE_RATE)) {
                VALUE_INT_MEDIA_SAMPLE_RATE = getIntFromMap(KEY_INT_MEDIA_SAMPLE_RATE);
                LogUtil.d(TAG, "VALUE_INT_MEDIA_SAMPLE_RATE = " + VALUE_INT_MEDIA_SAMPLE_RATE);
            }
            if (propertyMap.containsKey(KEY_INT_AUDIO_TRANSMISSION_MODE)) {
                VALUE_INT_AUDIO_TRANSMISSION_MODE = getIntFromMap(KEY_INT_AUDIO_TRANSMISSION_MODE);
                LogUtil.d(TAG, "VALUE_INT_AUDIO_TRANSMISSION_MODE = " + VALUE_INT_AUDIO_TRANSMISSION_MODE);
            }

            if (propertyMap.containsKey(KEY_CONTENT_ENCRYPTION)) {
                VALUE_CONTENT_ENCRYPTION = getBooleanFromMap(KEY_CONTENT_ENCRYPTION);
            }

            if (propertyMap.containsKey(KEY_ENGINE_TYPE)) {
                VALUE_ENGINE_TYPE = getIntFromMap(KEY_ENGINE_TYPE);
            }

            if (propertyMap.containsKey(KEY_BOOL_INPUT_DISABLE)) {
                VALUE_IS_INPUT_DISABLE = getIntFromMap(KEY_BOOL_INPUT_DISABLE);
            }

        } catch (Exception ex) {
            LogUtil.d(TAG, "update property get exception");
            return false;
        }

        return true;
    }

    private boolean getBooleanFromMap(String key) {
        if (propertyMap == null) {
            LogUtil.e(TAG, "propertyMap is null");
            return false;
        }

        String value = null;
        try {
            value = propertyMap.get(key);
            if (value != null && value.equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            LogUtil.d(TAG, "get property fail: " + key);
            ex.printStackTrace();
            return false;
        }
    }

    private int getIntFromMap(String key) {
        if (propertyMap == null) {
            LogUtil.e(TAG, "propertyMap is null");
            return Integer.MIN_VALUE;
        }

        String value = null;
        // try {
        value = propertyMap.get(key);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return Integer.MIN_VALUE;
        }
    }

    private String getStringFromMap(String key) {
        if (propertyMap == null) {
            LogUtil.e(TAG, "propertyMap is null");
            return null;
        }

        String value = null;
        try {
            value = propertyMap.get(key);
            if (value != null) {
                return value;
            } else {
                return null;
            }
        } catch (Exception ex) {
            LogUtil.d(TAG, "get property fail: " + key);
            ex.printStackTrace();
            return null;
        }
    }

    public int getHardKeyCode(String key) {
        if (key.equals(KEY_KEYCODE_HOME)) {
            return CommonParams.KEYCODE_HOME;
        } else if (key.equals(KEY_KEYCODE_PHONE_CALL)) {
            return CommonParams.KEYCODE_PHONE_CALL;
        } else if (key.equals(KEY_KEYCODE_PHONE_END)) {
            return CommonParams.KEYCODE_PHONE_END;
        } else if (key.equals(KEY_KEYCODE_PHONE_END_MUTE)) {
            return CommonParams.KEYCODE_PHONE_END_MUTE;
        } else if (key.equals(KEY_KEYCODE_HFP)) {
            return CommonParams.KEYCODE_HFP;
        } else if (key.equals(KEY_KEYCODE_SELECTOR_NEXT)) {
            return CommonParams.KEYCODE_SELECTOR_NEXT;
        } else if (key.equals(KEY_KEYCODE_SELECTOR_PREVIOUS)) {
            return CommonParams.KEYCODE_SELECTOR_PREVIOUS;
        } else if (key.equals(KEY_KEYCODE_SETTING)) {
            return CommonParams.KEYCODE_SETTING;
        } else if (key.equals(KEY_KEYCODE_MEDIA)) {
            return CommonParams.KEYCODE_MEDIA;
        } else if (key.equals(KEY_KEYCODE_RADIO)) {
            return CommonParams.KEYCODE_RADIO;
        } else if (key.equals(KEY_KEYCODE_NAVI)) {
            return CommonParams.KEYCODE_NAVI;
        } else if (key.equals(KEY_KEYCODE_SRC)) {
            return CommonParams.KEYCODE_SRC;
        } else if (key.equals(KEY_KEYCODE_MODE)) {
            return CommonParams.KEYCODE_MODE;
        } else if (key.equals(KEY_KEYCODE_BACK)) {
            return CommonParams.KEYCODE_BACK;
        } else if (key.equals(KEY_KEYCODE_SEEK_SUB)) {
            return CommonParams.KEYCODE_SEEK_SUB;
        } else if (key.equals(KEY_KEYCODE_SEEK_ADD)) {
            return CommonParams.KEYCODE_SEEK_ADD;
        } else if (key.equals(KEY_KEYCODE_VOLUME_SUB)) {
            return CommonParams.KEYCODE_VOLUME_SUB;
        } else if (key.equals(KEY_KEYCODE_VOLUME_ADD)) {
            return CommonParams.KEYCODE_VOLUME_ADD;
        } else if (key.equals(KEY_KEYCODE_MUTE)) {
            return CommonParams.KEYCODE_MUTE;
        } else if (key.equals(KEY_KEYCODE_OK)) {
            return CommonParams.KEYCODE_OK;
        } else if (key.equals(KEY_KEYCODE_MOVE_LEFT)) {
            return CommonParams.KEYCODE_MOVE_LEFT;
        } else if (key.equals(KEY_KEYCODE_MOVE_RIGHT)) {
            return CommonParams.KEYCODE_MOVE_RIGHT;
        } else if (key.equals(KEY_KEYCODE_MOVE_UP)) {
            return CommonParams.KEYCODE_MOVE_UP;
        } else if (key.equals(KEY_KEYCODE_MOVE_DOWN)) {
            return CommonParams.KEYCODE_MOVE_DOWN;
        } else if (key.equals(KEY_KEYCODE_MOVE_UP_LEFT)) {
            return CommonParams.KEYCODE_MOVE_UP_LEFT;
        } else if (key.equals(KEY_KEYCODE_MOVE_UP_RIGHT)) {
            return CommonParams.KEYCODE_MOVE_UP_RIGHT;
        } else if (key.equals(KEY_KEYCODE_MOVE_DOWN_LEFT)) {
            return CommonParams.KEYCODE_MOVE_DOWN_LEFT;
        } else if (key.equals(KEY_KEYCODE_MOVE_DOWN_RIGHT)) {
            return CommonParams.KEYCODE_MOVE_DOWN_RIGHT;
        } else if (key.equals(KEY_KEYCODE_TEL)) {
            return CommonParams.KEYCODE_TEL;
        } else if (key.equals(KEY_KEYCODE_MAIN)) {
            return CommonParams.KEYCODE_MAIN;
        } else if (key.equals(KEY_KEYCODE_MEDIA_START)) {
            return CommonParams.KEYCODE_MEDIA_START;
        } else if (key.equals(KEY_KEYCODE_MEDIA_STOP)) {
            return CommonParams.KEYCODE_MEDIA_STOP;
        } else if (key.equals(KEY_KEYCODE_VR_START)) {
            return CommonParams.KEYCODE_VR_START;
        } else if (key.equals(KEY_KEYCODE_VR_STOP)) {
            return CommonParams.KEYCODE_VR_STOP;
        } else if (key.equals(KEY_KEYCODE_NUMBER_0)) {
            return CommonParams.KEYCODE_NUMBER_0;
        } else if (key.equals(KEY_KEYCODE_NUMBER_1)) {
            return CommonParams.KEYCODE_NUMBER_1;
        } else if (key.equals(KEY_KEYCODE_NUMBER_2)) {
            return CommonParams.KEYCODE_NUMBER_2;
        } else if (key.equals(KEY_KEYCODE_NUMBER_3)) {
            return CommonParams.KEYCODE_NUMBER_3;
        } else if (key.equals(KEY_KEYCODE_NUMBER_4)) {
            return CommonParams.KEYCODE_NUMBER_4;
        } else if (key.equals(KEY_KEYCODE_NUMBER_5)) {
            return CommonParams.KEYCODE_NUMBER_5;
        } else if (key.equals(KEY_KEYCODE_NUMBER_6)) {
            return CommonParams.KEYCODE_NUMBER_6;
        } else if (key.equals(KEY_KEYCODE_NUMBER_7)) {
            return CommonParams.KEYCODE_NUMBER_7;
        } else if (key.equals(KEY_KEYCODE_NUMBER_8)) {
            return CommonParams.KEYCODE_NUMBER_8;
        } else if (key.equals(KEY_KEYCODE_NUMBER_9)) {
            return CommonParams.KEYCODE_NUMBER_9;
        } else if (key.equals(KEY_KEYCODE_NUMBER_STAR)) {
            return CommonParams.KEYCODE_NUMBER_STAR;
        } else if (key.equals(KEY_KEYCODE_NUMBER_POUND)) {
            return CommonParams.KEYCODE_NUMBER_POUND;
        } else if (key.equals(KEY_KEYCODE_NUMBER_ADD)) {
            return CommonParams.KEYCODE_NUMBER_ADD;
        } else if (key.equals(KEY_KEYCODE_NUMBER_DEL)) {
            return CommonParams.KEYCODE_NUMBER_DEL;
        } else if (key.equals(KEY_KEYCODE_NUMBER_CLEAR)) {
            return CommonParams.KEYCODE_NUMBER_CLEAR;
        }

        LogUtil.e(TAG, "get hard key code error");
        return -1;
    }

    public boolean dispatchHardKeyEvent(int keyCode) {
        if (propertyMap == null) {
            LogUtil.e(TAG, "propertyMap is null");
            return false;
        }

        try {
            Iterator<Map.Entry<String, String>> iter = propertyMap.entrySet().iterator();
            Entry<String, String> entry = null;
            String key = null;
            String value = null;
            int valueInt = -1;
            int keyCodeCarlife = -1;
            while (iter.hasNext()) {
                entry = iter.next();
                key = entry.getKey();

                if (!key.startsWith(KEY_PREFIX_HARD_KEY)) {
                    continue;
                }

                value = entry.getValue();
                valueInt = Integer.valueOf(value);
                if (valueInt == keyCode) {
                    keyCodeCarlife = getHardKeyCode(key);
                    if (keyCodeCarlife > 0) {
                        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
                        command.setServiceType(CommonParams.MSG_TOUCH_CAR_HARD_KEY_CODE);
                        TouchListenerManager.getInstance().sendHardKeyCodeEvent(keyCodeCarlife);
                        return true;
                    }
                }
            }

        } catch (Exception ex) {
            LogUtil.e(TAG, "dispatch hard key event fail");
            ex.printStackTrace();
            return false;
        }

        LogUtil.e(TAG, "can not support this keycode: " + keyCode);
        return false;
    }

    public int getIntProperty(String key) {
        if (key.equals(KEY_INT_AUDIO_TRACK_NUM)) {
            return VALUE_INT_AUDIO_TRACK_NUM;
        } else if (key.equals(KEY_INT_AUDIO_TRACK_STREAM_TYPE)) {
            return VALUE_INT_AUDIO_TRACK_STREAM_TYPE;
        } else if (key.equals(KEY_INT_AUDIO_TRACK_TYPE)) {
            return VALUE_INT_AUDIO_TRACK_TYPE;
        } else if (key.equals(KEY_INT_VOICE_MIC)) {
            return VALUE_INT_VOICE_MIC;
        } else if (key.equals(KEY_INT_MEDIA_SAMPLE_RATE)) {
            return VALUE_INT_MEDIA_SAMPLE_RATE;
        } else if (key.equals(KEY_INT_CONNECT_TYPE_ANDROID)) {
            return VALUE_INT_CONNECT_TYPE_ANDROID;
        } else if (key.equals(KEY_INT_CONNECT_TYPE_IPHONE)) {
            return VALUE_INT_CONNECT_TYPE_IPHONE;
        } else if (key.equals(KEY_INT_AUDIO_TRANSMISSION_MODE)) {
            return VALUE_INT_AUDIO_TRANSMISSION_MODE;
        } else if (key.equals(KEY_INT_IPHONE_USB_CONNECT_TYPE)) {
            return VALUE_INT_IPHONE_USB_CONNECT_TYPE;
        } else if (key.equals(KEY_INT_GPS_FORMAT)) {
            return VALUE_INT_GPS_FORMAT;
        } else if (key.equals(KEY_ENGINE_TYPE)) {
            return VALUE_ENGINE_TYPE;
        } else if (key.equals(KEY_BOOL_INPUT_DISABLE)) {
            return VALUE_IS_INPUT_DISABLE;
        }

        LogUtil.e(TAG, "can not find key: " + key);
        return Integer.MIN_VALUE;
    }

    public boolean getBooleanProperty(String key) {
        if (key.equals(KEY_BOOL_VOICE_WAKEUP)) {
            return VALUE_BOOL_VOICE_WAKEUP;
        } else if (key.equals(KEY_BOOL_NEED_MORE_DECODE_TIME)) {
            return VALUE_BOOL_NEED_MORE_DECODE_TIME;
        } else if (key.equals(KEY_BOOL_BLUETOOTH_INTERNAL_UI)) {
            return VALUE_BOOL_BLUETOOTH_INTERNAL_UI;
        } else if (key.equals(KEY_BOOL_BLUETOOTH_AUTO_PAIR)) {
            return VALUE_BOOL_BLUETOOTH_AUTO_PAIR;
        } else if (key.equals(KEY_BOOL_TRANSPARENT_SEND_TOUCH_EVENT)) {
            return VALUE_BOOL_TRANSPARENT_SEND_TOUCH_EVENT;
        } else if (key.equals(KEY_BOOL_SEND_ACTION_DOWN)) {
            return VALUE_BOOL_SEND_ACTION_DOWN;
        } else if (key.equals(KEY_BOOL_VEHICLE_GPS)) {
            return VALUE_BOOL_VEHICLE_GPS;
        } else if (key.equals(KEY_BOOL_FOCUS_UI)) {
            return VALUE_BOOL_FOCUS_UI;
        } else if (key.equals(KEY_BOOL_TTS_REQUEST_AUDIO_FOCUS)) {
            return VALUE_BOOL_AUDIO_FOCUS_REQUIRED;
        } else if (key.equals(KEY_CONTENT_ENCRYPTION)) {
            return VALUE_CONTENT_ENCRYPTION;
        }

        LogUtil.e(TAG, "can not find key: " + key);
        return false;
    }

    public String getStringProperty(String key) {
        LogUtil.e(TAG, "can not find key: " + key);
        if (key.equals(KEY_STRING_IPHONE_NCM_ETHERNET_NAME)) {
            return VALUE_STRING_IPHONE_NCM_ETHERNET_NAME;
        }
        return null;
    }

    public boolean getReadConfStatus() {
        return isReadConfSuccess;
    }

    public boolean isReadMaxTime() {
        return readConfCnt >= MAX_TIME_READ_CONF;
    }
}
