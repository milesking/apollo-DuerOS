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

import java.util.Timer;
import java.util.TimerTask;

import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class GestureListenerImp extends SimpleOnGestureListener {
    private static final String TAG = "GestureListenerImp";

    private static final float TOUCH_PRECISION = (float) 10e-4;
    private static boolean flagScroll = false;

    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    public static int SEND_ACTION_UP_TIMEOUT_MS = 150;

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        LogUtil.d(TAG, "onSingleTapUp-----" + getActionName(e.getAction()));
        TouchListenerManager.getInstance().sendSingleClickEvent(e.getX(), e.getY());
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (e == null) {
            return;
        }
        LogUtil.d(TAG, "onLongPress-----" + getActionName(e.getAction()));
        TouchListenerManager.getInstance().sendActionUpEvent(e.getX(), e.getY());
    }

    @SuppressWarnings("unused")
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        LogUtil.d(TAG, "onScroll-----" + getActionName(e2.getAction()) + ",(" + e1.getX() + "," + e1.getY() + ") ,("
                + e2.getX() + "," + e2.getY() + ")" + " ,(" + distanceX + "," + distanceY + ")");
        stopTimer();
        boolean isSendActionDown = CarlifeConfUtil.getInstance()
                .getBooleanProperty(CarlifeConfUtil.KEY_BOOL_SEND_ACTION_DOWN);
        if (!flagScroll && !isSendActionDown) {
            TouchListenerManager.getInstance().sendActionDownEvent(e1.getX(), e1.getY());
            flagScroll = true;
        }
        TouchListenerManager.getInstance().sendActionMoveEvent(e2.getX(), e2.getY());
        startTimer(e2.getX(), e2.getY());
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        LogUtil.d(TAG, "onFling-----" + getActionName(e2.getAction()) + ",(" + e1.getX() + "," + e1.getY() + ") ,("
                + e2.getX() + "," + e2.getY() + ")" + " ,(" + velocityX + "," + velocityY + ")");
        stopTimer();
        TouchListenerManager.getInstance().sendActionUpEvent(e2.getX(), e2.getY());
        flagScroll = false;
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        if (e == null) {
            return;
        }
        LogUtil.d(TAG, "onShowPress-----" + getActionName(e.getAction()));
        TouchListenerManager.getInstance().sendLongPressEvent(e.getX(), e.getY());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (e == null) {
            return false;
        }
        LogUtil.d(TAG, "onDown-----" + getActionName(e.getAction()));
        boolean isSendActionDown = CarlifeConfUtil.getInstance()
                .getBooleanProperty(CarlifeConfUtil.KEY_BOOL_SEND_ACTION_DOWN);
        if (isSendActionDown) {
            TouchListenerManager.getInstance().sendActionDownEvent(e.getX(), e.getY());
        }
        TouchListenerManager.getInstance().sendActionBeginEvent(e.getX(), e.getY());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (e == null) {
            return false;
        }
        LogUtil.d(TAG, "onDoubleTap-----" + getActionName(e.getAction()) + ",(" + e.getX() + "," + e.getY() + ")");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (e == null) {
            return false;
        }
        LogUtil.d(TAG, "onDoubleTapEvent-----" + getActionName(e.getAction()) + ",(" + e.getX() + "," + e.getY() + ")");
        if (e.getAction() == MotionEvent.ACTION_UP) {
            TouchListenerManager.getInstance().sendDoubleClickEvent(e.getX(), e.getY());
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (e == null) {
            return false;
        }
        LogUtil.d(TAG, "onSingleTapConfirmed-----" + getActionName(e.getAction()));
        return false;
    }

    private String getActionName(int action) {
        String name = "";
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                name = "ACTION_DOWN";
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                name = "ACTION_MOVE";
                break;
            }
            case MotionEvent.ACTION_UP: {
                name = "ACTION_UP";
                break;
            }
            default:
                break;
        }
        return name;
    }

    public void startTimer(float x, float y) {
        try {
            LogUtil.d(TAG, "start send action_up timer");
            final float tx = x;
            final float ty = y;
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mTimer != null) {
                        LogUtil.e(TAG, "start send action_up timer 1");
                        TouchListenerManager.getInstance().sendActionUpEvent(tx, ty);
                        flagScroll = false;
                        stopTimer();
                    }
                }
            };
            mTimer.schedule(mTimerTask, SEND_ACTION_UP_TIMEOUT_MS);
        } catch (Exception ex) {
            LogUtil.d(TAG, "startTimer get exception");
            ex.printStackTrace();
        }
    }

    public void stopTimer() {
        LogUtil.d(TAG, "stop send action_up timer");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

}
