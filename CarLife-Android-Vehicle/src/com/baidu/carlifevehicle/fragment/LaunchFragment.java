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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.message.MsgBaseHandler;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.LogUtil;
import com.baidu.carlifevehicle.util.PreferenceUtil;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/*
 * 启动Fragment
 */
public class LaunchFragment extends BaseFragment {

    private static final String TAG = "LaunchFragment";

    private MsgBaseHandler mHandler = null;

    private ImageView mBrandLogoIv;

    private ImageView mCopyRightIv;

    private static LaunchFragment mLaunchFragment;

    public static LaunchFragment getInstance() {
        if (mLaunchFragment == null) {
            mLaunchFragment = new LaunchFragment();
        }
        return mLaunchFragment;

    }

    public LaunchFragment() {
        // TODO Auto-generated constructor stub
        mHandler = new MsgLaunchFragmentHandler();
        MsgHandlerCenter.registerMessageHandler(mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LogUtil.d(TAG, "onCreateView");
        mContentView = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.frag_launch, null);
        mBrandLogoIv = (ImageView) mContentView.findViewById(R.id.launch_iv_brand_logo);
        mCopyRightIv = (ImageView) mContentView.findViewById(R.id.launch_iv_copyright);
        if (CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC)
                || CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_SHANGHAIGM_CADILLAC_DUAL_AUDIO)) {
            mBrandLogoIv.setImageResource(R.drawable.logo_cadillac);
        } else if (CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
            mBrandLogoIv.setImageResource(R.drawable.logo_changan);
        } else if (CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_BYD)) {
            mBrandLogoIv.setImageResource(R.drawable.logo_byd);
        } else if (CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_CHEVROLET_K216)) {
            mBrandLogoIv.setImageResource(R.drawable.logo_chevrolet);
        }
        boolean isFirstInstall = PreferenceUtil.getInstance().getBoolean(PreferenceUtil.FIRST_INSTALL_KEY, true);
        if (isFirstInstall) {
            // adapt for VEHICLE_CHANNEL_EL_AFTER_MARKET, not to display user guide
            if (!CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_EL_AFTER_MARKET)) {
                MsgHandlerCenter.dispatchMessageDelay(CommonParams.MSG_MAIN_DISPLAY_USER_GUIDE_FRAGMENT, 2000);
            } else {
                MsgHandlerCenter.dispatchMessageDelay(CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT, 2000);
            }
            PreferenceUtil.getInstance().putBoolean(PreferenceUtil.FIRST_INSTALL_KEY, false);
        } else {
            MsgHandlerCenter.dispatchMessageDelay(CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT, 2000);
        }
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LogUtil.d(TAG, "onDetach");
        MsgHandlerCenter.unRegisterMessageHandler(mHandler);
    }

    private class MsgLaunchFragmentHandler extends MsgBaseHandler {

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    default:
                        break;
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "launch fragment get exception");
            }

        }

        @Override
        public void careAbout() {
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mActivity != null) {
            mActivity.openExitAppDialog();
        }
        return true;
    }

}