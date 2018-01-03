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
package com.baidu.carlifevehicle;

import android.util.Log;

import com.baidu.carlifevehicle.util.LogUtil;

import java.lang.reflect.Field;

/**
 * 公共参数类
 *
 * @author ouyangnengjun
 */
public class CommonParams {

    /**
     * Carlife控制消息类型
     */
    public static final int MSG_CMD_TYPE_RESERVED = 0x00;
    public static final int MSG_CMD_TYPE_REQUEST = 0x01;
    public static final int MSG_CMD_TYPE_RESPONSE = 0x02;
    public static final int MSG_CMD_TYPE_NOTIFICATION = 0x03;

    /**
     * Carlife手机端OS类型
     */
    public static final int MSG_CMD_PHONE_TYPE_DEFAULT = 0x01;
    public static final int MSG_CMD_PHONE_TYPE_IOS = 0x02;
    public static final int MSG_CMD_PHONE_TYPE_ANDROID = 0x03;

    /**
     * Carlife车极端OS类型
     */
    public static final int MSG_CMD_CAR_TYPE_DEFAULT = 0x01;
    public static final int MSG_CMD_CAR_TYPE_LINUX = 0x02;
    public static final int MSG_CMD_CAR_TYPE_QNX = 0x03;
    public static final int MSG_CMD_CAR_TYPE_WINCE = 0x04;
    public static final int MSG_CMD_CAR_TYPE_ANDROID = 0x05;

    /**
     * Carlife控制消息默认值
     */
    public static final int MSG_CMD_DEFAULT_VALUE = 0x00;

    /**
     * Carlife控制消息Data字段的最大长度为32KB
     */
    public static final int MSG_CMD_MAX_DATA_LEN = 32 * 1024;

    /**
     * Carlife控制消息Head中各字段的长度
     */
    public static final int MSG_CMD_HEAD_SIZE_BYTE = 8;
    public static final int MSG_CMD_HEAD_SIZE_BIT = 8 * 8;
    public static final int MSG_CMD_HEAD_SIZE_LENGTH = 16;
    public static final int MSG_CMD_HEAD_SIZE_RESERVED = 16;
    public static final int MSG_CMD_HEAD_SIZE_SERVICE_TYPE = 32;

    public static final int MSG_VIDEO_HEAD_SIZE_BYTE = 12;

    /**
     * Carlife控制消息版本号
     */
    public static final int MSG_CMD_PROTOCOL_VERSION = 1001;
    public static final int PROTOCOL_VERSION_MAJOR_VERSION = 1;
    public static final int PROTOCOL_VERSION_MINOR_VERSION = 0;
    public static final int PROTOCOL_VERSION_MATCH = 1;
    public static final int PROTOCOL_VERSION_NOT_MATCH = 2;

    /**
     * 手机和车机之间相互传递的控制消息
     **/
    // 写日志到文件的消息
    public static final int MSG_CMD_HU_LOG = 0x7FFF0001;
    // 初始化时交互的信息
    public static final int MSG_CMD_HU_PROTOCOL_VERSION = 0x00018001;
    public static final int MSG_CMD_PROTOCOL_VERSION_MATCH_STATUS = 0x00010002;
    public static final int MSG_CMD_HU_INFO = 0x00018003;
    public static final int MSG_CMD_MD_INFO = 0x00010004;
    public static final int MSG_CMD_HU_BT_OOB_INFO = 0x00018005;
    public static final int MSG_CMD_MD_BT_OOB_INFO = 0x00010006;

    // 视频相关消息
    public static final int MSG_CMD_VIDEO_ENCODER_INIT = 0x00018007;
    public static final int MSG_CMD_VIDEO_ENCODER_INIT_DONE = 0x00010008;
    public static final int MSG_CMD_VIDEO_ENCODER_START = 0x00018009;
    public static final int MSG_CMD_VIDEO_ENCODER_PAUSE = 0x0001800A;
    public static final int MSG_CMD_VIDEO_ENCODER_RESET = 0x0001800B;
    public static final int MSG_CMD_VIDEO_ENCODER_FRAME_RATE_CHANGE = 0x0001800C;
    public static final int MSG_CMD_VIDEO_ENCODER_FRAME_RATE_CHANGE_DONE = 0x0001000D;

    // 音频相关消息
    public static final int MSG_CMD_PAUSE_MEDIA = 0x0001800E;

    // 车载信息
    public static final int MSG_CMD_CAR_VELOCITY = 0x0001800F;
    public static final int MSG_CMD_CAR_GPS = 0x00018010;
    public static final int MSG_CMD_CAR_GYROSCOPE = 0x00018011;
    public static final int MSG_CMD_CAR_ACCELERATION = 0x00018012;
    public static final int MSG_CMD_CAR_OIL = 0x00018013;

    // 电话状态
    public static final int MSG_CMD_TELE_STATE_CHANGE_INCOMING = 0x00010014;
    public static final int MSG_CMD_TELE_STATE_CHANGE_OUTGOING = 0x00010015;
    public static final int MSG_CMD_TELE_STATE_CHANGE_IDLE = 0x00010016;
    public static final int MSG_CMD_TELE_STATE_CHANGE_INCALLING = 0x00010017;

    // 移动设备端状态
    public static final int MSG_CMD_SCREEN_ON = 0x00010018;
    public static final int MSG_CMD_SCREEN_OFF = 0x00010019;
    public static final int MSG_CMD_SCREEN_USERPRESENT = 0x0001001A;
    public static final int MSG_CMD_FOREGROUND = 0x0001001B;
    public static final int MSG_CMD_BACKGROUND = 0x0001001C;

    // 启动模式
    public static final int MSG_CMD_LAUNCH_MODE_NORMAL = 0x0001801D;
    public static final int MSG_CMD_LAUNCH_MODE_PHONE = 0x0001801E;
    public static final int MSG_CMD_LAUNCH_MODE_MAP = 0x0001801F;
    public static final int MSG_CMD_LAUNCH_MODE_MUSIC = 0x00018020;
    public static final int MSG_CMD_GO_TO_DESKTOP = 0x00010021;

    // 语音相关
    public static final int MSG_CMD_MIC_RECORD_WAKEUP_START = 0x00010022;
    public static final int MSG_CMD_MIC_RECORD_END = 0x00010023;
    public static final int MSG_CMD_MIC_RECORD_RECOG_START = 0x00010024;

    public static final int MSG_CMD_GO_TO_FOREGROUND = 0x00018025;
    public static final int MSG_CMD_MODULE_STATUS = 0x00010026;
    public static final int MSG_CMD_STATISTIC_INFO = 0x00018027;
    public static final int MSG_CMD_MODULE_CONTROL = 0x00018028;

    // 音乐相关
    public static final int MSG_CMD_MEDIA_INFO = 0x00010035;
    public static final int MSG_CMD_MEDIA_PROGRESS_BAR = 0x00010036;

    public static final int MSG_CMD_CONNECT_EXCEPTION = 0x00010037;
    public static final int MSG_CMD_REQUEST_GO_TO_FOREGROUND = 0x00010038;

    // 点击反馈发消息到车机
    public static final int MSG_CMD_UI_ACTION_SOUND = 0x00010039;

    // 蓝牙电话相关的命令消息
    public static final int MSG_CMD_BT_HFP_REQUEST = 0x00010040;
    public static final int MSG_CMD_BT_HFP_INDICATION = 0x00018041;
    public static final int MSG_CMD_BT_HFP_CONNECTION = 0x00018042;

    // 车机对手机进行安全认证
    public static final int MSG_CMD_HU_AUTHEN_REQUEST = 0x00018048;
    public static final int MSG_CMD_MD_AUTHEN_RESPONSE = 0x00010049;
    public static final int MSG_CMD_HU_AUTHEN_RESULT = 0x0001804A;
    public static final int MSG_CMD_MD_AUTHEN_RESULT = 0x0001004B;

    public static final int MSG_CMD_START_BT_AUTOPAIR_REQUEST = 0x0001004D;
    public static final int MSG_CMD_BT_HFP_RESPONSE = 0x0001804E;
    public static final int MSG_CMD_BT_HFP_STATUS_REQUEST = 0x0001004F;
    public static final int MSG_CMD_BT_HFP_STATUS_RESPONSE = 0x00018050;
    // 功能定制相关的命令消息
    public static final int MSG_CMD_MD_FEATURE_CONFIG_REQUEST = 0x00010051;
    public static final int MSG_CMD_HU_FEATURE_CONFIG_RESPONSE = 0x00018052;

    public static final int MSG_CMD_BT_START_IDENTIFY_REQ = 0x00018053;
    public static final int MSG_CMD_BT_IDENTIFY_RESULT_IND = 0x00010054;

    // 错误码统计信息
    public static final int MSG_CMD_ERROR_CODE = 0x00018055;

    public static final int MSG_CMD_VIDEO_ENCODER_JPEG = 0x00018056;
    public static final int MSG_CMD_VIDEO_ENCODER_JPEG_ACK = 0x00010057;

    public static final int MSG_CMD_BT_HFP_CALL_STATUS_COVER = 0x00010058;

    public static final int MSG_CMD_MD_EXIT = 0x00010059;

    // 数据加密相关
    public static final int MSG_CMD_MD_RSA_PUBLIC_KEY_REQUEST = 0x0001006A;
    public static final int MSG_CMD_HU_RSA_PUBLIC_KEY_RESPONSE = 0x0001806B;
    public static final int MSG_CMD_MD_AES_KEY_SEND_REQUEST = 0x0001006C;
    public static final int MSG_CMD_HU_AES_REC_RESPONSE = 0x0001806D;
    public static final int MSG_CMD_MD_ENCRYPT_READY = 0x0001006E;

    // Video通道相关消息
    public static final int MSG_VIDEO_DATA = 0x00020001; // 在RecordUtil中直接使用值，修改时请手动同步过去
    public static final int MSG_VIDEO_HEARTBEAT = 0x00020002; // 在RecordUtil中直接使用值，修改时请手动同步过去

    // Media通道相关消息
    public static final int MSG_MEDIA_INIT = 0x00030001;
    public static final int MSG_MEDIA_STOP = 0x00030002;
    public static final int MSG_MEDIA_PAUSE = 0x00030003;
    public static final int MSG_MEDIA_RESUME_PLAY = 0x00030004;
    public static final int MSG_MEDIA_SEEK_TO = 0x00030005;
    public static final int MSG_MEDIA_DATA = 0x00030006;

    // 消息订阅机制
    public static final int MSG_CMD_CAR_DATA_SUBSCRIBE_REQ = 0x00010031;
    public static final int MSG_CMD_CAR_DATA_SUBSCRIBE_RSP = 0x00018032;
    public static final int MSG_CMD_CAR_DATA_START_REQ = 0x00010033;
    public static final int MSG_CMD_CAR_DATA_STOP_REQ = 0x00010034;

    // TTS通道相关消息
    public static final int MSG_NAVI_TTS_INIT = 0x00040001;
    public static final int MSG_NAVI_TTS_END = 0x00040002;
    public static final int MSG_NAVI_TTS_DATA = 0x00040003;

    // VR通道相关消息
    public static final int MSG_VR_DATA = 0x00058001;
    public static final int MSG_VR_AUDIO_INIT = 0x00050002;
    public static final int MSG_VR_AUDIO_DATA = 0x00050003;
    public static final int MSG_VR_AUDIO_STOP = 0x00050004;
    public static final int MSG_VR_AUDIO_INTERRUPT = 0x00050006;

    // Touch通道相关消息
    public static final int MSG_TOUCH_ACTION = 0x00068001;
    public static final int MSG_TOUCH_ACTION_DOWN = 0x00068002;
    public static final int MSG_TOUCH_ACTION_UP = 0x00068003;
    public static final int MSG_TOUCH_ACTION_MOVE = 0x00068004;
    public static final int MSG_TOUCH_SINGLE_CLICK = 0x00068005;
    public static final int MSG_TOUCH_DOUBLE_CLICK = 0x00068006;
    public static final int MSG_TOUCH_LONG_PRESS = 0x00068007;
    public static final int MSG_TOUCH_CAR_HARD_KEY_CODE = 0x00068008;
    public static final int MSG_TOUCH_UI_ACTION_SOUND = 0x00060009;
    public static final int MSG_TOUCH_ACTION_BEGIN = 0x0006800A;

    // 车厂定制需求
    // 长安车机定制需求，车机端在后台并且开始播放音乐的时候发送广播
    public static final String CARLIFE_BACKGROUND_MUSIC_START = "com.baidu.carlife.background.music.start";
    public static final String CARLIFE_BACKGROUND_MUSIC_STOP = "com.baidu.carlife.background.music.stop";

    // 硬按键消息
    public static final int KEYCODE_HOME = 0x00000001;
    public static final int KEYCODE_PHONE_CALL = 0x00000002;
    public static final int KEYCODE_PHONE_END = 0x00000003;
    public static final int KEYCODE_PHONE_END_MUTE = 0x00000004;
    public static final int KEYCODE_HFP = 0x00000005;
    public static final int KEYCODE_SELECTOR_NEXT = 0x00000006;
    public static final int KEYCODE_SELECTOR_PREVIOUS = 0x00000007;
    public static final int KEYCODE_SETTING = 0x00000008;
    public static final int KEYCODE_MEDIA = 0x00000009;
    public static final int KEYCODE_RADIO = 0x0000000A;
    public static final int KEYCODE_NAVI = 0x0000000B;
    public static final int KEYCODE_SRC = 0x0000000C;
    public static final int KEYCODE_MODE = 0x0000000D;
    public static final int KEYCODE_BACK = 0x0000000E;
    public static final int KEYCODE_SEEK_SUB = 0x0000000F;
    public static final int KEYCODE_SEEK_ADD = 0x00000010;
    public static final int KEYCODE_VOLUME_SUB = 0x00000011;
    public static final int KEYCODE_VOLUME_ADD = 0x00000012;
    public static final int KEYCODE_MUTE = 0x00000013;
    public static final int KEYCODE_OK = 0x00000014;
    public static final int KEYCODE_MOVE_LEFT = 0x00000015;
    public static final int KEYCODE_MOVE_RIGHT = 0x00000016;
    public static final int KEYCODE_MOVE_UP = 0x00000017;
    public static final int KEYCODE_MOVE_DOWN = 0x00000018;
    public static final int KEYCODE_MOVE_UP_LEFT = 0x00000019;
    public static final int KEYCODE_MOVE_UP_RIGHT = 0x0000001A;
    public static final int KEYCODE_MOVE_DOWN_LEFT = 0x0000001B;
    public static final int KEYCODE_MOVE_DOWN_RIGHT = 0x0000001C;
    public static final int KEYCODE_TEL = 0x0000001D;
    public static final int KEYCODE_MAIN = 0x0000001E;
    public static final int KEYCODE_MEDIA_START = 0x0000001F;
    public static final int KEYCODE_MEDIA_STOP = 0x00000020;
    public static final int KEYCODE_VR_START = 0x00000021;
    public static final int KEYCODE_VR_STOP = 0x00000022;
    public static final int KEYCODE_NUMBER_0 = 0x00000023;
    public static final int KEYCODE_NUMBER_1 = 0x00000024;
    public static final int KEYCODE_NUMBER_2 = 0x00000025;
    public static final int KEYCODE_NUMBER_3 = 0x00000026;
    public static final int KEYCODE_NUMBER_4 = 0x00000027;
    public static final int KEYCODE_NUMBER_5 = 0x00000028;
    public static final int KEYCODE_NUMBER_6 = 0x00000029;
    public static final int KEYCODE_NUMBER_7 = 0x0000002A;
    public static final int KEYCODE_NUMBER_8 = 0x0000002B;
    public static final int KEYCODE_NUMBER_9 = 0x0000002C;
    public static final int KEYCODE_NUMBER_STAR = 0x0000002D; // *
    public static final int KEYCODE_NUMBER_POUND = 0x0000002E; // #
    public static final int KEYCODE_NUMBER_DEL = 0x0000002F;
    public static final int KEYCODE_NUMBER_CLEAR = 0x00000030;
    public static final int KEYCODE_NUMBER_ADD = 0x00000031; // +

    /**
     * 设置模块
     */
    public static final String USB_PREFRENECE_NAME = "UsbConfig";
    public static final String USB_PARA_IS_USB = "is_usb";
    public static final String USB_PARA_WIFI_IP = "wifi_ip";
    public static final String IOS_DEVICE_TOKEN = "device_token";

    /**
     * 连接模块
     */
    public static final int MSG_REC_REGISTER_CLIENT = 901;
    public static final int MSG_REC_UNREGISTER_CLIENT = 902;

    public static final int MSG_CONNECT_INIT = 1000;
    public static final int MSG_CONNECT_STATUS_PRECONNECT = 1001;
    public static final int MSG_CONNECT_STATUS_DISCONNECTED = 1002;
    public static final int MSG_CONNECT_STATUS_CONNECTING = 1003;
    public static final int MSG_CONNECT_STATUS_CONNECTED = 1004;
    public static final int MSG_CONNECT_STATUS_RECONNECTING = 1005;
    public static final int MSG_CONNECT_STATUS_RECONNECTED = 1006;
    public static final int MSG_CONNECT_FAIL = 1007;
    public static final int MSG_CONNECT_FAIL_INSTALL = 1008;
    public static final int MSG_CONNECT_FAIL_START = 1009;
    public static final int MSG_CONNECT_FAIL_NOT_SURPPORT = 1010;
    public static final int MSG_CONNECT_FAIL_TIMEOUT = 1011;
    public static final int MSG_CONNECT_FAIL_START_BDSC = 1012;
    public static final int MSG_CONNECT_RESET_TIMEOUT_INSTALL = 1013;
    public static final int MSG_CONNECT_CHANGE_PROGRESS_NUMBER = 1014;
    public static final int MSG_CONNECT_AOA_NO_PERMISSION = 1015;
    public static final int MSG_CONNECT_AOA_CHANGE_ACCESSORY_MODE = 1016;
    public static final int MSG_CONNECT_AOA_REQUEST_MD_PERMISSION = 1017;
    public static final int MSG_CONNECT_AOA_NOT_SUPPORT = 1018;

    public static final int MSG_CONNECT_UPDATE_UI_PRECONNECT = 1021;
    public static final int MSG_CONNECT_UPDATE_UI_DISCONNECTED = 1022;
    public static final int MSG_CONNECT_UPDATE_UI_CONNECTED = 1023;
    public static final int MSG_CONNECT_UPDATE_UI_CONNECTTING = 1024;
    public static final int MSG_CONNECT_UPDATE_UI_RECONNECTTING = 1025;
    public static final int MSG_CONNECT_UPDATE_UI_REDISCONNECTED = 1026;

    public static final int MSG_USB_STATE_MSG = 1031;
    public static final int MSG_USB_STATE_MSG_ON = 1032;
    public static final int MSG_USB_STATE_MSG_OFF = 1033;
    public static final int MSG_CONNECT_SERVICE_MSG = 1034;
    public static final int MSG_CONNECT_SERVICE_MSG_START = 1035;
    public static final int MSG_CONNECT_SERVICE_MSG_STOP = 1036;

    public static final int MSG_BONJOUR_IP_RESOLVE = 1041;

    // zxj  AOA-ADB自动切换，增加的消息
    // 当前界面显示ADB打开提示时，拔出USB需要用以下消息刷新下状态
    public static final int MSG_FRAGMENT_REFRESH = 1052;

    public static final int MSG_MAIN_DISPLAY_CONNECT_SCREENOFF = 2001;
    public static final int MSG_MAIN_DISPLAY_CONNECT_USB = 2002;
    public static final int MSG_MAIN_DISPLAY_CONNECT_WIFI = 2003;
    public static final int MSG_MAIN_DISPLAY_TOUCH_FRAGMENT = 2004;
    public static final int MSG_MAIN_DISPLAY_MAIN_FRAGMENT = 2005;
    public static final int MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT = 2006;
    public static final int MSG_MAIN_DISPLAY_SETTING_FRAGMENT = 2007;
    public static final int MSG_MAIN_DISPLAY_DIAGNOSE_FRAGMENT = 2008;
    public static final int MSG_MAIN_DISPLAY_USER_GUIDE_FRAGMENT = 2009;

    public static final String CARLIFE_BROADCAST_CONNECT_DISCONNECTED = "com.baidu.carlife.disconnected";
    public static final String CARLIFE_BROADCAST_CONNECT_CONNECTING = "com.baidu.carlife.connecting";
    public static final String CARLIFE_BROADCAST_CONNECT_CONNECTED = "com.baidu.carlife.connected";
    public static final String CARLIFE_BROADCAST_CONNECT_RECONNECTING = "com.baidu.carlife.reconnecting";
    public static final String CARLIFE_BROADCAST_CONNECT_RECONNECTED = "com.baidu.carlife.reconnected";
    public static final String CARLIFE_BROADCAST_CONNECT_USB_RESET = "com.baidu.carlife.usb.reset";

    // SharedPreferences相关
    public static final String CARLIFE_NORMAL_PREFERENCES = "Carlife";
    public static final String CARLIFE_CONNECT_COUNT = "connect_count";

    public static final String CONNECT_STATUS_SHARED_PREFERENCES = "CarlifeConnectStatus";
    public static final String CONNECT_STATUS = "connect_status";
    public static final String CARLIFE_INSTALLED = "carlife_installed";
    public static final String MODULE_ID = "module_id";
    public static final String STATUS_ID = "status_id";

    /**
     * 音频模块
     */
    public static final int MSG_CMD_GET_AUDIO_FOCUS = 3001;
    public static final int MSG_CMD_AUDIO_FOCUS_GAIN = 3002;
    public static final int MSG_CMD_AUDIO_FOCUS_LOSS = 3003;
    public static final int MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT = 3004;
    public static final int MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT_CAN_DUCK = 3005;

    public static final int VR_AUDIOFOCUS_LOSS = 3006;
    public static final int VR_AUDIOFOCUS_LOSS_TRANSIENT = 3007;
    public static final int VR_AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = 3008;
    public static final int VR_AUDIOFOCUS_GAIN = 3009;
    // 收到手机端语音模块识别开始工作的消息
    public static final int MSG_CMD_PHONE_VR_RECORD_START = 3010;

    /**
     * 蓝牙模块相关:4300-4400
     */
    public static final int MSG_ACTION_PAIRING_REQUEST = 4300;
    public static final int MSG_ACTION_BOND_STATE_CHANGED = 4301;
    public static final int MSG_ACTION_FOUND = 4302;
    public static final int MSG_ACTION_STATE_CHANGED = 4303;
    public static final int MSG_ACTION_BT_INIT_START = 4304;
    public static final int MSG_ACTION_BT_INIT_END = 4305;

    /**
     * 状态栏消息id
     **/
    public static final int APP_NOTIFICATION_ID = 5000;

    // 退出Carlife
    public static final int MSG_ACTION_EXIT_APP = 6000;

    /**
     * 车厂车机硬按键
     */
    public static final int HYUNDAI_KEYCODE_SEEK_UP = 222;
    public static final int HYUNDAI_KEYCODE_SEEK_DOWN = 223;

    /**
     * 其他
     */
    public static String SD_DIR = null;
    public static final String SD_DIR_NAME = "BaiduCarlife";
    public static final String DATA_TMP_DIR = "/data/local/tmp";
    public static final String CARLIFE_APK_NAME = "CarLife.apk";
    public static final int MSG_ARG_INVALID = -1;
    public static final int MAX_DATA_DISPLAY_LENGTH = 120;

    public static final String SERVER_LOCALHOST_URL = "127.0.0.1";
    public static final int SOCKET_WIFI_PORT = 7240;
    public static final int SOCKET_LOCALHOST_PORT = 7200;
    public static final int SOCKET_VIDEO_WIFI_PORT = 8240;
    public static final int SOCKET_VIDEO_LOCALHOST_PORT = 8200;
    public static final int SOCKET_AUDIO_WIFI_PORT = 9240;
    public static final int SOCKET_AUDIO_LOCALHOST_PORT = 9200;
    public static final int SOCKET_AUDIO_TTS_WIFI_PORT = 9241;
    public static final int SOCKET_AUDIO_TTS_LOCALHOST_PORT = 9201;
    public static final int SOCKET_AUDIO_VR_WIFI_PORT = 9242;
    public static final int SOCKET_AUDIO_VR_LOCALHOST_PORT = 9202;
    public static final int SOCKET_TOUCH_WIFI_PORT = 9340;
    public static final int SOCKET_TOUCH_LOCALHOST_PORT = 9300;

    public static final int BOARDCAST_WIFI_PORT = 7999;
    public static final int BOARDCAST_NCM_PORT = 7998;

    public static final int MAX_NUMBER_AUTO_CONNECT = 3;

    public static final String SERVER_SOCKET_NAME = "Cmd";
    public static final String SERVER_SOCKET_VIDEO_NAME = "Video";
    public static final String SERVER_SOCKET_AUDIO_NAME = "Media";
    public static final String SERVER_SOCKET_AUDIO_TTS_NAME = "TTS";
    public static final String SERVER_SOCKET_AUDIO_VR_NAME = "VR";
    public static final String SERVER_SOCKET_TOUCH_NAME = "Touch";
    public static final int MSG_CHANNEL_ID = 0x0001;
    public static final int MSG_CHANNEL_ID_VIDEO = 0x0002;
    public static final int MSG_CHANNEL_ID_AUDIO = 0x0003;
    public static final int MSG_CHANNEL_ID_AUDIO_TTS = 0x0004;
    public static final int MSG_CHANNEL_ID_AUDIO_VR = 0x0005;
    public static final int MSG_CHANNEL_ID_TOUCH = 0x0006;

    public static final String SCREENCAP_ANDROID_XX = "bdscxx";
    public static final String SCREENCAP_ANDROID = "bdsc";
    public static final String SCREENCAP_STRING_XX = "xx";
    public static final String SCREENCAP_ANDROID_16 = "bdsc16";
    public static final String SCREENCAP_ANDROID_17 = "bdsc17";
    public static final String SCREENCAP_ANDROID_18 = "bdsc18";
    public static final String SCREENCAP_ANDROID_19 = "bdsc19";
    public static final String SCREENCAP_ANDROID_19_01 = "bdsc19_01";
    public static final String INPUT_ANDROID = "bdim";
    public static final String JAR_ANDROID = "jar";
    public static final String INPUT_JAR_ANDROID = "bdim.jar";
    public static final String APP_PROCESS_ANDROID = "app_process";
    public static final String APP_PROCESS_DALVIK_CACHE = "dalvik-cache";
    public static final String SCREENCAP_ANDROID_BDSY = "bdsy";

    public static final String TYPE_OF_OS = "Android";
    /**
     * Show touch positions on screen? 0 = no 1 = yes
     *
     * @author ouyangnengjun android数据库隐藏字段 Setting.System.SHOW_TOUCHES
     */
    public static final String SETTING_SYSTEM_SHOW_TOUCH = "show_touches";

    /*
     * 编译开关
     */
    // 通过设置log级别来控制log的输入
    public static int LOG_LEVEL = Log.ERROR;
    // jenkins里对应的build.number
    public static String BUILD_NUMBER = "";
    // 是否拉起车机端的CarLife:ture 拉起车机端CarLife  ; false 不拉起
    public static boolean PULL_UP_VEHICLE = true;

    // 单音轨版本
    public static final String VEHICLE_CHANNEL_NORMAL = "20011100";
    // 双音轨版本
    public static final String VEHICLE_CHANNEL_DUAL_AUDIO_TRACK = "20012100";
    // 2015年9月上市的新途胜版本
    public static final String VEHICLE_CHANNEL_HYUNDAI = "20022100";
    // 现代索纳塔LF
    public static final String VEHICLE_CHANNEL_HYUNDAI_LFSONATA = "20022101";
    // 现代名图CF
    public static final String VEHICLE_CHANNEL_HYUNDAI_CFMISTRA = "20022102";
    // 现代朗动
    public static final String VEHICLE_CHANNEL_HYUNDAI_MDAVANTE = "20022103";
    // 现代ix-35
    public static final String VEHICLE_CHANNEL_HYUNDAI_LMIX35 = "20022104";
    // 现代ix-25
    public static final String VEHICLE_CHANNEL_HYUNDAI_GCIX25 = "20022105";
    // 现代格锐（GrandSantafe）
    public static final String VEHICLE_CHANNEL_HYUNDAI_GRANDSANTEFE = "20022106";
    // 2015年9月上市的起亚K5版本
    public static final String VEHICLE_CHANNEL_KIA = "20032100";
    // 2016上市的起亚KX5
    public static final String VEHICLE_CHANNEL_KIA_KX5 = "20032101";
    // 2016年上市的起亚K3版本
    public static final String VEHICLE_CHANNEL_KIA_K3 = "20032102";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_AUDI = "20041100";
    public static final String VEHICLE_CHANNEL_AUDI_DUAL_AUDIO = "20042100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_SHANGHAIGM = "20051100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_DAIMLER = "20062100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_PANASONIC = "20071100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_VOLKSWAGEN = "20081100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_CHANGAN = "20092100";
    // 长安CS-15款
    public static final String VEHICLE_CHANNEL_CHANGAN_CS15 = "20092101";
    // 长安悦翔V7-16款
    public static final String VEHICLE_CHANNEL_CHANGAN_V716 = "20092102";
    // 长安CS35-16款
    public static final String VEHICLE_CHANNEL_CHANGAN_CS3516 = "20092103";
    // 长安逸动-16款
    public static final String VEHICLE_CHANNEL_CHANGAN_YIDONG16 = "20092104";
    // 长安睿成-16款
    public static final String VEHICLE_CHANNEL_CHANGAN_RUICHENG16 = "20092105";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_HARMAN = "20101100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_DELPHI = "20111100";
    // 华阳MTK3360
    public static final String VEHICLE_CHANNEL_HUAYANG_MTK3360 = "20122100";
    // 华阳MTK8317
    public static final String VEHICLE_CHANNEL_HUAYANG_MTK8317 = "20122101";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_ALPINE = "20131100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_PIONEER = "20141100";
    // 路畅MStar786
    public static final String VEHICLE_CHANNEL_ROADROVER_MSTAR786 = "20152100";
    // 路畅MStar786（车机没有Mic）
    public static final String VEHICLE_CHANNEL_ROADROVER_MSTAR786_2 = "20152200";
    // 路畅全志
    public static final String VEHICLE_CHANNEL_ROADROVER_QUANZHI = "20152101";
    // 路畅MTK8317
    public static final String VEHICLE_CHANNEL_ROADROVER_MTK8317 = "20152102";
    // 路畅飞思卡尔imax6
    public static final String VEHICLE_CHANNEL_ROADROVER_IMAX6 = "20152103";
    // 路畅mx plus mtk 8217
    public static final String VEHICLE_CHANNEL_ROADROVER_MX_MTK8217 = "20152104";
    // 飞歌G6（MTK8317）
    public static final String VEHICLE_CHANNEL_FEIGE_G6 = "20162100";
    // 飞歌G8（高通8228）
    public static final String VEHICLE_CHANNEL_FEIGE_G8 = "20162101";
    // 飞歌G9（高通8974）
    public static final String VEHICLE_CHANNEL_FEIGE_G9 = "20162102";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_WISTEON = "20171100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC = "20181100";
    // 2015年12月上市的XTS2016版
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC_DUAL_AUDIO = "20182100";
    // 暂时没有使用
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_BUICK = "20191100";
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_BUICK_DUAL_AUDIO = "20192100";
    // 雪弗莱
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_CHEVROLET = "20201100";
    public static final String VEHICLE_CHANNEL_SHANGHAIGM_CHEVROLET_DUAL_AUDIO = "20202100";
    public static final String VEHICLE_CHANNEL_CHEVROLET_K216 = "20222103";
    // 2015年11月上市的比亚迪宋
    public static final String VEHICLE_CHANNEL_BYD = "20211100";
    // 远峰后装AutochipsAC8217平台
    public static final String VEHICLE_CHANNEL_YUANFENG = "20222101";
    // 远峰后装CSR A7平台（一汽X80）
    public static final String VEHICLE_CHANNEL_YUANFENG_CSRA7 = "20222102";
    // 怡利电子HUD EL-322C-CL01
    public static final String VEHICLE_CHANNEL_EL_322C_CL01 = "20292100";
    // 怡利电子HUD 后装项目
    public static final String VEHICLE_CHANNEL_EL_AFTER_MARKET = "20542100";
    // 凯越后装AutochipsAC8327平台
    public static final String VEHICLE_CHANNEL_KAIYUE = "20302100";

    // 后装车机默认的渠道号
    public static final String VEHICLE_CHANNEL_AFTER_MARKET = "20000000";
    // 前装车机默认的渠道号
    public static final String VEHICLE_CHANNEL_PREINSTALL_MARKET = "20000001";

    public static String VEHICLE_CHANNEL = VEHICLE_CHANNEL_AFTER_MARKET;

    public CommonParams() {
        // TODO Auto-generated constructor stub
    }

    public static String getMsgName(int serviceType) {
        String msgName = null;
        int tmpValue;
        try {
            Class<?> cls = com.baidu.carlifevehicle.CommonParams.class;
            Field[] fields = cls.getFields();
            int len = fields.length;
            LogUtil.d("GetMsgName", "len = " + len);
            for (int i = 0; i < len; i++) {
                // if (fields[i].isAccessible()) {
                try {
                    tmpValue = fields[i].getInt(cls);
                    if (tmpValue == serviceType) {
                        msgName = fields[i].getName();
                        break;
                    }
                } catch (Exception ex) {
                    // LogUtil.d("GetMsgName", "get msg name fail: i = " + i);
                }
                // }
            }
        } catch (Exception excep) {
            LogUtil.d("GetMsgName", "get msg name fail");
        }

        return msgName;
    }

}
