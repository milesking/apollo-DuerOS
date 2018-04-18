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

import java.lang.ref.WeakReference;
import java.util.List;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.adapter.AppManagerAdapter;
import com.baidu.che.codriverlauncher.model.AppInfo;
import com.baidu.che.codriverlauncher.util.AppInfoProvider;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * show all app of the device
 */
public class MoreActivity extends BaseActivity implements OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private static final String TAG = "MoreActivity";
    private AppInfoProvider mProvider;
    private List<AppInfo> mAllAppList;

    private GridView mAllAppGridView;
    private static final int GET_ALL_APP = 0;
    private static final int UPDATE_ALL_APP = 1;
    private Context mContext;

    private AppManagerAdapter mAppAdapter;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        initTitleBar(getResources().getString(R.string.more));
        initView();
    }

    private void initView() {
        mContext = this;
        mAllAppGridView = (GridView) findViewById(R.id.gv_all_app);
        findViewById(R.id.common_activity_title).setOnClickListener(this);
        // Get data
        getData();
    }

    public void getData() {
        new Thread() {
            public void run() {
                mProvider = new AppInfoProvider(MoreActivity.this);
                mAllAppList = mProvider.queryAppInfo();
                LogUtil.d(TAG, "GET_ALLAPP_size()：" + mAllAppList.size() + "");
                Message msg = new Message();
                msg.what = GET_ALL_APP;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private void updateData() {
        new Thread() {
            public void run() {
                mProvider = new AppInfoProvider(MoreActivity.this);
                mAllAppList = mProvider.queryAppInfo();
                LogUtil.d(TAG, "UPDATE_ALLAPP_size()：" + mAllAppList.size() + "");
                Message msg = new Message();
                msg.what = UPDATE_ALL_APP;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    final Handler mHandler = new CustomHandler(this);

    private static class CustomHandler extends Handler {
        private WeakReference<MoreActivity> weakReference;
        private MoreActivity activity;

        public CustomHandler(MoreActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            activity = weakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case GET_ALL_APP:
                    activity.mAppAdapter = new AppManagerAdapter(activity.mContext,
                            activity.mAllAppList);
                    activity.mAllAppGridView.setAdapter(activity.mAppAdapter);
                    activity.mAllAppGridView.setOnItemClickListener(activity);
                    activity.mAllAppGridView.setOnItemLongClickListener(activity);
                    break;
                case UPDATE_ALL_APP:
                    activity.mAppAdapter.setAppInfos(activity.mAllAppList);
                    activity.mAppAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_activity_title:
                finish();
                break;
            default:
                break;
        }
    }

    // Click to jump to the application
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
                            long arg3) {
        if (mAppAdapter != null) {
            if (!mAppAdapter.isDeleting()) {
                Intent intent = mAllAppList.get(position).getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (!mAllAppList.get(position).isSystem()) {
                String strUri = "package:" + mAllAppList.get(position).getPackageName();
                Uri uri = Uri.parse(strUri); // Through a uri to access the package name that you want to uninstall
                Intent delectIntent = new Intent();
                delectIntent.setAction(Intent.ACTION_DELETE);
                delectIntent.setData(uri);
                startActivityForResult(delectIntent, 0);
            } else {
                showToast(getResources().getString(R.string.more_not_uninstall));
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mAppAdapter.setDeleting(true);
        mAppAdapter.notifyDataSetChanged();
        for (AppInfo info : mAllAppList) {
            if (info.isSystem()) {
                LogUtil.d(TAG, "AppInfo,getPackageName：" + info.getPackageName()
                        + ";getAppName:" + info.getAppName() + ";getActivityName:"
                        + info.getActivityName());
            }
        }
        return false;
    }

    private BroadcastReceiver receiver;

    @Override
    public void onResume() {
        super.onResume();
        if (mAppAdapter != null) {
            // update data
            updateData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppAdapter != null && mAppAdapter.isDeleting()) {
            mAppAdapter.setDeleting(false);
            mAppAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAppAdapter != null && mAppAdapter.isDeleting()) {
            mAppAdapter.setDeleting(false);
            mAppAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    public void showToast(String content) {
        if (toast == null) {
            toast = Toast.makeText(mContext,
                    content,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
