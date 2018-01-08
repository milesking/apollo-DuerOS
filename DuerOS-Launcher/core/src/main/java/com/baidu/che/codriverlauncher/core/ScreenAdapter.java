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
package com.baidu.che.codriverlauncher.core;

import java.util.ArrayList;
import java.util.List;

import com.baidu.che.codriverlauncher.util.LogUtil;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/**
 * screen adapter
 */

public class ScreenAdapter {

    private static final float BASE_DPI = 160.0f;
    private static final String BRAND_NOWADA = "NOWADA";
    private static final String BRAND_FYT = "FYT";

    enum StandardScreen {
        W_854_H_480(854, 480, 240, 1.0f),
        W_1280_H_720(1280, 720, 320, 1.0f),
        W_1280_H_480(1280, 480, 240, 1.0f),
        W_1198_H_480(1198, 480, 240, 1.0f),
        W_1198_H_400(1198, 400, 210, 1.0f),
        W_1024_H_600(1024, 600, 240, 1.0f);

        int width;
        int height;
        int density;
        float fontScale;

        boolean match(int width, int height) {
            return this.width == width && this.height == height;
        }

        StandardScreen(int width, int height, int density, float fontScale) {
            this.width = width;
            this.height = height;
            this.density = density;
            this.fontScale = fontScale;
        }
    }

    private List<StandardScreen> mScreenList;

    private ScreenAdapter() {
        mScreenList = new ArrayList<>();
        mScreenList.add(StandardScreen.W_854_H_480);
        mScreenList.add(StandardScreen.W_1280_H_720);
        mScreenList.add(StandardScreen.W_1280_H_480);
        mScreenList.add(StandardScreen.W_1198_H_480);
        mScreenList.add(StandardScreen.W_1024_H_600);
        mScreenList.add(StandardScreen.W_1198_H_400);
    }

    private static final class InstanceHolder {
        private static final ScreenAdapter INSTANCE = new ScreenAdapter();
    }

    public static ScreenAdapter getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * this is a rearview mirror screen or not
     */
    public boolean isMirror(Context context) {
        // consider this is a rearview mirrew
        // screen when the radio of width to height is bigger than 16:9
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        return screenWidth * 1.0f / screenHeight > 16.0f / 9;
    }

    public void adaptDensity(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int densityDpi = displayMetrics.densityDpi;
        float density = displayMetrics.density;
        float scaledDensity = displayMetrics.scaledDensity;
        float fontScale = configuration.fontScale;
        LogUtil.i("ScreenAdapter", "resolution=" + screenWidth + "*" + screenHeight + ",densityDpi="
                + densityDpi + ",density=" + density + ",scaledDensity=" + scaledDensity + ",fontScale=" + fontScale);
        for (StandardScreen screen : mScreenList) {
            if (screen.match(screenWidth, screenHeight)) {
                if (densityDpi != screen.density) {
                    if (!isNowadaOrFYT()) {
                        updateConfiguration(resources, screen.density, screen.fontScale);
                    } else {
                        updateConfiguration(resources, 200, screen.fontScale);
                    }
                } else {
                    LogUtil.i("ScreenAdapter", "the screen params is standard");
                }
                return;
            }
        }
        LogUtil.e("ScreenAdapter", "screen adapt failed, resolution=" + screenWidth + "*" + screenHeight);
    }

    private boolean isNowadaOrFYT() {
        return TextUtils.equals(BRAND_NOWADA, Build.BRAND) || TextUtils.equals(BRAND_FYT, Build.BRAND);
    }

    private void updateConfiguration(Resources resources, int densityDpi, float fontScale) {
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float density = densityDpi / BASE_DPI;
        configuration.densityDpi = densityDpi;
        configuration.fontScale = fontScale;
        displayMetrics.densityDpi = densityDpi;
        displayMetrics.density = density;
        displayMetrics.scaledDensity = density;
        resources.updateConfiguration(configuration, displayMetrics);
        LogUtil.i("ScreenAdapter",
                "[new] densityDpi=" + densityDpi + ",density=scaledDensity=" + density + ",fontScale=" + fontScale);
    }
}
