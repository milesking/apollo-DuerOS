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

import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.fragment.MainFragment;
import com.baidu.carlifevehicle.util.LogUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadingProgressBar extends RelativeLayout {
    private static final String TAG = "RingProgressBar";
    private View mView;
    private TextView mRateTv;
    private ImageView mLogoIv;
    private ImageView mProgressBar;
    private ImageView mLogoNameIv;
    private AnimatorSet animSet1;
    private AnimatorSet animSet2;
    private AnimatorSet animSet3;
    private AnimatorSet logoNameAnimSet;
    private ObjectAnimator windMillRotation;
    boolean cancel = false;

    public LoadingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LoadingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadingProgressBar(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.layout_progress_bar, this, true);
        mProgressBar = (ImageView) mView.findViewById(R.id.progress_bar);
        mLogoIv = (ImageView) mView.findViewById(R.id.progress_iv_logo);
        mLogoNameIv = (ImageView) mView.findViewById(R.id.progress_iv_logo_name);
        mRateTv = (TextView) mView.findViewById(R.id.pregress_progress_rate);
    }

    public void resetLoadingView() {
        LogUtil.d(TAG, "resetLoadingView");
        stopAnimation();
        mProgressBar.setVisibility(View.VISIBLE);
        mRateTv.setVisibility(View.VISIBLE);
        mLogoNameIv.setVisibility(View.GONE);
        mLogoIv.setAlpha(0.2f);
        mLogoIv.setScaleX(0.75f);
        mLogoIv.setScaleY(0.75f);
        windMillRotation = ObjectAnimator.ofFloat(mProgressBar, "rotation", 0, 360).setDuration(800);
        LinearInterpolator lir = new LinearInterpolator();
        windMillRotation.setInterpolator(lir);
        windMillRotation.setRepeatCount(Animation.INFINITE);
        windMillRotation.start();
        LogUtil.d(TAG, "windMillRotation.start()");
    }

    public void updateRate(int rate) {
        mRateTv.setText(getContext().getString(R.string.loading_progress_tips) + rate + "%");
        LogUtil.d(TAG, "rate=" + rate + "%");
    }

    public void loadFinished() {
        mProgressBar.setVisibility(View.GONE);
        mRateTv.setVisibility(View.GONE);
        ObjectAnimator logoScaleX1 = ObjectAnimator.ofFloat(mLogoIv, "scaleX", 0.75f, 0.6f).setDuration(200);
        ObjectAnimator logoScaleY1 = ObjectAnimator.ofFloat(mLogoIv, "scaleY", 0.75f, 0.6f).setDuration(200);

        ObjectAnimator logoScaleX2 = ObjectAnimator.ofFloat(mLogoIv, "scaleX", 0.6f, 1).setDuration(500);
        ObjectAnimator logoScaleY2 = ObjectAnimator.ofFloat(mLogoIv, "scaleY", 0.6f, 1).setDuration(500);
        ObjectAnimator logoScaleAlpha2 = ObjectAnimator.ofFloat(mLogoIv, "alpha", 0.2f, 0.6f).setDuration(300);
        ObjectAnimator logoScaleAlpha3 = ObjectAnimator.ofFloat(mLogoIv, "alpha", 0.6f, 1).setDuration(200);

        animSet1 = new AnimatorSet();
        animSet1.playTogether(logoScaleX1, logoScaleY1);

        animSet2 = new AnimatorSet();
        animSet2.playTogether(logoScaleX2, logoScaleY2, logoScaleAlpha2);

        animSet3 = new AnimatorSet();
        animSet3.play(animSet1).before(animSet2);

        animSet3.play(animSet2).before(logoScaleAlpha3);

        animSet3.start();
        animSet3.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLogoNameIv.setVisibility(View.VISIBLE);
                ObjectAnimator logoNameScaleAlpha = ObjectAnimator.ofFloat(mLogoNameIv, "alpha", 0, 1).setDuration(300);
                ObjectAnimator logoNameScaleX = ObjectAnimator.ofFloat(mLogoNameIv, "scaleX", 0, 1).setDuration(300);
                ObjectAnimator logoNameScaleY = ObjectAnimator.ofFloat(mLogoNameIv, "scaleY", 0, 1).setDuration(300);
                logoNameAnimSet = new AnimatorSet();
                logoNameAnimSet.play(logoNameScaleAlpha).with(logoNameScaleX).with(logoNameScaleY);
                logoNameAnimSet.start();
                logoNameAnimSet.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator arg0) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {

                    }

                    @Override
                    public void onAnimationEnd(Animator arg0) {
                        
                        LogUtil.d(TAG, "onAnimationEnd");
                        
                        if (!cancel) {
                            LogUtil.d(TAG, "check cancel=false");
                            stopAnimation();
                        } else {
                            LogUtil.d(TAG, "check cancel=ture");
                            cancel = false;
                        }
                        
                        try {
                            if (ConnectManager.getInstance().getConnectType() == ConnectManager.CONNECTED_BY_AOA
                                    && ConnectClient.getInstance().isCarlifeConnected()) {
                                String hintResStr =
                                        getResources().getString(R.string.usb_connect_aoa_request_md_permisson);
                                MainFragment.getInstance().updateExceptionTips(hintResStr);
                            }
                        } catch (Exception ex) {
                            LogUtil.d(TAG, "aoa connection get exception");
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator arg0) {
                        LogUtil.d(TAG, "onAnimationCancel");
                    }
                });

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

        });
    }

    public void stopAnimation() {
        LogUtil.d(TAG, "stopAnimation()");
        if (windMillRotation != null) {
            LogUtil.d(TAG, "windMillRotation.cancel()");
            windMillRotation.cancel();
            windMillRotation = null;
        }
        if (animSet1 != null) {
            animSet1.cancel();
            animSet1 = null;
        }
        if (animSet2 != null) {
            animSet2.cancel();
            animSet2 = null;
        }
        if (animSet3 != null) {
            LogUtil.d(TAG, "animSet3.cancel()");
            animSet3.cancel();
            cancel = true;
            animSet3 = null;
        }
    }
}
