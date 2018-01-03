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

import java.lang.reflect.Field;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

public class ScreenUtil {

    private static final String TAG = "ScreenUtil";

    public static final int DENSITY_DEFAULT = 160;

    private DisplayMetrics mDM;
    private float mDensity = 0;
    private int mWidthPixels = 0;
    private int mHeightPixels = 0;
    private int mWindowWidthPixels = 0;
    private int mWindowHeightPixels = 0;
    private int mStatusBarHeight = 0;
    private int mDPI = 0;

    public static final int SCREEN_SIZE_Y_LARGE = 640;

    private static ScreenUtil mInstance = null;

    private ScreenUtil() {

    }

    public static ScreenUtil getInstance() {
        if (null == mInstance) {
            synchronized (ScreenUtil.class) {
                if (null == mInstance) {
                    mInstance = new ScreenUtil();
                }
            }
        }

        return mInstance;
    }

    public void init(Activity activity) {
        if (activity == null) {
            return;
        }
        mDM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(mDM);

        mDensity = mDM.density;
        mWidthPixels = mDM.widthPixels;
        mHeightPixels = mDM.heightPixels;
        mWindowWidthPixels = getWindowWidth(activity);
        mWindowHeightPixels = getWindowHeight(activity);
        mStatusBarHeight = getStatusBarHeightInner(activity);

        try {
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion > 3) {
                // Android 1.6 or higher
                mDPI = mDM.densityDpi;
            } else {
                mDPI = DENSITY_DEFAULT;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (mDPI == 0) {
            mDPI = 160;
        }
        LogUtil.e(TAG, "mDensity = " + mDensity);
        LogUtil.e(TAG, "mWidthPixels = " + mWidthPixels + ", mHeightPixels = " + mHeightPixels);
        LogUtil.e(TAG, "mWindowWidthPixels = " + mWindowWidthPixels + ", mWindowHeightPixels = "
                + mWindowHeightPixels);
        LogUtil.e(TAG, "mStatusBarHeight = " + mStatusBarHeight);
        LogUtil.e(TAG, "mDPI = " + mDPI);
    }

    public DisplayMetrics getDisplayMetrics() {
        return mDM;
    }

    public float getDensity() {
        return mDensity;
    }

    public int getDPI() {
        return mDPI;
    }

    public int getWidthPixels() {
        return mWidthPixels;
    }

    public int getHeightPixels() {
        return mHeightPixels;
    }

    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public int getWindowWidthPixels() {
        return mWindowWidthPixels;
    }

    public int getWindowHeightPixels() {
        return mWindowHeightPixels;
    }

    public int dip2px(int dip) {
        return (int) (0.5F + mDensity * dip);
    }

    public int dip2px(float dip) {
        return (int) (0.5F + mDensity * dip);
    }

    public int px2dip(int px) {
        return (int) (0.5F + px / mDensity);
    }

    public int px2dip(float px) {
        return (int) (0.5F + px / mDensity);
    }

    public int percentHeight(float percent) {
        return (int) (percent * getHeightPixels());
    }

    public int percentWidth(float percent) {
        return (int) (percent * getWidthPixels());
    }

    private int getStatusBarHeightInner(Activity activity) {
        if (activity == null) {
            return 0;
        }
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            statusBarHeight = rect.top;
        }
        return statusBarHeight;
    }

    private int getWindowWidth(Activity activity) {
        View decor = activity.getWindow().getDecorView();
        return decor.getMeasuredWidth();
    }

    private int getWindowHeight(Activity activity) {
        View decor = activity.getWindow().getDecorView();
        return decor.getMeasuredHeight();
    }

}
