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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover;
import com.baidu.carlife.protobuf.CarlifeConnectExceptionProto.CarlifeConnectException;
import com.baidu.carlife.protobuf.CarlifeDeviceInfoProto.CarlifeDeviceInfo;
import com.baidu.carlife.protobuf.CarlifeModuleStatusListProto.CarlifeModuleStatusList;
import com.baidu.carlife.protobuf.CarlifeModuleStatusProto.CarlifeModuleStatus;
import com.baidu.carlife.protobuf.CarlifeProtocolVersionMatchStatusProto.CarlifeProtocolVersionMatchStatus;
import com.baidu.carlife.protobuf.CarlifeVehicleInfoProto;
import com.baidu.carlife.protobuf.CarlifeVideoEncoderInfoProto.CarlifeVideoEncoderInfo;
import com.baidu.carlifevehicle.audioplayer.VehicleFactoryAdapter;
import com.baidu.carlifevehicle.audioplayer.VehiclePCMPlayer;
import com.baidu.carlifevehicle.bluetooth.BtDeviceManager;
import com.baidu.carlifevehicle.bluetooth.BtHfpManager;
import com.baidu.carlifevehicle.broadcast.BroadcastManager;
import com.baidu.carlifevehicle.broadcast.HardKeyReceiverChangAn;
import com.baidu.carlifevehicle.connect.AOAConnectManager;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectHeartBeat;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.connect.ConnectNcmDriverClient;
import com.baidu.carlifevehicle.encryption.DebugLogUtil;
import com.baidu.carlifevehicle.encryption.EncryptConfig;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.fragment.BaseFragment;
import com.baidu.carlifevehicle.fragment.CarLifeFragmentManager;
import com.baidu.carlifevehicle.fragment.ExceptionFragment;
import com.baidu.carlifevehicle.fragment.IncallFragment;
import com.baidu.carlifevehicle.fragment.LaunchFragment;
import com.baidu.carlifevehicle.fragment.MainFragment;
import com.baidu.carlifevehicle.fragment.NewUserGuideFragment;
import com.baidu.carlifevehicle.fragment.TouchFragment;
import com.baidu.carlifevehicle.logic.CarlifeDeviceInfoManager;
import com.baidu.carlifevehicle.logic.CarlifeProtocolVersionInfoManager;
import com.baidu.carlifevehicle.logic.FeatureConfigManager;
import com.baidu.carlifevehicle.logic.ModuleStatusManage;
import com.baidu.carlifevehicle.logic.voice.VoiceManager;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.model.ModuleStatusModel;
import com.baidu.carlifevehicle.touch.CarlifeOnTouchListener;
import com.baidu.carlifevehicle.touch.TouchListenerManager;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.DecodeUtil;
import com.baidu.carlifevehicle.util.DigitalTrans;
import com.baidu.carlifevehicle.util.ErrorCodeReport;
import com.baidu.carlifevehicle.util.ErrorCodeType;
import com.baidu.carlifevehicle.util.LogUtil;
import com.baidu.carlifevehicle.util.PreferenceUtil;
import com.baidu.carlifevehicle.util.PushUtil;
import com.baidu.carlifevehicle.util.ScreenUtil;
import com.baidu.carlifevehicle.vehicledata.CarDataManager;
import com.baidu.carlifevehicle.view.CarlifeDialog.OnCarlifeClickListener;
import com.baidu.carlifevehicle.view.CarlifeMessageDialog;
import com.baidu.carlifevehicle.view.ControlTestWindow;
import com.baidu.carlifevehicle.view.SettingAboutWindow;
import com.google.protobuf.InvalidProtocolBufferException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

public class CarlifeActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "CarlifeActivity";

    private static final String HK_INTENT_CHANGAN = "android.intent.action.C3_HARDKEY";
    private HardKeyReceiverChangAn mHardKeyReceiverChangAn;

    private MsgBaseHandler mMainActivityHandler = null;
    private ViewGroup mRootView = null;

    private TouchFragment touchFragment = null;
    private SurfaceView mSurfaceView = null;
    private Surface mSurface;
    private boolean mIsClientScreenOff = false;
    private boolean mIsClientBackground = false;
    private boolean mIsCallCoverShowed = false;

    private boolean mIsForeground = true;
    private boolean mIsConnectException = false;
    private int mLaunchMode = -1;

    private int numOfAutoConnect = 0;

    private int numClickCnt = 0;
    private int numClickSetting = 0;
    private int callStatus = 0;
    private String phoneNum = "";
    private String phoneName = "";

    private ArrayList<CarlifeOnTouchListener> mTouchListeners = new ArrayList<CarlifeOnTouchListener>();

    private CarlifeMessageDialog mExitAppDialog = null;

    /**
     * test use start
     */
    
    public static long mTimeCarlifeStart = 0;
    public static long mTimeCarlifeInit = 0;
    public static long mTimeConnectStart = 0;
    public static long mTimeConnectFinish = 0;

    private CarLifeFragmentManager mCarLifeFragmentManager;

    /**
     * test use end
     */
    private boolean mIsCalling = false;
    private boolean mIsCallComing = false;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private GpsStatus.Listener mStatusListener;

    private boolean mHasEverConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EncryptSetupManager.getInstance();

        LogUtil.d(TAG, "onCreate");
        mTimeCarlifeStart = SystemClock.elapsedRealtime();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mRootView = (ViewGroup) findViewById(R.id.main_root_view);
        PreferenceUtil.getInstance().init(this);
        mCarLifeFragmentManager = new CarLifeFragmentManager(this);
        // initialize basefragment, must be called before using it's subclass
        BaseFragment.initBeforeAll(this);
        mCarLifeFragmentManager.showFragment(LaunchFragment.getInstance());
        mSurfaceView = (SurfaceView) findViewById(R.id.main_video_surface_view);
        mSurfaceView.getHolder().addCallback(this);
        mMainActivityHandler = new MsgMainActivityHandler();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        MsgHandlerCenter.registerMessageHandler(mMainActivityHandler);
        ErrorCodeReport.getInterface().init(this);
        mMainActivityHandler.sendEmptyMessageDelayed(CommonParams.MSG_CONNECT_INIT, 500);
        ControlTestWindow.getInstance().init(this, mRootView);
        SettingAboutWindow.getInstance().init(this, mRootView);

        initLaunchMode(getIntent());
        mTimeCarlifeInit = SystemClock.elapsedRealtime();
        LogUtil.d(TAG, "-QA_Test- OnCreate_InitTime = " + (mTimeCarlifeInit - mTimeCarlifeStart));

        DebugLogUtil.getInstance().init(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    public CarLifeFragmentManager getCarLifeVehicleFragmentManager() {
        return mCarLifeFragmentManager;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        // clear enter and exit animations
        overridePendingTransition(0, 0);
        VoiceManager.getInstance().onActivityResume();
        CarlifeUtil.sendVideoTransMsg();
        super.onResume();
        mIsForeground = true;
        fullScreen();
        showTouch();
    }

    private void showTouch() {
        if (!CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))
                && !CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_BYD.substring(0, 4))
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MSTAR786)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_QUANZHI)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_IMAX6)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MX_MTK8217)) {
            Settings.System.putInt(getContentResolver(), CommonParams.SETTING_SYSTEM_SHOW_TOUCH, 1);
        }
    }

    private void closeTouch() {
        if (!CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))
                && !CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_BYD.substring(0, 4))
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MSTAR786)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_QUANZHI)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_IMAX6)
                && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MX_MTK8217)) {
            Settings.System.putInt(getContentResolver(), CommonParams.SETTING_SYSTEM_SHOW_TOUCH, 0);
        }
    }

    private void fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // must store the new intent unless getIntent() will
        setIntent(intent);
        LogUtil.d(TAG, "onNewIntent");
        initLaunchMode(getIntent());
        CarlifeUtil.sendLaunchMode(mLaunchMode);
    }

    @Override
    protected void onPause() {
        // clear enter and exit animations
        overridePendingTransition(0, 0);
        super.onPause();
        VoiceManager.getInstance().onActivityPause();
        CarlifeUtil.sendVideoPauseMsg();
        mIsForeground = false;
        closeTouch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveConnectStatus(false);
        ModuleStatusManage.initModuleStatus();

        BtDeviceManager.getInstance().uninit();

        MsgHandlerCenter.unRegisterMessageHandler(mMainActivityHandler);
        ConnectManager.getInstance().uninit();
        ConnectClient.getInstance().uninit();

        DecodeUtil.getInstance().stopThread();

        VoiceManager.getInstance().uninit();
        ErrorCodeReport.getInterface().writeErrorCodeToFile();
        ConnectNcmDriverClient.getInstance().stopNcmDriverClientThread();

        LogUtil.e(TAG, "++++++++++++++++++++Baidu Carlife End++++++++++++++++++++");

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = mCarLifeFragmentManager.getCurrentFragment();
        if (fragment != null && fragment.onBackPressed()) {
            return;
        }
        openExitAppDialog();
    }

    private class MsgMainActivityHandler extends MsgBaseHandler {

        public void handleMessage(Message msg) {
            try {
                LogUtil.d(TAG, "handleMessage get msg: " + DigitalTrans.algorismToHEXString(msg.what, 8));
                switch (msg.what) {
                    case CommonParams.MSG_CONNECT_INIT:
                        if (CarlifeConfUtil.getInstance().getReadConfStatus()) {
                            init();
                        } else {
                            if (!CarlifeConfUtil.getInstance().isReadMaxTime()) {
                                LogUtil.d(TAG, "read conf again");
                                CarlifeConfUtil.getInstance().init();
                                this.sendEmptyMessageDelayed(CommonParams.MSG_CONNECT_INIT, 500);
                            } else {
                                openExitAppDialogOnReadConfFail();
                            }
                        }
                        break;
                    case CommonParams.MSG_CONNECT_STATUS_CONNECTED:
                        LogUtil.e(TAG, "---------CONNECT_STATUS_CONNECTED---------");
                        saveConnectStatus(true);
                        if (ConnectManager.CONNECTED_TYPE != ConnectManager.CONNECTED_BY_USBMUXD) {
                            CarlifeProtocolVersionInfoManager.getInstance().sendProtocolMatchStatus();
                        }
                        mTimeConnectFinish = SystemClock.elapsedRealtime();
                        LogUtil.d(TAG,
                                "-QA_Test- StartConnect_FinishConnect = " + (mTimeConnectFinish - mTimeConnectStart));
                        BtDeviceManager.getInstance().onUsbConnected();
                        if (CommonParams.VEHICLE_CHANNEL
                                .startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
                            if (mHardKeyReceiverChangAn == null) {
                                mHardKeyReceiverChangAn = new HardKeyReceiverChangAn();
                            }
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(HK_INTENT_CHANGAN);
                            registerReceiver(mHardKeyReceiverChangAn, filter);
                        }
                        CarDataManager.getInstance().init(getBaseContext());
                        BroadcastManager.getInstance().sendBoadcastToVehicle(BroadcastManager.TYPE_CONNECTED);
                        break;
                    case CommonParams.MSG_CONNECT_STATUS_CONNECTING:
                        LogUtil.e(TAG, "---------CONNECT_STATUS_CONNECTING---------");
                        mTimeConnectStart = SystemClock.elapsedRealtime();
                        LogUtil.d(TAG, "-QA_Test- InitTime_StartConnect = " + (mTimeConnectStart - mTimeCarlifeInit));
                        ConnectManager.getInstance().setIsProtocolVersionMatch(false);
                        saveConnectStatus(false);
                        mIsClientBackground = false;
                        mIsClientScreenOff = false;
                        int connectCnt = PreferenceUtil.getInstance().getInt(CommonParams.CARLIFE_CONNECT_COUNT, 0);
                        PreferenceUtil.getInstance().putInt(CommonParams.CARLIFE_CONNECT_COUNT, connectCnt + 1);
                        if (mCarLifeFragmentManager != null) {
                            mCarLifeFragmentManager.showFragment(MainFragment.getInstance());
                        }
                        mHasEverConnect = true;
                        break;
                    case CommonParams.MSG_CONNECT_STATUS_DISCONNECTED:
                        LogUtil.e(TAG, "---------CONNECT_STATUS_DISCONNECTED---------");
                        ConnectManager.getInstance().setIsProtocolVersionMatch(false);
                        mIsConnectException = false;
                        ConnectManager.getInstance().stopAllConnectSocket();
                        if (ConnectManager.CONNECTED_BY_EAN == ConnectManager.getInstance().getConnectType()) {
                            ConnectHeartBeat.getInstance().stopConnectHeartBeatTimer();
                        }
                        saveConnectStatus(false);
                        DecodeUtil.getInstance().disconnectedReset();
                        numOfAutoConnect++;
                        // restart the connect thread
                        ConnectManager.getInstance().stopConnectThread();
                        ConnectManager.getInstance().startConnectThread();
                        if (mCarLifeFragmentManager != null) {
                            mCarLifeFragmentManager.showFragment(MainFragment.getInstance());
                        }
                        ConnectManager.getInstance().stopHeartBeatTimer();
                        TouchListenerManager.getInstance().uninitTouchMethod();
                        initLaunchMode(null);
                        if (CommonParams.VEHICLE_CHANNEL
                                .startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
                            mLocationManager.removeUpdates(mLocationListener);
                            mLocationManager.removeGpsStatusListener(mStatusListener);
                            if (mHardKeyReceiverChangAn != null) {
                                unregisterReceiver(mHardKeyReceiverChangAn);
                            }
                        }
                        // stop all car data report
                        CarDataManager.getInstance().uninit();
                        BtDeviceManager.getInstance().onUsbDisconnected();
                        if (BtHfpManager.getInstance().isServiceRunning) {
                            LogUtil.v(TAG, "BtHfpManager.getInstance().isServiceRunning");
                        } else {
                            mIsCalling = false;
                            mIsCallComing = false;
                        }
                        mIsCallCoverShowed = false;
                        callStatus = 0;
                        BroadcastManager.getInstance().sendBoadcastToVehicle(BroadcastManager.TYPE_DISCONNECTED);

                        //
                        EncryptSetupManager.getInstance().onDisConnection();
                        break;
                    case CommonParams.MSG_CMD_PROTOCOL_VERSION_MATCH_STATUS:
                        if (ConnectManager.CONNECTED_BY_EAN == ConnectManager.getInstance().getConnectType()) {
                            ConnectHeartBeat.getInstance().startConnectHeartBeatTimer();
                        }
                        CarlifeCmdMessage protocolMatchM = (CarlifeCmdMessage) msg.obj;
                        CarlifeProtocolVersionMatchStatus protocolMatchStatus = null;
                        int matchStatus = CommonParams.PROTOCOL_VERSION_NOT_MATCH;
                        try {
                            protocolMatchStatus = CarlifeProtocolVersionMatchStatus.parseFrom(protocolMatchM.getData());
                            CarlifeProtocolVersionInfoManager.getInstance().setProtocolMatchStatus(protocolMatchStatus);
                            matchStatus = protocolMatchStatus.getMatchStatus();
                        } catch (InvalidProtocolBufferException e) {
                            LogUtil.e(TAG, "Get Protocol Version Match Status Error");
                            e.printStackTrace();
                            break;
                        }
                        LogUtil.e(TAG, "Protocol Version Match Version: " + matchStatus);
                        if (matchStatus == CommonParams.PROTOCOL_VERSION_MATCH) {
                            ConnectManager.getInstance().startHeartBeatTimer();
                            ConnectManager.getInstance().setIsProtocolVersionMatch(true);
                            CarlifeUtil.sendStatisticsInfo((int) (mTimeConnectFinish - mTimeConnectStart));
                            CarlifeDeviceInfoManager.getInstance().sendCarlifeDeviceInfo();
                            // notify mobile device to return the width and height of codec
                            if (!VehicleFactoryAdapter.getInstance().isContentEncrypt() || EncryptConfig
                                    .AES_ENCRYPT_AS_BEGINE) {
                                CarlifeUtil.sendVideoCodecMsg(mSurfaceView.getWidth(),
                                        mSurfaceView.getHeight(), 0);
                            }
                            DecodeUtil.getInstance().startThread();
                            VehiclePCMPlayer.getInstance().initial();
                            VehiclePCMPlayer.getInstance().threadStart();
                            TouchListenerManager.getInstance().initTouchMethod(CarlifeConfUtil.getInstance()
                                    .getBooleanProperty(CarlifeConfUtil.KEY_BOOL_TRANSPARENT_SEND_TOUCH_EVENT));

                            ConnectManager.getInstance().sendConnectTypeToMd(
                                    ModuleStatusModel.CARLIFE_CONNECT_MODULE_ID, ConnectManager.CONNECTED_TYPE);

                            if (CommonParams.VEHICLE_CHANNEL
                                    .equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MSTAR786_2)) {
                                CarlifeUtil.sendModuleControlToMd(ModuleStatusModel.CARLIFE_MIC_MODULE_ID,
                                        ModuleStatusModel.MIC_STATUS_USE_MOBILE_MIC);
                            }

                        } else {
                            ConnectManager.getInstance().setIsProtocolVersionMatch(false);
                            ConnectClient.getInstance().setIsConnected(false);
                            ConnectManager.getInstance().stopConnectThread();
                            showToast(getResources().getString(R.string.carlife_toast_protocol_not_match));
                        }
                        break;
                    case CommonParams.MSG_CMD_VIDEO_ENCODER_INIT_DONE:
                        CarlifeCmdMessage carlifeMsg = (CarlifeCmdMessage) msg.obj;
                        CarlifeVideoEncoderInfo videoInfo = null;
                        try {
                            videoInfo = CarlifeVideoEncoderInfo.parseFrom(carlifeMsg.getData());
                        } catch (InvalidProtocolBufferException e) {
                            LogUtil.e(TAG, "Get Video Encoder Init Info Error");
                            e.printStackTrace();
                            break;
                        }
                        int encWidth = videoInfo.getWidth();
                        int encHeight = videoInfo.getHeight();
                        DebugLogUtil.getInstance().println("video width, height: " + encWidth + " " + encHeight);

                        DecodeUtil.getInstance().initDecoder(mSurface, encWidth, encHeight);
                        TouchListenerManager.getInstance().setPhoneContainerWidth(encWidth);
                        TouchListenerManager.getInstance().setPhoneContainerHeight(encHeight);
                        LogUtil.e(TAG, "Phone Screencap Size: width = " + encWidth + ", height = " + encHeight);
                        TouchListenerManager.getInstance().setContainerWidth(mSurfaceView.getWidth());
                        TouchListenerManager.getInstance().setContainerHeight(mSurfaceView.getHeight());
                        LogUtil.e(TAG, "Car Surface View Size: width = " + mSurfaceView.getWidth() + ", height = "
                                + mSurfaceView.getHeight());
                        CarlifeUtil.sendVideoTransMsg();
                        CarlifeUtil.sendLaunchMode(mLaunchMode);
                        if (mLaunchMode == CommonParams.MSG_CMD_LAUNCH_MODE_MUSIC) {
                            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CMD_GET_AUDIO_FOCUS);
                        }
                        ErrorCodeReport.getInterface().writeErrorCode(ErrorCodeType.CARLIFE_CONNECT_CONNECTED);
                        ErrorCodeReport.getInterface().sendErrorCode();
                        break;
                    case CommonParams.MSG_CMD_MD_INFO:
                        try {
                            CarlifeCmdMessage carlifeM = (CarlifeCmdMessage) msg.obj;
                            CarlifeDeviceInfo info = null;
                            info = CarlifeDeviceInfo.parseFrom(carlifeM.getData());
                            CarlifeDeviceInfoManager.getInstance().setPhoneDeviceInfo(info);
                            PushUtil.setIOSDeviceToken(info.getToken());
                            LogUtil.d(TAG, info.toString());
                            BtDeviceManager.getInstance().onCarlifeAuthenticated();

                            if (ConnectManager.getInstance().getConnectType() == ConnectManager.CONNECTED_BY_AOA
                                    && info.getSdkInt() < android.os.Build.VERSION_CODES.LOLLIPOP) {
                                ConnectManager.getInstance().stopHeartBeatTimer();

                                MsgHandlerCenter.dispatchMessageDelay(
                                            CommonParams.MSG_CONNECT_AOA_NOT_SUPPORT, 2000);
                            }
                        } catch (Exception ex) {
                            LogUtil.e(TAG, "get md info error");
                            ex.printStackTrace();
                        }
                        break;
                    case CommonParams.MSG_CMD_SCREEN_ON:
                        break;
                    case CommonParams.MSG_CMD_SCREEN_OFF:
                        break;
                    case CommonParams.MSG_CMD_SCREEN_USERPRESENT:
                        break;
                    case CommonParams.MSG_CMD_BACKGROUND:
                        mIsClientBackground = true;
                        if (!mIsConnectException) {
                            this.removeMessages(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT);
                        }
                        break;
                    case CommonParams.MSG_CMD_FOREGROUND:
                        mIsClientBackground = false;
                        if (!mIsConnectException && DecodeUtil.getInstance().isDecoderReady()) {
                            MsgHandlerCenter.dispatchMessageDelay(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT, 2000);
                        }
                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT:
                        // Recover call status cover page only when in ringing
                        if (mIsCallCoverShowed && callStatus == 1) {

                            CarlifeBTHfpCallStatusCover.Builder builder = CarlifeBTHfpCallStatusCover.newBuilder();
                            if (builder != null) {
                                LogUtil.d("Bt", "Recover callstatus cover on reception of foreground message");
                                builder.setState(callStatus);
                                if (TextUtils.isEmpty(phoneNum)) {
                                    builder.setPhoneNum("");
                                } else {
                                    builder.setPhoneNum(phoneNum);
                                }

                                if (TextUtils.isEmpty(phoneName)) {
                                    builder.setName("");
                                } else {
                                    builder.setName(phoneName);
                                }
                                CarlifeBTHfpCallStatusCover cover = builder.build();
                                CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
                                btCommand.setServiceType(CommonParams.MSG_CMD_BT_HFP_CALL_STATUS_COVER);
                                btCommand.setData(cover.toByteArray());
                                btCommand.setLength(cover.getSerializedSize());
                                MsgHandlerCenter.dispatchMessage(
                                        CommonParams.MSG_CMD_BT_HFP_CALL_STATUS_COVER, btCommand);

                            }
                        } else if (ConnectClient.getInstance().isCarlifeConnected()
                                && !mIsConnectException) {
                            if (mCarLifeFragmentManager != null) {
                                mCarLifeFragmentManager.showFragment(TouchFragment.getInstance());
                            }
                        }
                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT:
                        if (!ConnectClient.getInstance().isCarlifeConnected()
                                || !DecodeUtil.getInstance().isDecoderReady()) {
                            if (mCarLifeFragmentManager != null) {
                                mCarLifeFragmentManager.showFragment(MainFragment.getInstance());
                            }
                        } else {
                            // if connected, jump to touch fragment
                            if (!mIsClientBackground) {
                                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                            } else {
                                MsgHandlerCenter.dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT);
                            }
                        }
                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT:
                        if (mCarLifeFragmentManager == null) {
                            return;
                        }
                        mCarLifeFragmentManager.showFragment(ExceptionFragment.getInstance());
                        LogUtil.i(TAG, "mIsCalling=" + mIsCalling);
                        if (BtHfpManager.getInstance().isServiceRunning) {

                            ExceptionFragment.getInstance()
                                    .changeTipsCallback(getString(R.string.connect_screenoff_hint));
                            ExceptionFragment.getInstance().setStartAppBtnVisible();
                            ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_click);
                        } else {
                            if (mIsCalling) {
                                if (mIsCallComing) {
                                    ExceptionFragment.getInstance().changeTipsCallback(
                                            getString(R.string.line_is_coming));
                                    ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_incoming);
                                    ExceptionFragment.getInstance().setStartAppBtnHide();
                                } else {
                                    ExceptionFragment.getInstance().changeTipsCallback(
                                            getString(R.string.line_is_busy));
                                    ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_calling);
                                    ExceptionFragment.getInstance().setStartAppBtnHide();
                                }

                            } else {
                                ExceptionFragment.getInstance()
                                        .changeTipsCallback(getString(R.string.connect_screenoff_hint));
                                ExceptionFragment.getInstance().setStartAppBtnVisible();
                                ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_click);
                            }
                        }

                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_SETTING_FRAGMENT:
                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_DIAGNOSE_FRAGMENT:
                        break;
                    case CommonParams.MSG_CMD_GO_TO_DESKTOP:
                        goToDesktop();
                        break;
                    case CommonParams.MSG_CMD_BT_HFP_CALL_STATUS_COVER:
                        CarlifeCmdMessage hfpCarlifeCmdMessage = (CarlifeCmdMessage) msg.obj;
                        CarlifeBTHfpCallStatusCover callStatusCover = null;
                        try {
                            callStatusCover = CarlifeBTHfpCallStatusCover.parseFrom(hfpCarlifeCmdMessage.getData());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            break;
                        }
                        if (callStatusCover == null) {
                            return;
                        }

                        callStatus = callStatusCover.getState();
                        phoneNum = callStatusCover.getPhoneNum();
                        phoneName = callStatusCover.getName();

                        LogUtil.d("Bt", "call status = " + callStatus + ",call number = " + phoneNum + ", call name = "
                                + phoneName);
                        if (BtHfpManager.getInstance().isServiceRunning) {
                            if (mCarLifeFragmentManager == null) {
                                return;
                            }
                            mCarLifeFragmentManager.showFragment(IncallFragment.getInstance());
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    String temp = phoneName;
                                    if (TextUtils.isEmpty(temp)) {
                                        temp = getString(R.string.phone_name_unknown);
                                    }
                                    switch (callStatus) {
                                        case 1:
                                            IncallFragment.getInstance().showIncomingLayout(temp);
                                            mIsCallCoverShowed = true;
                                            break;
                                        case 2:
                                            IncallFragment.getInstance().showOutgoingLayout(temp);
                                            mIsCallCoverShowed = true;
                                            break;
                                        case 3:
                                            mIsCallCoverShowed = false;
                                            removeMessages(CommonParams.MSG_CMD_BT_HFP_CALL_STATUS_COVER);
                                            removeMessages(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                                            if (!mIsConnectException && DecodeUtil.getInstance().isDecoderReady()) {

                                                MsgHandlerCenter
                                                        .dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                                            }

                                            break;
                                        default:
                                            break;
                                    }

                                }
                            }, 0);

                        } else {
                            if (mCarLifeFragmentManager == null) {
                                return;
                            }
                            mCarLifeFragmentManager.showFragment(ExceptionFragment.getInstance());
                            LogUtil.i(TAG, "mIsCalling=" + mIsCalling);

                            if (mIsCalling) {
                                ExceptionFragment.getInstance().changeTipsCallback(getString(R.string.line_is_busy));
                                ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_calling);
                            } else {
                                ExceptionFragment.getInstance().changeTipsCallback(
                                        getString(R.string.connect_screenoff_hint));
                                ExceptionFragment.getInstance().changeDrawableCallback(R.drawable.car_ic_click);
                                ExceptionFragment.getInstance().setStartAppBtnVisible();
                            }

                        }
                        break;
                    case CommonParams.MSG_CMD_MODULE_STATUS:
                        CarlifeCmdMessage carlifeModelueStateM = (CarlifeCmdMessage) msg.obj;
                        try {
                            CarlifeModuleStatusList carlifeModuleStatusList =
                                    CarlifeModuleStatusList.parseFrom(carlifeModelueStateM.getData());
                            if (carlifeModuleStatusList == null) {
                                return;
                            }
                            List<CarlifeModuleStatus> moduleStatusList = carlifeModuleStatusList.getModuleStatusList();
                            if (moduleStatusList != null && moduleStatusList.size() > 0) {
                                for (int i = 0; i < moduleStatusList.size(); i++) {
                                    CarlifeModuleStatus carlifeModuleStatus = moduleStatusList.get(i);
                                    int moduleId = carlifeModuleStatus.getModuleID();
                                    int statusId = carlifeModuleStatus.getStatusID();
                                    LogUtil.d(TAG, "moduleID=" + moduleId + ",statusID=" + statusId);
                                    // save module status if connected
                                    if (ConnectClient.getInstance().isCarlifeConnected()) {
                                        ModuleStatusManage.saveModuleStatus(moduleId, statusId);
                                    }
                                    switch (moduleId) {
                                        case ModuleStatusModel.CARLIFE_VR_MODULE_ID:
                                            if (statusId == ModuleStatusModel.VR_STATUS_RUNNING) {
                                                // clear last result before new record begin
                                                MsgHandlerCenter.dispatchMessage(
                                                        CommonParams.MSG_CMD_PHONE_VR_RECORD_START);
                                                VoiceManager.getInstance().onVoiceRecogRunning();
                                            } else if (statusId == ModuleStatusModel.VR_STATUS_IDLE) {
                                                VoiceManager.getInstance().onVoiceRecogIDLE();
                                            }
                                            break;
                                        case ModuleStatusModel.CARLIFE_PHONE_MODULE_ID:
                                            if (BtHfpManager.getInstance().isServiceRunning) {
                                                LogUtil.d("Bt", "Module Status : Call Status changed");

                                            } else {
                                                if (statusId == ModuleStatusModel.PHONE_STATUS_IDLE || statusId == -1) {
                                                    mIsCalling = false;
                                                    mIsCallComing = false;
                                                    if (mIsClientBackground && !mIsConnectException) {
                                                        MsgHandlerCenter.dispatchMessage(
                                                                CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT);
                                                    } else {
                                                        if (DecodeUtil.getInstance().isDecoderReady()) {
                                                            MsgHandlerCenter.dispatchMessageDelay(
                                                                    CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT, 1000);
                                                        }
                                                    }

                                                } else {
                                                    mIsCalling = true;
                                                    if (statusId == ModuleStatusModel.PHONE_STATUS_INCOMING) {
                                                        mIsCallComing = true;
                                                    } else {
                                                        mIsCallComing = false;
                                                    }
                                                    if (!mIsConnectException) {
                                                        MsgHandlerCenter.dispatchMessage(
                                                                CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT);
                                                    }
                                                }
                                            }

                                            break;
                                        case ModuleStatusModel.CARLIFE_MUSIC_MODULE_ID:
                                            if (CommonParams.VEHICLE_CHANNEL
                                                    .startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
                                                if (!mIsForeground) {
                                                    if (statusId == ModuleStatusModel.MUSIC_STATUS_RUNNING) {
                                                        LogUtil.e(TAG, "CARLIFE_BACKGROUND_MUSIC_START");
                                                        sendBroadcast(new Intent(
                                                                CommonParams.CARLIFE_BACKGROUND_MUSIC_START));
                                                    } else if (statusId == ModuleStatusModel.MUSIC_STATUS_IDLE) {
                                                        LogUtil.e(TAG, "CARLIFE_BACKGROUND_MUSIC_STOP");
                                                    }
                                                }
                                            }
                                            break;
                                        case ModuleStatusModel.CARLIFE_NAVI_MODULE_ID:
                                            if (ModuleStatusModel.NAVI_STATUS_START == statusId) {
                                                LogUtil.d(TAG, "NAVI_STATUS_START");
                                                BroadcastManager.getInstance().sendBoadcastToVehicle(
                                                        BroadcastManager.TYPE_NAVI_START);
                                            } else if (ModuleStatusModel.NAVI_STATUS_STOP == statusId) {
                                                LogUtil.d(TAG, "NAVI_STATUS_STOP");
                                                BroadcastManager.getInstance().sendBoadcastToVehicle(
                                                        BroadcastManager.TYPE_NAVI_STOP);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case CommonParams.MSG_MAIN_DISPLAY_USER_GUIDE_FRAGMENT:
                        // user guide
                        if (!mHasEverConnect) {
                            mCarLifeFragmentManager.showFragment(NewUserGuideFragment.getInstance());
                        }
                        break;
                    case CommonParams.MSG_CMD_CONNECT_EXCEPTION:
                        CarlifeCmdMessage carlifeCmdMsg = (CarlifeCmdMessage) msg.obj;
                        try {
                            CarlifeConnectException carlifeConnectException =
                                    CarlifeConnectException.parseFrom(carlifeCmdMsg.getData());
                            handleConnectException(carlifeConnectException);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CommonParams.MSG_CMD_REQUEST_GO_TO_FOREGROUND:
                        LogUtil.e(TAG, "get request go to foreground");
                        CarlifeUtil.sendGotoCarlife();
                        break;
                    case CommonParams.MSG_CMD_AUDIO_FOCUS_GAIN:
                        if (mHardKeyReceiverChangAn != null) {
                            mHardKeyReceiverChangAn.setHoldingAudioFocus(true);
                        }
                        break;
                    case CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS:
                        if (mHardKeyReceiverChangAn != null) {
                            mHardKeyReceiverChangAn.setHoldingAudioFocus(false);
                        }
                        break;
                    case CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT:
                        if (mHardKeyReceiverChangAn != null) {
                            mHardKeyReceiverChangAn.setHoldingAudioFocus(false);
                        }
                        break;
                    case CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        if (mHardKeyReceiverChangAn != null) {
                            mHardKeyReceiverChangAn.setHoldingAudioFocus(false);
                        }
                        break;
                    case CommonParams.MSG_ACTION_EXIT_APP:
                        exitApp();
                        break;
                    case CommonParams.MSG_CMD_MD_EXIT:
                        ConnectClient.getInstance().setIsConnected(false);
                        break;
                    case CommonParams.MSG_CMD_CAR_DATA_SUBSCRIBE_REQ:
                        CarDataManager.getInstance().onRequest(0, 0, 0);
                        break;

                    case CommonParams.MSG_CMD_CAR_DATA_START_REQ:
                        LogUtil.d(TAG, "On reception of MSG_CMD_CAR_DATA_START_REQ");
                        CarlifeCmdMessage carlifeCarDataStart = (CarlifeCmdMessage) msg.obj;
                        try {
                            CarlifeVehicleInfoProto.CarlifeVehicleInfo carlifeVehicleInfo = CarlifeVehicleInfoProto
                                    .CarlifeVehicleInfo.parseFrom(carlifeCarDataStart.getData());
                            if (carlifeVehicleInfo == null) {
                                return;
                            }
                            int moduleId = carlifeVehicleInfo.getModuleID();
                            int moduleFlag = carlifeVehicleInfo.getFlag();
                            int frequency = carlifeVehicleInfo.getFrequency();
                            LogUtil.d(TAG, "CarDataStart Module =" + moduleId + "CarDataStart Flag =" +
                                    moduleFlag);
                            CarDataManager.getInstance().onStart(moduleId, moduleFlag, frequency);

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case CommonParams.MSG_CMD_CAR_DATA_STOP_REQ:
                        break;
                    case CommonParams.MSG_CMD_MD_RSA_PUBLIC_KEY_REQUEST:
                        EncryptSetupManager.getInstance().sendRsaPublicKey();
                        break;

                    case CommonParams.MSG_CMD_MD_AES_KEY_SEND_REQUEST:
                        EncryptSetupManager.getInstance().getAESKey(msg);
                        // encrypt on
                        EncryptSetupManager.getInstance().setEncryptSwitch(true);

                        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
                        command.setServiceType(CommonParams.MSG_CMD_HU_AES_REC_RESPONSE);
                        Message msgTmp =
                                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0,
                                        command);
                        ConnectClient.getInstance().sendMsgToService(msgTmp);
                        break;
                    case CommonParams.MSG_CMD_MD_ENCRYPT_READY:
                        CarlifeUtil.sendVideoCodecMsg(mSurfaceView.getWidth(), mSurfaceView
                                .getHeight(), 0);
                        break;

                    default:
                        break;
                }
            } catch (Exception ex) {
                LogUtil.e(TAG, "handle message exception");
                ex.printStackTrace();
            }
        }

        @Override
        public void careAbout() {
            addMsg(CommonParams.MSG_CONNECT_INIT);
            addMsg(CommonParams.MSG_CONNECT_STATUS_CONNECTED);
            addMsg(CommonParams.MSG_CONNECT_STATUS_CONNECTING);
            addMsg(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
            addMsg(CommonParams.MSG_CMD_VIDEO_ENCODER_INIT_DONE);
            addMsg(CommonParams.MSG_CMD_HU_PROTOCOL_VERSION);
            addMsg(CommonParams.MSG_CMD_PROTOCOL_VERSION_MATCH_STATUS);
            addMsg(CommonParams.MSG_CMD_MD_INFO);
            addMsg(CommonParams.MSG_CMD_HU_INFO);

            addMsg(CommonParams.MSG_CMD_SCREEN_ON);
            addMsg(CommonParams.MSG_CMD_SCREEN_OFF);
            addMsg(CommonParams.MSG_CMD_SCREEN_USERPRESENT);
            addMsg(CommonParams.MSG_CMD_FOREGROUND);
            addMsg(CommonParams.MSG_CMD_BACKGROUND);

            addMsg(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
            addMsg(CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT);
            addMsg(CommonParams.MSG_MAIN_DISPLAY_EXCEPTION_FRAGMENT);
            addMsg(CommonParams.MSG_MAIN_DISPLAY_SETTING_FRAGMENT);
            addMsg(CommonParams.MSG_MAIN_DISPLAY_DIAGNOSE_FRAGMENT);
            addMsg(CommonParams.MSG_MAIN_DISPLAY_USER_GUIDE_FRAGMENT);

            addMsg(CommonParams.MSG_CMD_BT_HFP_CALL_STATUS_COVER);
            addMsg(CommonParams.MSG_CMD_GO_TO_DESKTOP);

            addMsg(CommonParams.MSG_CMD_HU_BT_OOB_INFO);
            addMsg(CommonParams.MSG_CMD_MODULE_STATUS);
            if (!CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_MSTAR786)
                    && !CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_ROADROVER_QUANZHI)) {
                // adapt for luchang head unit, ignore bluetooth
                addMsg(CommonParams.MSG_CMD_MD_BT_OOB_INFO);
            }
            addMsg(CommonParams.MSG_CMD_CONNECT_EXCEPTION);
            addMsg(CommonParams.MSG_CMD_REQUEST_GO_TO_FOREGROUND);

            addMsg(CommonParams.MSG_CMD_AUDIO_FOCUS_GAIN);
            addMsg(CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS);
            addMsg(CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT);
            addMsg(CommonParams.MSG_CMD_AUDIO_FOCUS_LOSS_TRANSIENT_CAN_DUCK);

            addMsg(CommonParams.MSG_CMD_CAR_DATA_SUBSCRIBE_REQ);
            addMsg(CommonParams.MSG_CMD_CAR_DATA_START_REQ);
            addMsg(CommonParams.MSG_CMD_CAR_DATA_STOP_REQ);
            addMsg(CommonParams.MSG_ACTION_EXIT_APP);
            addMsg(CommonParams.MSG_CMD_MD_EXIT);

            // encrypt
            addMsg(CommonParams.MSG_CMD_MD_RSA_PUBLIC_KEY_REQUEST);
            addMsg(CommonParams.MSG_CMD_MD_AES_KEY_SEND_REQUEST);
            addMsg(CommonParams.MSG_CMD_MD_ENCRYPT_READY);
        }
    }

    private void init() {
        LogUtil.e(TAG, "++++++++++++++++++++Baidu Carlife Begin++++++++++++++++++++");

        LogUtil.e(TAG, "BUILD_NUMBER = " + CommonParams.BUILD_NUMBER);

        CarlifeUtil.getInstance().init(this);
        BroadcastManager.getInstance().init(this);
        CommonParams.SD_DIR = CarlifeUtil.getInstance().getSDPath() + "/" + CommonParams.SD_DIR_NAME;
        File file = new File(CommonParams.SD_DIR);
        if (!file.exists()) {
            file.mkdir();
        }

        ScreenUtil.getInstance().init(this);
        CarlifeDeviceInfoManager.getInstance().init();
        CarlifeProtocolVersionInfoManager.getInstance().init();

        CrashHandler.getInstance().init(getApplicationContext());

        saveConnectStatus(false);
        ModuleStatusManage.initModuleStatus();
        FeatureConfigManager.getInstance().init(mContext);

        CarlifeUtil.dumpCarlifeFile();

        ConnectManager.getInstance().init(this);
        ConnectClient.getInstance().init(this);
        ConnectNcmDriverClient.getInstance().startNcmDriverClientThread();

        int connectTypeAndroidProperty = CarlifeConfUtil.getInstance().getIntProperty(
                CarlifeConfUtil.KEY_INT_CONNECT_TYPE_ANDROID);

        AOAConnectManager.getInstance().init(this);

        TouchListenerManager.getInstance().init(this);

        // Initiate bluetooth device manager which take charge of bluetooth connection management
        BtDeviceManager.getInstance().init(this);
        // start voice service
        VoiceManager.getInstance().init(mContext);

        LogUtil.d(TAG, "init end");
    }

    private void initLaunchMode(Intent intent) {
        LogUtil.d(TAG, "initLaunchMode");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String mode = bundle.getString("mode");
                if (mode != null) {
                    if (mode.equals("home")) {
                        LogUtil.d(TAG, "home mode");
                        mLaunchMode = CommonParams.MSG_CMD_LAUNCH_MODE_NORMAL;
                        return;
                    } else if (mode.equals("phone")) {
                        LogUtil.d(TAG, "phone mode");
                        mLaunchMode = CommonParams.MSG_CMD_LAUNCH_MODE_PHONE;
                        return;
                    } else if (mode.equals("map")) {
                        LogUtil.d(TAG, "map mode");
                        mLaunchMode = CommonParams.MSG_CMD_LAUNCH_MODE_MAP;
                        return;
                    } else if (mode.equals("music")) {
                        LogUtil.d(TAG, "music mode");
                        mLaunchMode = CommonParams.MSG_CMD_LAUNCH_MODE_MUSIC;
                        return;
                    }
                }
            }
        }

        LogUtil.d(TAG, "no launch mode");
        mLaunchMode = -1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.e(TAG, "surfaceChanged ~ ~");
        mSurface = holder.getSurface();
        DecodeUtil.getInstance().pauseThread();
        DecodeUtil.getInstance().initDecoder(mSurface);
        DecodeUtil.getInstance().resumeThread();
        LogUtil.e(TAG, "surfaceChanged ~ ~ end");

        TouchListenerManager.getInstance().setContainerWidth(width);
        TouchListenerManager.getInstance().setContainerHeight(height);
        LogUtil.e(TAG, "Car Surface View Size: width = " + width + ", height = " + height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.e(TAG, "surfaceDestroyed - - - ");
        DecodeUtil.getInstance().pauseThread();
        LogUtil.e(TAG, "surfaceDestroyed - - - end");
    }

    public void openExitAppDialog() {
        mExitAppDialog = new CarlifeMessageDialog(this).setTitleText(R.string.alert_quit)
                .setMessage(R.string.alert_quit_app_content).setFirstBtnText(R.string.alert_confirm)
                .setFirstBtnTextColorHighLight().setOnFirstBtnClickListener(new OnCarlifeClickListener() {
                    @Override
                    public void onClick() {
                        exitApp();
                    }
                }).setSecondBtnText(R.string.alert_cancel);

        mExitAppDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mExitAppDialog = null;
            }
        });

        if (!mExitAppDialog.isShowing()) {
            try {
                mExitAppDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openExitAppDialogOnReadConfFail() {
        mExitAppDialog = new CarlifeMessageDialog(this).setTitleText(R.string.alert_quit)
                .setMessage(R.string.conf_init_fail).setOnFirstBtnClickListener(new OnCarlifeClickListener() {
                    @Override
                    public void onClick() {
                        exitApp();
                    }
                }).setFirstBtnText(R.string.alert_confirm);

        mExitAppDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mExitAppDialog = null;
            }
        });

        if (!mExitAppDialog.isShowing()) {
            try {
                mExitAppDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void justForTest() {
        new Thread() {
            @Override
            public void run() {
                MsgHandlerCenter.dispatchMessageDelay(CommonParams.MSG_CONNECT_STATUS_CONNECTING, 7000);
            }
        }.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, event.toString());
        if (mCarLifeFragmentManager.getCurrentFragment() != null
                && mCarLifeFragmentManager.getCurrentFragment() instanceof TouchFragment) {
            for (CarlifeOnTouchListener listener : mTouchListeners) {
                listener.onTouchEvent(event);
            }
        }
        if (CarlifeUtil.isDebug() && CommonParams.LOG_LEVEL <= Log.DEBUG) {
            float x = event.getX();
            float y = event.getY();
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {

                if (Math.abs(ScreenUtil.getInstance().getWidthPixels() - x) <= 80.0
                        && Math.abs(ScreenUtil.getInstance().getHeightPixels() / 2 - y) <= 80.0) {
                    numClickCnt++;
                } else {
                    numClickCnt = 0;
                }
                if (numClickCnt >= 5) {
                    ControlTestWindow.getInstance().displayWindow();
                    numClickCnt = 0;
                }
            }
        }
        if (!ConnectClient.getInstance().isCarlifeConnected()) {
            LogUtil.d(TAG, "Carlife can not connect");
            float x = event.getX();
            float y = event.getY();
            float x0 = Math.abs(ScreenUtil.getInstance().getWidthPixels());
            float y0 = Math.abs(ScreenUtil.getInstance().getHeightPixels());
            LogUtil.d(TAG, "x =" + x + "y =" + y + "x0 =" + x0 + "y0 =" + y0);
            int action = event.getAction();
            LogUtil.d(TAG, "action =" + action);
            if (action == MotionEvent.ACTION_DOWN) {
                if ((x0 - x) <= 80.0 && (y0 - y) <= 80.0) {
                    numClickSetting++;
                    LogUtil.d(TAG, "numClickSetting = " + numClickCnt);
                } else {
                    numClickSetting = 0;
                }
                if (numClickSetting >= 5) {
                    SettingAboutWindow.getInstance().displayWindow();
                    numClickSetting = 0;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void registerListener(CarlifeOnTouchListener listener) {
        mTouchListeners.add(listener);
    }

    public void unregisterListener(CarlifeOnTouchListener listener) {
        mTouchListeners.remove(listener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.d(TAG, "onKeyDown keyCode = " + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtil.d(TAG, "onKeyUp keyCode = " + keyCode);
        CarlifeConfUtil.getInstance().dispatchHardKeyEvent(keyCode);
        return super.onKeyUp(keyCode, event);
    }

    public void goToDesktop() {
        if (CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
            exitApp();
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    public void saveConnectStatus(boolean status) {
        try {
            PreferenceUtil.getInstance().putBoolean(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES,
                    CommonParams.CONNECT_STATUS, status);
        } catch (Exception ex) {
            LogUtil.e(TAG, "save connect status error");
            ex.printStackTrace();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void exitApp() {
        onPause();
        onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            fullScreen();
        }
    }

    private void handleConnectException(CarlifeConnectException exception) {
        String hintResStr = null;
        switch (exception.getExceptionType()) {
            case 2:
                hintResStr = getResources().getString(R.string.carlife_video_permission_denied_hint);
                mIsConnectException = true;
                ConnectManager.getInstance().stopHeartBeatTimer();
                break;
            case 1:
                hintResStr = getResources().getString(R.string.carlife_phone_not_support_hint);
                mIsConnectException = true;
                ConnectManager.getInstance().stopHeartBeatTimer();
                break;
            case 3:
                hintResStr = getResources().getString(R.string.carlife_video_permission_hint);
                break;
            default:
                break;
        }
        if (hintResStr == null) {
            return;
        }
        if (mCarLifeFragmentManager != null) {
            mCarLifeFragmentManager.showFragment(MainFragment.getInstance());
        }
        MainFragment.getInstance().updateExceptionTips(hintResStr);
    }
}
