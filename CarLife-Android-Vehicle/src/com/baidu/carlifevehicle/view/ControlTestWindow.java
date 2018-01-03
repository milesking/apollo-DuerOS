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
package com.baidu.carlifevehicle.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.baidu.carlife.protobuf.CarlifeCarSpeedProto;
import com.baidu.carlife.protobuf.CarlifeModuleStatusProto.CarlifeModuleStatus;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.model.ModuleStatusModel;
import com.baidu.carlifevehicle.touch.TouchListenerManager;
import com.baidu.carlifevehicle.util.DecodeUtil;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

public class ControlTestWindow implements OnClickListener {

    public static final String TAG = "ControlTestWindow";
    private static ControlTestWindow mInstance = null;

    private ViewGroup mRootView = null;
    private Context mContext = null;

    private PopupWindow mFullWindow = null;
    private View mFullWindowLayout = null;
    private EditText mVideoEt = null;
    private Button mVideoCommitBtn = null;
    private Button mVideoSaveBtn = null;
    private ImageButton mExitBtn = null;
    private Button mHomeBtn = null;
    private Button mPhoneBtn = null;
    private Button mMapBtn = null;
    private Button mMeidaBtn = null;
    private Button mSeekSubBtn = null;
    private Button mSeekAddBtn = null;
    private Button mSeekSelectorPreviousBtn = null;
    private Button mSeekSelectorNextBtn = null;
    private Button mBackBtn = null;
    private Button mOkBtn = null;
    private Button mUpBtn = null;
    private Button mDownBtn = null;
    private Button mLeftBtn = null;
    private Button mRightBtn = null;
    private OutputStream mFout = null;
    private Button mMusicStart;
    private Button mMusicStop;
    private Button mRecordStart;
    private Button mRecordStop;
    private Button mNaviStop;
    private Button mPhoneCall;
    private Button mPhoneEnd;
    private Button mCarSpeed10km;
    private Button mCarSpeed2km;

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/spspps.data";

    private ControlTestWindow() {
    }

    public static ControlTestWindow getInstance() {
        if (null == mInstance) {
            synchronized (ControlTestWindow.class) {
                if (null == mInstance) {
                    mInstance = new ControlTestWindow();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context, ViewGroup parent) {
        try {
            mContext = context;
            mRootView = parent;

            mFullWindowLayout = LayoutInflater.from(mContext).inflate(R.layout.control_test_window, null);
            mFullWindow = new PopupWindow(mFullWindowLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            mExitBtn = (ImageButton) mFullWindowLayout.findViewById(R.id.exit_img_btn);
            mExitBtn.setOnClickListener(this);

            mVideoEt = (EditText) mFullWindowLayout.findViewById(R.id.control_video_test_et);
            mVideoCommitBtn = (Button) mFullWindowLayout.findViewById(R.id.control_video_test_btn1);
            mVideoSaveBtn = (Button) mFullWindowLayout.findViewById(R.id.control_video_test_btn2);
            mVideoCommitBtn.setOnClickListener(this);
            mVideoSaveBtn.setOnClickListener(this);

            mHomeBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_home);
            mPhoneBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_phone);
            mMapBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_map);
            mMeidaBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_media);
            mHomeBtn.setOnClickListener(this);
            mPhoneBtn.setOnClickListener(this);
            mMapBtn.setOnClickListener(this);
            mMeidaBtn.setOnClickListener(this);

            mSeekSubBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_seek_sub);
            mSeekAddBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_seek_add);
            mSeekSelectorPreviousBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_selector_previous);
            mSeekSelectorNextBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_selector_next);
            mBackBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_back);
            mSeekSubBtn.setOnClickListener(this);
            mSeekAddBtn.setOnClickListener(this);
            mSeekSelectorPreviousBtn.setOnClickListener(this);
            mSeekSelectorNextBtn.setOnClickListener(this);
            mBackBtn.setOnClickListener(this);

            mOkBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_ok);
            mUpBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_up);
            mDownBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_down);
            mLeftBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_left);
            mRightBtn = (Button) mFullWindowLayout.findViewById(R.id.control_btn_right);
            mOkBtn.setOnClickListener(this);
            mUpBtn.setOnClickListener(this);
            mDownBtn.setOnClickListener(this);
            mLeftBtn.setOnClickListener(this);
            mRightBtn.setOnClickListener(this);

            mMusicStart = (Button) mFullWindowLayout.findViewById(R.id.control_music_start);
            mMusicStop = (Button) mFullWindowLayout.findViewById(R.id.control_music_stop);
            mRecordStart = (Button) mFullWindowLayout.findViewById(R.id.control_record_start);
            mRecordStop = (Button) mFullWindowLayout.findViewById(R.id.control_record_stop);
            mNaviStop = (Button) mFullWindowLayout.findViewById(R.id.control_navi_stop);
            mPhoneCall = (Button) mFullWindowLayout.findViewById(R.id.control_phone_call);
            mPhoneEnd = (Button) mFullWindowLayout.findViewById(R.id.control_phone_end);
            mMusicStart.setOnClickListener(this);
            mMusicStop.setOnClickListener(this);
            mRecordStart.setOnClickListener(this);
            mRecordStop.setOnClickListener(this);
            mNaviStop.setOnClickListener(this);
            mPhoneCall.setOnClickListener(this);
            mPhoneEnd.setOnClickListener(this);

            mFullWindowLayout.findViewById(R.id.control_btn_not_support_mic).setOnClickListener(this);
            mFullWindowLayout.findViewById(R.id.control_btn_use_mobile_mic).setOnClickListener(this);
            mFullWindowLayout.findViewById(R.id.control_btn_clear).setOnClickListener(this);
            mFullWindowLayout.findViewById(R.id.control_btn_delete).setOnClickListener(this);

            mCarSpeed10km = (Button) mFullWindowLayout.findViewById(R.id.control_car_speed_10);
            mCarSpeed10km.setOnClickListener(this);
            mCarSpeed2km = (Button) mFullWindowLayout.findViewById(R.id.control_car_speed_02);
            mCarSpeed2km.setOnClickListener(this);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void displayWindow() {
        mFullWindow.setFocusable(true);
        mFullWindow.showAtLocation(mRootView, Gravity.CENTER, 0, 0);
    }

    public void closeWindow() {
        if (mFullWindow != null && mFullWindow.isShowing()) {
            mFullWindow.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit_img_btn:
                closeWindow();
                break;
            case R.id.control_btn_home:
                showToast("KEYCODE_MAIN");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MAIN);
                break;
            case R.id.control_btn_phone:
                showToast("KEYCODE_TEL");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_TEL);
                break;
            case R.id.control_btn_map:
                showToast("KEYCODE_NAVI");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_NAVI);
                break;
            case R.id.control_btn_media:
                showToast("KEYCODE_MEDIA");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MEDIA);
                break;
            case R.id.control_btn_seek_sub:
                showToast("KEYCODE_SEEK_SUB");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_SUB);
                break;
            case R.id.control_btn_seek_add:
                showToast("KEYCODE_SEEK_ADD");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SEEK_ADD);
                break;
            case R.id.control_btn_selector_previous:
                showToast("KEYCODE_SELECTOR_PREVIOUS");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SELECTOR_PREVIOUS);
                break;
            case R.id.control_btn_selector_next:
                showToast("KEYCODE_SELECTOR_NEXT");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_SELECTOR_NEXT);
                break;
            case R.id.control_btn_back:
                showToast("KEYCODE_BACK");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_BACK);
                break;
            case R.id.control_btn_ok:
                showToast("KEYCODE_OK");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_OK);
                break;
            case R.id.control_btn_up:
                showToast("KEYCODE_MOVE_UP");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MOVE_UP);
                break;
            case R.id.control_btn_down:
                showToast("KEYCODE_MOVE_DOWN");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MOVE_DOWN);
                break;
            case R.id.control_btn_left:
                showToast("KEYCODE_MOVE_LEFT");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MOVE_LEFT);
                break;
            case R.id.control_btn_right:
                showToast("KEYCODE_MOVE_RIGHT");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MOVE_RIGHT);
                break;
            case R.id.control_video_test_btn1:
                int frameRate;
                String num = mVideoEt.getText().toString();
                if (num == null) {
                    return;
                }
                try {
                    frameRate = Integer.valueOf(num);
                    if (frameRate < 0) {
                        return;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                DecodeUtil.getInstance().sendFrameRateMsg(frameRate);
                break;
            case R.id.control_video_test_btn2:
                if (mFout == null) {
                    initFout();
                }
                byte[] buffer = DecodeUtil.getInstance().getSpsPps();
                if (buffer != null) {
                    try {
                        mFout.write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.control_music_start:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MEDIA_START);
                break;
            case R.id.control_music_stop:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_MEDIA_STOP);
                break;
            case R.id.control_record_start:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_VR_START);
                break;
            case R.id.control_record_stop:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_VR_STOP);
                break;
            case R.id.control_navi_stop:
                sendCommandToMd(ModuleStatusModel.CARLIFE_NAVI_MODULE_ID, ModuleStatusModel.NAVI_STATUS_IDLE);
                break;
            case R.id.control_phone_call:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_PHONE_CALL);
                break;
            case R.id.control_phone_end:
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_PHONE_END);
                break;
            case R.id.control_btn_not_support_mic:
                sendCommandToMd(ModuleStatusModel.CARLIFE_MIC_MODULE_ID, ModuleStatusModel.MIC_STATUS_NOT_SUPPORTED);
                break;
            case R.id.control_btn_use_mobile_mic:
                sendCommandToMd(ModuleStatusModel.CARLIFE_MIC_MODULE_ID, ModuleStatusModel.MIC_STATUS_USE_MOBILE_MIC);
                break;
            case R.id.control_btn_delete:
                showToast("KEYCODE_DEL");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_NUMBER_DEL);
                break;
            case R.id.control_btn_clear:
                showToast("KEYCODE_CLEAR");
                TouchListenerManager.getInstance().sendHardKeyCodeEvent(CommonParams.KEYCODE_NUMBER_CLEAR);
                break;
            case R.id.control_car_speed_10:
                showToast("Car Speed: 10KM");
                sendCarVelocityToMD(10);
                break;
            case R.id.control_car_speed_02:
                showToast("Car Speed: 2KM");
                sendCarVelocityToMD(2);
                break;
            default:
                break;
        }

    }

    private void initFout() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mFout = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private void sendCommandToMd(int moduleId, int statusId) {
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

    private void sendCarVelocityToMD(int carSpeed) {
        long timeStamp = System.currentTimeMillis();
        CarlifeCarSpeedProto.CarlifeCarSpeed.Builder carSpeedBuilder =
                CarlifeCarSpeedProto.CarlifeCarSpeed.newBuilder();
        carSpeedBuilder.setSpeed(carSpeed);
        carSpeedBuilder.setTimeStamp(timeStamp);

        CarlifeCarSpeedProto.CarlifeCarSpeed carlifeCarSpeed = carSpeedBuilder.build();
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_CAR_VELOCITY);
        command.setData(carlifeCarSpeed.toByteArray());
        command.setLength(carlifeCarSpeed.getSerializedSize());
        Message msgTmp = Message.obtain(null, command.getServiceType(),
                CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }
}
