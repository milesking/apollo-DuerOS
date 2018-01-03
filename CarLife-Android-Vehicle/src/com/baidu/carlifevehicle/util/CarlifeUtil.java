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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import com.baidu.android.common.util.CommonParam;
import com.baidu.carlife.protobuf.CarlifeModuleStatusProto.CarlifeModuleStatus;
import com.baidu.carlife.protobuf.CarlifeStatisticsInfoProto.CarlifeStatisticsInfo;
import com.baidu.carlife.protobuf.CarlifeVideoEncoderInfoProto.CarlifeVideoEncoderInfo;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.CrashHandler;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.fragment.BaseFragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CarlifeUtil {

    private static final String TAG = "CarlifeUtil";
    private static final String CARLIFE_DEFAULT_CUID = "UNKOWN_CUID";

    private static final int STORAGE_INTERFACE_CONUT = 1;
    private static final int STORAGE_INTERFACE_ID = 0;
    private static final int STORAGE_INTERFACE_CLASS = 8;
    private static final int STORAGE_INTERFACE_SUBCLASS = 6;
    private static final int STORAGE_INTERFACE_PROTOCOL = 80;

    private static CarlifeUtil mInstance = null;
    private Context mContext = null;

    private String mCuid = null;
    private String mVersionName = null;
    private int mVersionCode = -1;
    private String mChannel = null;

    public static CarlifeUtil getInstance() {
        if (null == mInstance) {
            synchronized (CarlifeUtil.class) {
                if (null == mInstance) {
                    mInstance = new CarlifeUtil();
                }
            }
        }
        return mInstance;
    }

    public CarlifeUtil() {
    }

    public void init(Context context) {
        if (context == null)
            return;
        mContext = context;

        mCuid = CommonParam.getCUID(mContext);
        LogUtil.e(TAG, "CUID = " + mCuid);

        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            mVersionName = pi.versionName;
            mVersionCode = pi.versionCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.d(TAG, "get version fail");
        }
        LogUtil.e(TAG, "versionName = " + mVersionName);
        LogUtil.e(TAG, "versionCode = " + mVersionCode);

        mChannel = CommonParams.VEHICLE_CHANNEL;
    }

    /**
     * get carlife version name
     * @return version name as string
     */
    public String getVersionName() {
        return mVersionName;
    }

    /**
     * get carlife version code
     * @return version code as int
     */
    public int getVersionCode() {
        return mVersionCode;
    }

    /**
     * get cuid
     * @return cuid, default is {@link CarlifeUtil#CARLIFE_DEFAULT_CUID}
     */
    public String getCuid() {
        if (mCuid == null)
            return CARLIFE_DEFAULT_CUID;
        return mCuid;
    }

    public static void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
               closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            } finally {
                closeable = null;
            }
        }
    }

    /**
     * get vehicle channel
     * @return unique channel id as string
     */
    public String getChannel() {
        return mChannel;
    }

    /**
     * get current time
     * @return time as string
     */
    public String getCurrentTime() {
        Calendar mCalendar = Calendar.getInstance();
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        return (hour + ":" + minute);
    }

    /**
     * get sdcard directory path
     * @return path as string, null if context is null
     */
    public String getSDPath() {
        String path = null;
        if (mContext == null)
            return null;
        path = mContext.getFilesDir().toString();
        return path;
    }

    /**
     * dump file from assert directory
     * @param from file name of the file in assert directory
     * @param to the directory to dump the file to
     * @return true for success, false for error
     */
    public boolean dumpAssetsFile(String from, String to) {
        if (mContext == null)
            return false;

        if (from == null || to == null) {
            LogUtil.e(TAG, "from or to is null");
            return false;
        }

        InputStream is = null;
        BufferedOutputStream bfs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            is = mContext.getResources().getAssets().open(from);

            File newfile = new File(to);
            if (newfile.isDirectory()) {
                newfile = new File(to + "/" + from);
            }
            if (!newfile.exists()) {
                newfile.createNewFile();
            }
            bfs = new BufferedOutputStream(new FileOutputStream(newfile));
            byte[] buffer = new byte[1024];
            while ((byteread = is.read(buffer)) != -1) {
                bytesum += byteread;
                bfs.write(buffer, 0, byteread);
            }
            LogUtil.d(TAG, "Dump [" + from + "] to [" + to + "] Success");
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "Dump [" + from + "] to [" + to + "] Failed");
            e.printStackTrace();
            return false;
        } finally {
            closeCloseable(is);
            closeCloseable(bfs);
        }
    }

    /**
     *
     * @return true for debug variant, false for release
     */
    public static boolean isDebug() {
        if (CommonParams.LOG_LEVEL < Log.ERROR) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * send video codec message to service
     * @param width width
     * @param height height
     * @param frameRate frame rate, if 0, means no limit to frame rate
     */
    public static void sendVideoCodecMsg(int width, int height, int frameRate) {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_VIDEO_ENCODER_INIT);
        CarlifeVideoEncoderInfo.Builder builder = CarlifeVideoEncoderInfo.newBuilder();
        builder.setWidth(width);
        builder.setHeight(height);
        builder.setFrameRate(frameRate);
        CarlifeVideoEncoderInfo videoInfo = builder.build();
        command.setData(videoInfo.toByteArray());
        command.setLength(videoInfo.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public static void sendVideoTransMsg() {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_VIDEO_ENCODER_START);
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public static void sendVideoPauseMsg() {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_VIDEO_ENCODER_PAUSE);
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public static void sendLaunchMode(int mLaunchMode) {
        if (mLaunchMode >= CommonParams.MSG_CMD_LAUNCH_MODE_NORMAL
                && mLaunchMode <= CommonParams.MSG_CMD_LAUNCH_MODE_MUSIC) {
            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(mLaunchMode);
            Message msgTmp =
                    Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
            ConnectClient.getInstance().sendMsgToService(msgTmp);
        }
    }

    public static Looper getLooper(HandlerThread handlerThread) {
        Looper looper = handlerThread.getLooper();
        while (looper == null) {
            handlerThread.start();
            looper = handlerThread.getLooper();
            if (looper != null) {
                break;
            }
        }
        return looper;
    }

    public static void sendGotoCarlife() {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_GO_TO_FOREGROUND);
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public static void sendStatisticsInfo(int connect_time) {
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_STATISTIC_INFO);
        CarlifeStatisticsInfo.Builder builder = CarlifeStatisticsInfo.newBuilder();
        builder.setCuid(CarlifeUtil.getInstance().getCuid());
        builder.setVersionName(CarlifeUtil.getInstance().getVersionName());
        builder.setVersionCode(CarlifeUtil.getInstance().getVersionCode());
        builder.setChannel(CarlifeUtil.getInstance().getChannel());
        builder.setConnectCount(PreferenceUtil.getInstance().getInt(CommonParams.CARLIFE_CONNECT_COUNT, 1));
        builder.setConnectSuccessCount(1);
        builder.setConnectTime(connect_time);
        String crashLog = CrashHandler.getInstance().readCrashInfoFromDir();
        if (crashLog != null) {
            LogUtil.d(TAG, crashLog);
            builder.setCrashLog(crashLog);
        }
        CarlifeStatisticsInfo statisInfo = builder.build();
        command.setData(statisInfo.toByteArray());
        command.setLength(statisInfo.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
        PreferenceUtil.getInstance().putInt(CommonParams.CARLIFE_CONNECT_COUNT, 0);
    }

    public static void sendModuleControlToMd(int moduleId, int statusId) {
        CarlifeModuleStatus.Builder moduleStatusBuilder = CarlifeModuleStatus.newBuilder();
        moduleStatusBuilder.setModuleID(moduleId);
        moduleStatusBuilder.setStatusID(statusId);
        CarlifeModuleStatus carlifeModuleStatus = moduleStatusBuilder.build();
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_MODULE_CONTROL);
        command.setData(carlifeModuleStatus.toByteArray());
        command.setLength(carlifeModuleStatus.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public static void dumpCarlifeFile() {
        new Thread() {
            @Override
            public void run() {
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.SCREENCAP_ANDROID_16,
                        CommonParams.SD_DIR + "/" + CommonParams.SCREENCAP_ANDROID_16);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.SCREENCAP_ANDROID_17,
                        CommonParams.SD_DIR + "/" + CommonParams.SCREENCAP_ANDROID_17);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.SCREENCAP_ANDROID_18,
                        CommonParams.SD_DIR + "/" + CommonParams.SCREENCAP_ANDROID_18);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.SCREENCAP_ANDROID_19,
                        CommonParams.SD_DIR + "/" + CommonParams.SCREENCAP_ANDROID_19);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.SCREENCAP_ANDROID_19_01,
                        CommonParams.SD_DIR + "/" + CommonParams.SCREENCAP_ANDROID_19_01);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.INPUT_ANDROID,
                        CommonParams.SD_DIR + "/" + CommonParams.INPUT_ANDROID);
                CarlifeUtil.getInstance().dumpAssetsFile(CommonParams.INPUT_JAR_ANDROID,
                        CommonParams.SD_DIR + "/" + CommonParams.INPUT_JAR_ANDROID);
            }
        }.start();
    }

    public boolean isUsbStorageDevice(Context context) {
        UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (null == mUsbManager) {
            LogUtil.e(TAG, "There is no devices");
            return false;
        } else {
            LogUtil.v(TAG, "Usb Devices: " + String.valueOf(mUsbManager.toString()));
        }
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (STORAGE_INTERFACE_CONUT == device.getInterfaceCount()) {
                UsbInterface usbInter = device.getInterface(STORAGE_INTERFACE_ID);
                if ((STORAGE_INTERFACE_CLASS == usbInter.getInterfaceClass())
                        && (STORAGE_INTERFACE_SUBCLASS == usbInter.getInterfaceSubclass())
                        && (STORAGE_INTERFACE_PROTOCOL == usbInter.getInterfaceProtocol())) {
                    LogUtil.e(TAG, "This is mass storage 1");
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isUsbStorageDevice(UsbDevice device) {
        if( device == null ) {
            LogUtil.e(TAG, "this device is null");
            return false;
        }

        if (STORAGE_INTERFACE_CONUT == device.getInterfaceCount()) {
            UsbInterface usbInter = device.getInterface(STORAGE_INTERFACE_ID);
            if ((STORAGE_INTERFACE_CLASS == usbInter.getInterfaceClass())
                    && (STORAGE_INTERFACE_SUBCLASS == usbInter.getInterfaceSubclass())
                    && (STORAGE_INTERFACE_PROTOCOL == usbInter.getInterfaceProtocol())) {
                LogUtil.e(TAG, "this device is mass storage 2");
                return true;
            }
        }

        return false;
    }

    public static void showToastInUIThread(final int textId) {
        BaseFragment.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseFragment.getMainActivity(), textId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
