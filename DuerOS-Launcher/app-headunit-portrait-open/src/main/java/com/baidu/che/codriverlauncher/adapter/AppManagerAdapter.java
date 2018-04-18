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
package com.baidu.che.codriverlauncher.adapter;

import java.util.List;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.model.AppInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * show all app
 */

public class AppManagerAdapter extends BaseAdapter {
    private List<AppInfo> appInfos;
    private Context context;
    private boolean isDeleting = false;

    public AppManagerAdapter(Context context, List<AppInfo> appInfos) {
        this.appInfos = appInfos;
        this.context = context;
    }

    // Set the data of the adapter
    public void setAppInfos(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    // set isDeleting
    public void setDeleting(boolean isDeleting) {
        this.isDeleting = isDeleting;
    }

    // Whether or not it is being deleted
    public boolean isDeleting() {
        return isDeleting;
    }

    @Override
    public int getCount() {
        return appInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfo info = appInfos.get(position);
        String packageName = info.getPackageName();
        if (convertView == null) {
            View view = View.inflate(context, R.layout.gridview_item_app, null);
            AppManagerViews views = new AppManagerViews();
            views.ivAppIcon = (ImageView) view.findViewById(R.id.app_icon);
            views.tvAppName = (TextView) view.findViewById(R.id.app_name);
            views.ivDelete = (ImageView) view.findViewById(R.id.app_delete);
            views.ivAppIcon.setImageDrawable(info.getIcon());
            views.tvAppName.setText(info.getAppName());
            if (isDeleting() && !info.isSystem()) {
                views.ivDelete.setVisibility(View.VISIBLE);
            } else {
                views.ivDelete.setVisibility(View.GONE);
            }
            view.setTag(views);
            return view;
        } else {
            AppManagerViews views = (AppManagerViews) convertView.getTag();
            views.ivAppIcon.setImageDrawable(info.getIcon());
            views.tvAppName.setText(info.getAppName());

            if (isDeleting() && !info.isSystem()) {
                views.ivDelete.setVisibility(View.VISIBLE);
            } else {
                views.ivDelete.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    /**
     * The class used to optimize ListView
     */
    private class AppManagerViews {
        ImageView ivAppIcon;
        TextView tvAppName;
        ImageView ivDelete;
    }
}
