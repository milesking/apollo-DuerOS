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

import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DirectionViewPager extends ViewPager {

    private static final String TAG = "MultiViewPager";
    private float downX = 0;
    private boolean toRight;
    private boolean toLeft;

    public DirectionViewPager(Context context) {
        super(context);
    }

    public DirectionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                float upX = ev.getX();
                if (upX - downX > 0) {
                    toRight = true;
                    toLeft = false;
                    LogUtil.i(TAG, "toRight=" + toRight + ",toLeft=" + toLeft);
                } else if (upX - downX < 0) {
                    toLeft = true;
                    toRight = false;
                    LogUtil.i(TAG, "toLeft=" + toLeft + ",toRight=" + toRight);
                } else {
                    toLeft = false;
                    toRight = false;
                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean slidingRight() {
        LogUtil.i(TAG, "toRight=" + toRight);
        return toRight;
    }

    public boolean slidingLeft() {
        LogUtil.i(TAG, "toLeft=" + toLeft);
        return toLeft;
    }

}
