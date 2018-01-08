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
package com.baidu.che.codriverlauncher.ui;

import java.util.List;

import com.baidu.che.codriverlauncher.BaseActivity;
import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.adapter.AppManagerAdapter;
import com.baidu.che.codriverlauncher.model.AppInfo;
import com.baidu.che.codriverlauncher.receiver.AppBroadcastReceiver;
import com.baidu.che.codriverlauncher.util.AppInfoProvider;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;
import com.baidu.che.codriverlauncher.view.BlankGridView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * show all app of the device
 */
public class MoreActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, AppBroadcastReceiver.AppChangedCallback {

    private static final String TAG = "MoreActivity";
    private static final int GET_ALLAPP = 0;
    private static final int UPDATE_ALLAPP = 1;
    private static final int ADD_SINGLE_APP = 2;
    private static final int REMOVED_SINGLE_APP = 3;
    private static final int CANCEL_UNINSTALL = 4;

    private BlankGridView mGridView;

    private AppInfoProvider mProvider;
    private List<AppInfo> mAllAppList;


    private AppManagerAdapter mAppAdapter;
    private Toast toast;
    private AppBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        initView();
        getData();
        registAppReceiver();
    }

    private void initView() {
        findViewById(R.id.more_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mGridView = (BlankGridView) findViewById(R.id.more_gridview);
        mGridView.setOnTouchInvalidPositionListener(new BlankGridView.OnTouchInvalidPositionListener() {
            @Override
            public boolean onTouchInvalidPosition(int motionEvent) {
                if (motionEvent == MotionEvent.ACTION_UP) {
                    mHandler.sendEmptyMessage(CANCEL_UNINSTALL);
                }
                return false;
            }
        });
    }

    public void getData() {
        new Thread() {
            public void run() {
                mProvider = new AppInfoProvider(MoreActivity.this);
                mAllAppList = mProvider.queryAppInfo();
                LogUtil.d(TAG, "GET_ALLAPP_size()：" + mAllAppList.size() + "");
                Message msg = new Message();
                msg.what = GET_ALLAPP;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    private void registAppReceiver() {
        mReceiver = new AppBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addDataScheme("package");
        this.registerReceiver(mReceiver, filter);
    }


    private void updateData() {
        new Thread() {
            public void run() {
                mProvider = new AppInfoProvider(MoreActivity.this);
                mAllAppList = mProvider.queryAppInfo();
                LogUtil.d(TAG, "UPDATE_ALLAPP_size()：" + mAllAppList.size() + "");
                Message msg = new Message();
                msg.what = UPDATE_ALLAPP;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_ALLAPP:
                    mAppAdapter = new AppManagerAdapter(MoreActivity.this, mAllAppList);
                    mGridView.setAdapter(mAppAdapter);
                    mGridView.setOnItemClickListener(MoreActivity.this);
                    mGridView.setOnItemLongClickListener(MoreActivity.this);
                    break;
                case UPDATE_ALLAPP:
                    mAppAdapter.setAppInfos(mAllAppList);
                    mAppAdapter.notifyDataSetChanged();
                    break;
                case ADD_SINGLE_APP:
                    final int pos = msg.arg1;
                    mAppAdapter.notifyDataSetChanged();
                    mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mGridView.smoothScrollToPosition(pos);
                        }
                    });
                    break;
                case REMOVED_SINGLE_APP:
                    break;
                case CANCEL_UNINSTALL:
                    mAppAdapter.setDeleting(false);
                    mAppAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // launcher this app when is clicked
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
                            long arg3) {
        if (mAppAdapter != null) {
            if (!mAppAdapter.isDeleting()) {
                Intent intent = mAllAppList.get(position).getIntent();
                LauncherUtil.startActivitySafely(intent);
            } else if (!mAllAppList.get(position).isSystem()) {
                String strUri = "package:" + mAllAppList.get(position).getPackageName();
                Uri uri = Uri.parse(strUri);
                Intent delectIntent = new Intent();
                delectIntent.setAction(Intent.ACTION_DELETE);
                delectIntent.setData(uri);
                startActivityForResult(delectIntent, 0);
            } else {
                showToast(getString(R.string.app_cannot_uninstall));
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mAppAdapter.setDeleting(true);
        mAppAdapter.notifyDataSetChanged();
        for (AppInfo info : mAllAppList) {
            if (info.isSystem()) {
                LogUtil.d("AppInfo,getPackageName：", info.getPackageName() + ";getAppName:" + info
                        .getAppName() + ";getActivityName:" + info
                        .getActivityName());
            }
        }
        return true;
    }

    private BroadcastReceiver receiver;

    @Override
    public void onResume() {
        super.onResume();
        if (mAppAdapter != null) {
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
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
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
            toast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    @Override
    public void onAdded(final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities
                        (mainIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                if (!TextUtils.isEmpty(packageName)) {
                    String[] split = packageName.split(":");
                    if (split != null && split.length > 0) {
                        String filter = split[split.length - 1];
                        for (int i = 0; i < resolveInfos.size(); i++) {
                            AppInfo singleInfo = mProvider.getSingleInfo(resolveInfos.get(i), filter);
                            if (singleInfo != null) {
                                int pos = mAppAdapter.addThridApp(singleInfo);
                                Message msg = mHandler.obtainMessage(ADD_SINGLE_APP);
                                msg.arg1 = pos;
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                }

            }
        }).start();
    }
}
