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
package com.baidu.carlifevehicle.touch;

import com.baidu.carlife.protobuf.CarlifeCarHardKeyCodeProto.CarlifeCarHardKeyCode;
import com.baidu.carlife.protobuf.CarlifeTouchActionProto.CarlifeTouchAction;
import com.baidu.carlife.protobuf.CarlifeTouchSinglePointProto.CarlifeTouchSinglePoint;
import com.baidu.carlifevehicle.CarlifeActivity;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TouchListenerManager implements OnTouchListener {

    private static final String TAG = "TouchListenerManager";
    private static final int MAX_PIXEL = 10000;

    private static TouchListenerManager mInstance = null;

    private static boolean isNewTouchMethod = false;

    private int mContainerWidth = 0;
    private int mContainerHeight = 0;
    private int mPhoneContainerWidth = 0;
    private int mPhoneContainerHeight = 0;

    private GestureDetector mGestureDetector = null;
    private Context mContext = null;
    private CarlifeOnTouchListener mOldListener = null;
    private CarlifeOnTouchListener mNewListener = null;

    private TouchListenerManager() {

    }

    public static TouchListenerManager getInstance() {
        if (null == mInstance) {
            synchronized (TouchListenerManager.class) {
                if (null == mInstance) {
                    mInstance = new TouchListenerManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mGestureDetector = new GestureDetector(mContext, new GestureListenerImp());
        mOldListener = new CarlifeOnTouchListener() {
            @Override
            public void onTouchEvent(MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
            }
        };

        mNewListener = new CarlifeOnTouchListener() {
            @Override
            public void onTouchEvent(MotionEvent event) {
                sendAction(event.getX(), event.getY(), event.getAction());
            }
        };
    }

    /**
     * init touch detect mode
     * @param isNew true for new mode, false for old
     */
    public void initTouchMethod(boolean isNew) {
        LogUtil.e(TAG, "initTouchMethod: " + isNew);
        isNewTouchMethod = isNew;

        uninitTouchMethod();

        if (!isNewTouchMethod) {
            registerOldListener();
        } else {
            registerNewListener();
        }
    }

    public void uninitTouchMethod() {
        LogUtil.e(TAG, "uninitTouchMethod");
        unregisterOldListener();
        unregisterNewListener();
    }

    public void registerOldListener() {
        LogUtil.e(TAG, "register old touch listener");
        if (mContext == null || mGestureDetector == null || mOldListener == null) {
            LogUtil.e(TAG, "register old touch listener failed");
            return;
        }
        ((CarlifeActivity) mContext).registerListener(mOldListener);
    }

    public void unregisterOldListener() {
        LogUtil.e(TAG, "unregister old touch listener");
        if (mContext == null || mGestureDetector == null || mOldListener == null) {
            LogUtil.e(TAG, "unregister old touch listener failed");
            return;
        }
        ((CarlifeActivity) mContext).unregisterListener(mOldListener);
    }

    public void registerNewListener() {
        LogUtil.e(TAG, "register new touch listener");
        if (mContext == null || mNewListener == null) {
            LogUtil.e(TAG, "register new touch listener failed");
            return;
        }
        ((CarlifeActivity) mContext).registerListener(mNewListener);
    }

    public void unregisterNewListener() {
        LogUtil.e(TAG, "unregister new touch listener");
        if (mContext == null || mNewListener == null) {
            LogUtil.e(TAG, "unregister new touch listener failed");
            return;
        }
        ((CarlifeActivity) mContext).unregisterListener(mNewListener);
    }

    public int getContainerWidth() {
        return mContainerWidth;
    }

    public int getContainerHeight() {
        return mContainerHeight;
    }

    public int getPhoneContainerWidth() {
        return mPhoneContainerWidth;
    }

    public int getPhoneContainerHeight() {
        return mPhoneContainerHeight;
    }

    public void setContainerWidth(int w) {
        mContainerWidth = w;
    }

    public void setContainerHeight(int h) {
        mContainerHeight = h;
    }

    public void setPhoneContainerWidth(int w) {
        mPhoneContainerWidth = w;
    }

    public void setPhoneContainerHeight(int h) {
        mPhoneContainerHeight = h;
    }

    public void setGestureDetector(GestureDetector gd) {
        mGestureDetector = gd;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        } else {
            LogUtil.e(TAG, "mGestureDetector is null");
        }
        return true;
    }

    private boolean isParamValid() {
        if (mContainerWidth <= 0 || mContainerWidth > MAX_PIXEL) {
            return false;
        }
        if (mContainerHeight <= 0 || mContainerHeight > MAX_PIXEL) {
            return false;
        }
        if (mPhoneContainerWidth <= 0 || mPhoneContainerWidth > MAX_PIXEL) {
            return false;
        }
        if (mPhoneContainerHeight <= 0 || mPhoneContainerHeight > MAX_PIXEL) {
            return false;
        }

        return true;
    }

    public void sendAction(float x, float y, int action) {
        try {
            String mInfo = null;

            int tx = (int) (x * mPhoneContainerWidth / mContainerWidth);
            int ty = (int) (y * mPhoneContainerHeight / mContainerHeight);

            mInfo = "x = " + tx + " | y = " + ty + " | action = " + action;
            LogUtil.d(TAG, "sendActionEvent: " + mInfo);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_ACTION);
            CarlifeTouchAction.Builder builder = CarlifeTouchAction.newBuilder();
            builder.setX(tx);
            builder.setY(ty);
            builder.setAction(action);
            CarlifeTouchAction actionInfo = builder.build();
            command.setData(actionInfo.toByteArray());
            command.setLength(actionInfo.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionDownEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendActionDownEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_ACTION_DOWN);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionBeginEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendActionBeginEvent: " + mInfo);
            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_ACTION_BEGIN);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());
            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionUpEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendActionUpEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_ACTION_UP);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendActionMoveEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendActionMoveEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_ACTION_MOVE);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendSingleClickEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendSingleClickEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_SINGLE_CLICK);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendDoubleClickEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendDoubleClickEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_DOUBLE_CLICK);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendLongPressEvent(float x0, float y0) {
        try {
            String mInfo = "x = " + x0 + ", y = " + y0 + " | w0 = " + mContainerWidth + ", h0 = " + mContainerHeight
                    + " | w1 = " + mPhoneContainerWidth + ", h1 = " + mPhoneContainerHeight;
            LogUtil.d(TAG, "sendLongPressEvent: " + mInfo);

            int x1 = (int) (x0 * mPhoneContainerWidth / mContainerWidth);
            int y1 = (int) (y0 * mPhoneContainerHeight / mContainerHeight);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_LONG_PRESS);
            CarlifeTouchSinglePoint.Builder builder = CarlifeTouchSinglePoint.newBuilder();
            builder.setX(x1);
            builder.setY(y1);
            CarlifeTouchSinglePoint singlePoint = builder.build();
            command.setData(singlePoint.toByteArray());
            command.setLength(singlePoint.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendHardKeyCodeEvent(int keycode) {
        try {
            String mInfo = "keycode = " + keycode;
            LogUtil.d(TAG, "sendHardKeyCodeEvent: " + mInfo);

            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_TOUCH_CAR_HARD_KEY_CODE);
            CarlifeCarHardKeyCode.Builder builder = CarlifeCarHardKeyCode.newBuilder();
            builder.setKeycode(keycode);
            CarlifeCarHardKeyCode keyCode = builder.build();
            command.setData(keyCode.toByteArray());
            command.setLength(keyCode.getSerializedSize());

            ConnectManager.getInstance().writeCarlifeTouchMessage(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
