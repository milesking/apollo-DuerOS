/******************************************************************************
 * Copyright 2018 The Baidu Authors. All Rights Reserved.
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
package com.baidu.che.codriverlauncher.ui;

import java.util.Stack;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * all activity should extends this class
 */
public abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";

    private static Stack<BaseActivity> sActivities = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        addActivity(this);
    }

    @Override
    protected void onStart() {
        LogUtil.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume()");
        super.onResume();

    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy()");
        super.onDestroy();

        removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed()");
        super.onBackPressed();
    }

    private static void addActivity(BaseActivity activity) {
        if (activity == null) {
            return;
        }
        sActivities.push(activity);
    }

    private static void removeActivity(BaseActivity activity) {
        if (activity == null) {
            return;
        }

        if (sActivities.contains(activity)) {
            sActivities.remove(activity);
        }
    }

    public static void clearActivities() {
        if (!sActivities.empty()) {
            for (BaseActivity activity : sActivities) {
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
            sActivities.clear();
        }
    }

    public static void clearActivities(BaseActivity activity) {
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
        removeActivity(activity);
    }

    protected void initTitleBar(String titleText) {
        TextView title = (TextView) findViewById(R.id.title_text);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_title);
        if (title != null) {
            title.setText(titleText);
        }

        if (linearLayout != null) {
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}
