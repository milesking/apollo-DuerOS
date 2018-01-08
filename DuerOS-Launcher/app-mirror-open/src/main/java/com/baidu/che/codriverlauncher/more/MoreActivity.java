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
package com.baidu.che.codriverlauncher.more;

import java.util.List;

import com.baidu.che.codriverlauncher.BaseActivity;
import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.model.AppInfo;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * show all app in the device
 */

public class MoreActivity extends BaseActivity
        implements AppListAdapter.OnItemClickListener, AppListAdapter.OnItemLongClickListener {

    private static final String TAG = "MoreActivity";

    private static final int RECYCLER_VIEW_SPAN_COUNT = 2;

    private Toast mToast;

    private AppInfoProvider mProvider;

    private RecyclerView mRecyclerView;

    private AppListAdapter mAdapter;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        mProvider = new AppInfoProvider(this);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            loadingData();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null && mAdapter.isDeleting()) {
            mAdapter.setDeleting(false);
            mAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(int position, AppInfo appInfo) {
        if (mAdapter == null || appInfo == null) {
            return;
        }
        if (!mAdapter.isDeleting()) {
            LauncherUtil.startActivitySafely(appInfo.getIntent());
        } else if (appInfo.isSystem()) {
            showToast(getResources().getString(R.string.app_cannot_uninstall));
        } else {
            String strUri = "package:" + appInfo.getPackageName();
            Uri uri = Uri.parse(strUri);
            Intent deleteIntent = new Intent();
            deleteIntent.setAction(Intent.ACTION_DELETE);
            deleteIntent.setData(uri);
            startActivityForResult(deleteIntent, 0);
        }
    }

    @Override
    public void onItemLongClick(int position, AppInfo appInfo) {
        if (mAdapter != null) {
            mAdapter.setDeleting(true);
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        findViewById(R.id.more_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.more_recycler_view);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(RECYCLER_VIEW_SPAN_COUNT, StaggeredGridLayoutManager.HORIZONTAL));
        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!(v instanceof RecyclerView)) {
                        boolean isDeleting = mAdapter.isDeleting();
                        if (isDeleting) {
                            mAdapter.setDeleting(false);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
                return false;
            }
        });
        mAdapter = new AppListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
    }

    public void loadingData() {
        new Thread() {
            public void run() {
                final List<AppInfo> appList = mProvider.queryAppInfo();
                LogUtil.d(TAG, "app info size = " + appList.size() + "");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setData(appList);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    public void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }
}
