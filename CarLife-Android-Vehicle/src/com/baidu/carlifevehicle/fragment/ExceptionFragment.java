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

import com.baidu.carlife.protobuf.CarlifeDeviceInfoProto.CarlifeDeviceInfo;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.connect.ConnectNcmDriverClient;
import com.baidu.carlifevehicle.logic.CarlifeDeviceInfoManager;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExceptionFragment extends BaseFragment implements OnClickListener {

    private static final String TAG = "ExceptionFragment";
    private static final int CLICK_INTERVAL = 5000;
    private boolean isCalling = false ; 
    private ImageView mDisplayImgView = null;
    private TextView mDisplayTextView = null;
    private Button mStartAppBtn;
    private String mExceptionTips;
    private int mExceptionDrawableId;
    private static ExceptionFragment mExceptionFragment;
    private long mClickTime = System.currentTimeMillis();
    private static boolean mStartAppBtnVisible = true;

    public static ExceptionFragment getInstance() {
        if (mExceptionFragment == null) {
            mExceptionFragment = new ExceptionFragment();
        }
        return mExceptionFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LogUtil.d(TAG, "onCreateView");
        mContentView = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.frag_exception, null);
        mDisplayImgView = (ImageView) mContentView.findViewById(R.id.exception_display_img_view);
        mDisplayTextView = (TextView) mContentView.findViewById(R.id.exception_display_text_view);
        if (ConnectManager.CONNECTED_BY_NCM_IOS != ConnectManager.getInstance().getConnectType()
                && ConnectManager.CONNECTED_BY_WIFI != ConnectManager.getInstance().getConnectType()) {
            LogUtil.d(TAG, "android phone is install");
            setIsCalling(false);
        }
        mStartAppBtn = (Button) mContentView.findViewById(R.id.exception_start_app_btn);
        CarlifeDeviceInfo carlifeDeviceInfo = CarlifeDeviceInfoManager.getInstance().getPhoneDeviceInfo();
        if ((carlifeDeviceInfo == null)
                || (ConnectManager.CONNECTED_BY_WIFI == ConnectManager.getInstance().getConnectType()) 
                || isCalling) {
            // hide
            LogUtil.d(TAG, "hide");
            setIsCalling(false);
            mStartAppBtn.setVisibility(View.GONE);
            mStartAppBtnVisible = false;
        } else {
            // show
            LogUtil.d(TAG, "show");
            mStartAppBtn.setOnClickListener(this);
            // mStartAppBtnVisible = true;
        }

        if (mStartAppBtnVisible) {
            mStartAppBtn.setVisibility(View.VISIBLE);
        } else {
            mStartAppBtn.setVisibility(View.GONE);
        }
        
        if (!TextUtils.isEmpty(mExceptionTips)) {
            mDisplayTextView.setText(mExceptionTips);
        }
        if (mExceptionDrawableId > 0) {
            mDisplayImgView.setImageResource(mExceptionDrawableId);
        }

        // adapt for EL_AFTER_MARKET
        if (CommonParams.VEHICLE_CHANNEL.equals(CommonParams.VEHICLE_CHANNEL_EL_AFTER_MARKET)) {
            mStartAppBtn.setVisibility(View.GONE);
            mStartAppBtn = null;
            changeUILayout();
        }
        return mContentView;
    }
    
    private void changeUILayout() {
        RelativeLayout.LayoutParams params = null;
        
        params = (RelativeLayout.LayoutParams) mDisplayImgView.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        params.height = 200;
        mDisplayImgView.setLayoutParams(params);
        
        params = (RelativeLayout.LayoutParams) mDisplayTextView.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        mDisplayTextView.setLayoutParams(params);
        mDisplayTextView.setTextSize(22.0f);
        
        mContentView.invalidate();
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
    }

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - mClickTime > CLICK_INTERVAL) {
            mClickTime = clickTime;
        } else {
            return;
        }
        switch (v.getId()) {
            case R.id.exception_start_app_btn:
                if ((ConnectManager.CONNECTED_TYPE == ConnectManager.CONNECTED_BY_NCM_IOS)
                        || (ConnectManager.CONNECTED_TYPE == ConnectManager.CONNECTED_BY_EAN)) {
                    ConnectNcmDriverClient.getInstance().writeDataToDriver(ConnectNcmDriverClient.PULL_UP_CARLIFE);
                } else {
                    CarlifeUtil.sendGotoCarlife();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mActivity != null) {
            mActivity.openExitAppDialog();
        }
        return true;
    }

    public void changeTipsCallback(String tips) {
        if (!TextUtils.isEmpty(tips)) {
            mExceptionTips = tips;
        }
        if (mDisplayTextView != null) {
            mDisplayTextView.setText(mExceptionTips);
        }
    }

    public void setIsCalling(boolean state) {
        LogUtil.d(TAG, "setIsCalling" + state);
        isCalling = state;
    }
    
    public void setStartAppBtnHide() {
        LogUtil.d(TAG, "setStartAppBtnHide");
        if (ConnectManager.CONNECTED_BY_NCM_IOS == ConnectManager.getInstance().getConnectType()
                || ConnectManager.CONNECTED_BY_WIFI == ConnectManager.getInstance().getConnectType()) {
            setIsCalling(true);
        }
        if (mStartAppBtn != null) {
            mStartAppBtn.setVisibility(View.GONE);
        }
        mStartAppBtnVisible = false;
    }
    
    public void setStartAppBtnVisible() {
        if (mStartAppBtn != null) {
            mStartAppBtn.setVisibility(View.VISIBLE);
        }
        mStartAppBtnVisible = true;
    }
    
    public void changeDrawableCallback(int drawableId) {
        if (drawableId > 0) {
            mExceptionDrawableId = drawableId;
        }
        if (mDisplayImgView != null) {
            mDisplayImgView.setImageResource(mExceptionDrawableId);
        }
    }

}