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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SettingAboutWindow implements OnClickListener {
    public static final String TAG = "SettingAboutWindow";
    private static SettingAboutWindow mInstance = null;

    private ViewGroup mRootView = null;
    private Context mContext = null;
    private PopupWindow mFullWindow = null;
    private View mFullWindowLayout = null;
    private ImageButton mExitBtn = null;
    private TextView mTvVersionCode;

    private static String mPackageName;
    private static String mVersionName;
    private static int mVersionCode = -1;

    private SettingAboutWindow() {
    }

    public static SettingAboutWindow getInstance() {
        if (null == mInstance) {
            synchronized (SettingAboutWindow.class) {
                if (null == mInstance) {
                    mInstance = new SettingAboutWindow();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context, ViewGroup parent) {
        try {
            mContext = context;
            mRootView = parent;

            mFullWindowLayout = LayoutInflater.from(mContext).inflate(R.layout.frag_setting_about, null);
            mFullWindow = new PopupWindow(mFullWindowLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            mExitBtn = (ImageButton) mFullWindowLayout.findViewById(R.id.exit_img_btn);
            mExitBtn.setOnClickListener(this);

            mTvVersionCode = (TextView) mFullWindowLayout.findViewById(R.id.about_tv_version_code);

            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            mPackageName = pi.packageName;
            mVersionName = pi.versionName;
            mVersionCode = pi.versionCode;
            String temp = mContext.getString(R.string.version_code_prefix)
                     + mVersionName;
            if (!"".equals(CommonParams.BUILD_NUMBER)) {
                temp += " (" + CommonParams.BUILD_NUMBER + ")";
            }
            mTvVersionCode.setText(temp);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void displayWindow() {
        LogUtil.d(TAG, "----displayWindow()----");
        mFullWindow.setFocusable(true);
        mFullWindow.showAtLocation(mRootView, Gravity.CENTER, 0, 0);
    }

    public void closeWindow() {
        LogUtil.d(TAG, "----closeWindow()----");
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
            default:
                break;
        }
    }

}
