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
package com.baidu.carlifevehicle.fragment;

import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpMainFragment extends BaseFragment {

    private static final String TAG = "HelpMainFragment";

    private ImageButton mBackBtn;
    private TextView mTitle;
    private TextView mAndroid;
    private TextView mApple;
    private View mAndroidLayout;
    private View mAppleLayout;

    private static HelpMainFragment mHelpMainFragment;

    public static HelpMainFragment getInstance() {
        if (mHelpMainFragment == null) {
            mHelpMainFragment = new HelpMainFragment();
        }
        return mHelpMainFragment;
    }

    public HelpMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LogUtil.d(TAG, "onCreateView");
        mContentView = (ViewGroup) LayoutInflater.from(mActivity)
                .inflate(R.layout.frag_help_main, null);

        mBackBtn = (ImageButton) mContentView.findViewById(R.id.ib_left);
        mBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        mTitle.setText(getString(R.string.help_main_title));
        mAndroid = (TextView) mContentView.findViewById(R.id.android_device);
        mApple = (TextView) mContentView.findViewById(R.id.apple_device);
        mAndroidLayout = mContentView.findViewById(R.id.goto_android_layout);
        mAppleLayout = mContentView.findViewById(R.id.goto_apple_layout);
        mAndroidLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int connectTypeAndroidProperty =
                        CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_CONNECT_TYPE_ANDROID);
                if (connectTypeAndroidProperty == 0) {
                    mFragmentManager.showFragment(HelpAndroidUSBFragment.getInstance());
                } else {
                    mFragmentManager.showFragment(HelpAndroidAOAFragment.getInstance());
                }
                
            }
        });
        mAppleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int connectTypeAndroidProperty =
                        CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_CONNECT_TYPE_IPHONE);
                if (connectTypeAndroidProperty != 4) {
                    mFragmentManager.showFragment(HelpAppleFragment.getInstance());
                } else {
                    mFragmentManager.showFragment(HelpAppleNCMFragment.getInstance());
                }
            }
        });
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }

    @Override
    public boolean onBackPressed() {
        if (mFragmentManager != null) {
            mFragmentManager.showFragment(MainFragment.getInstance());
        }
        return true;
    }
}