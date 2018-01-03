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

import com.baidu.carlifevehicle.CarLifeApplication;

import android.content.Context;
import android.util.DisplayMetrics;

public class PhoneUtil {
    public static final float MIN_DENSITY = 240;
    public static int mScreenHeight = 0;
    public static int mScreenWidth = 0;
    public int mBottomTabHeight = 150;
    private static PhoneUtil mInstance;

    public static PhoneUtil getInstance() {
        if (null == mInstance) {
            synchronized (PhoneUtil.class) {
                if (null == mInstance) {
                    mInstance = new PhoneUtil();
                }
            }
        }

        return mInstance;
    }

    public PhoneUtil() {
        super();
    }

    /**
     * get screen height and width
     */
    public void init() {
        DisplayMetrics dMetrics = CarLifeApplication.getGlobContext().getResources().getDisplayMetrics();
        if (dMetrics != null) {
            mScreenHeight = dMetrics.heightPixels;
            mScreenWidth = dMetrics.widthPixels;
        }
    }

    public int getScreenHeight() {
        return mScreenHeight;

    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public static float getDensity(Context ctx) {
        if (ctx != null) {
            float density = ctx.getResources().getDisplayMetrics().densityDpi;
            return density;
        }
        return 0;

    }

    public int getBottomTabHeight() {
        return mBottomTabHeight;
    }

    public void setBottomTabHeight(int mBottomTabHeight) {
        mBottomTabHeight = mBottomTabHeight;
    }

    public static boolean isLowDensity(Context ctx) {
        float density = getDensity(ctx);
        if (density < MIN_DENSITY) {
            return true;
        }
        return false;
    }
}
