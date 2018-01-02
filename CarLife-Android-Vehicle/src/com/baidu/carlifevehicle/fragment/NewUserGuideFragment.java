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

import java.util.ArrayList;
import java.util.List;

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.view.DirectionViewPager;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class NewUserGuideFragment extends BaseFragment {
    static NewUserGuideFragment mNewUserGuideFragment = null;

    public static NewUserGuideFragment getInstance() {
        if (mNewUserGuideFragment == null) {
            mNewUserGuideFragment = new NewUserGuideFragment();
        }
        return mNewUserGuideFragment;

    }

    private DirectionViewPager mUserGuideViewPager;
    private int[] mDrawableIds = {R.drawable.car_qd01, R.drawable.car_qd02, R.drawable.car_qd03};
    private List<View> mViews = new ArrayList<View>();
    private RelativeLayout.LayoutParams mParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    private ViewPagerAdapter mViewPagerAdapter;
    private boolean flag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_guide, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserGuideViewPager = (DirectionViewPager) view.findViewById(R.id.guide_viewpager_user_guide);
        for (int i = 0; i < mDrawableIds.length; i++) {
            ImageView mImageView = new ImageView(mContext);
            mImageView.setImageResource(mDrawableIds[i]);
            mImageView.setScaleType(ScaleType.CENTER_INSIDE);
            mImageView.setLayoutParams(mParams);
            mViews.add(mImageView);
        }

        mViewPagerAdapter = new ViewPagerAdapter(mViews);
        mUserGuideViewPager.setAdapter(mViewPagerAdapter);
        mUserGuideViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (arg0 == mViewPagerAdapter.getCount() - 1 && mUserGuideViewPager.slidingLeft()
                        && !flag) {
                    MsgHandlerCenter.dispatchMessageDelay(
                            CommonParams.MSG_MAIN_DISPLAY_MAIN_FRAGMENT, 200);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        flag = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        flag = true;
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        flag = true;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private List<View> views;

        public ViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(views.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {

        }

        @Override
        public int getCount() {
            if (views != null) {
                return views.size();
            }

            return 0;
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {

            ((ViewPager) arg0).addView(views.get(arg1), 0);

            return views.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (arg0 == arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

    }

}
