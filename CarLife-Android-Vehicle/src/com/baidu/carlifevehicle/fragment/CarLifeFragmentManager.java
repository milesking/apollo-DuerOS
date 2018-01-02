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

import java.util.List;

import com.baidu.carlifevehicle.BaseActivity;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.LogUtil;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CarLifeFragmentManager {
    private static final String TAG = "CarLifeFragmentManager";
    private FragmentManager mFragmentManager;
    private BaseFragment mCurrentFragment;

    public CarLifeFragmentManager(BaseActivity activity) {
        try {
            LogUtil.e(TAG, "task id = " + activity.toString());
            mFragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
            List<Fragment> mListFragment = mFragmentManager.getFragments();
            MainFragment mMainFragment = MainFragment.getInstance();
            if (mListFragment != null) {
                int mListSize = mListFragment.size();
                LogUtil.d(TAG, "mListFragment size is " + mListSize);
                for (int i = 0; i < mListSize; i++) {
                    Fragment mFrag = mListFragment.get(i);
                    mFragmentTransaction.remove(mFrag);
                }
            } else {
                LogUtil.d(TAG, "mListFragment is null");
            }

            if (!mMainFragment.isAdded()) {
                mFragmentTransaction.add(R.id.main_content_frame,
                        mMainFragment, mMainFragment.getClass().getSimpleName());
            }
            mFragmentTransaction.commitAllowingStateLoss();
            mCurrentFragment = mMainFragment;
        } catch (Exception ex) {
            LogUtil.e(TAG, "CarLifeFragmentManager create fail");
            ex.printStackTrace();
        }
    }

    /**
     * 
     * @return the fragment currently displayed
     */
    public BaseFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     *
     * @param fragment the fragment to be displayed
     */
    public void showFragment(BaseFragment fragment) {
        if (fragment != null && mCurrentFragment != null && !fragment.equals(mCurrentFragment)) {
            try {
                Fragment localFragment = mFragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                // if fragment is MainFragment,hide it, otherwise, remove it
                if (MainFragment.class.getSimpleName().equals(mCurrentFragment.getClass().getSimpleName())
                        || TouchFragment.class.getSimpleName().equals(mCurrentFragment.getClass().getSimpleName())) {
                    LogUtil.i(TAG, "hide" + mCurrentFragment.getClass().getSimpleName());
                    fragmentTransaction.hide(mCurrentFragment);
                } else {
                    LogUtil.i(TAG, "remove" + mCurrentFragment.getClass().getSimpleName());
                    fragmentTransaction.remove(mCurrentFragment);
                }
                // if fragment is added to fragment stack，display it, otherwise, add it
                if (localFragment != null) {
                    LogUtil.i(TAG, "Show" + localFragment.getClass().getSimpleName());
                    fragmentTransaction.show(fragment);
                } else {
                    LogUtil.i(TAG, "Add" + fragment.getClass().getSimpleName());
                    if (!fragment.isAdded()) {
                        fragmentTransaction.add(R.id.main_content_frame, fragment, fragment.getClass().getSimpleName());
                    }
                }
                fragmentTransaction.commitAllowingStateLoss();
                mCurrentFragment = fragment;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
