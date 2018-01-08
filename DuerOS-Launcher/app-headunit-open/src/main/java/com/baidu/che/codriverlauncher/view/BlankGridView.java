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
package com.baidu.che.codriverlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * function:identify blank click in GridView
 */

public class BlankGridView extends GridView {
    private OnTouchInvalidPositionListener mTouchInvalidPosListener;

    public BlankGridView(Context context) {
        super(context);
    }

    public BlankGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnTouchInvalidPositionListener {
        boolean onTouchInvalidPosition(int motionEvent);
    }

    /**
     * interface of blank click
     */
    public void setOnTouchInvalidPositionListener(OnTouchInvalidPositionListener listener) {
        mTouchInvalidPosListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mTouchInvalidPosListener == null) {
                return super.onTouchEvent(event);
            }

            if (!isEnabled()) {
                // A disabled view that is clickable still consumes the touch
                // events, it just doesn't respond to them.
                return isClickable() || isLongClickable();
            }

            final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());
            if (motionPosition == INVALID_POSITION) {
                super.onTouchEvent(event);
                return mTouchInvalidPosListener.onTouchInvalidPosition(event.getActionMasked());
            }
        }

        return super.onTouchEvent(event);
    }
}
