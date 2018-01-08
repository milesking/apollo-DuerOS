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
package com.baidu.che.codriverlauncher.adapter;

import java.util.List;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.model.AppInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private final Animation mAnimation;

    public AppManagerAdapter(Context context, List<AppInfo> appInfos) {
        this.appInfos = appInfos;
        this.context = context;
        mAnimation = AnimationUtils.loadAnimation(context, R.anim.launcher_delete_anim);

    }

    public void setAppInfos(List<AppInfo> appInfos) {
        this.appInfos = appInfos;
    }

    public void setDeleting(boolean isDeleting) {
        this.isDeleting = isDeleting;
    }

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
        if (convertView == null) {
            View view = View.inflate(context, R.layout.gridview_item_app, null);
            AppManagerViews views = new AppManagerViews();
            views.appIcon = (ImageView) view.findViewById(R.id.grid_item_app_icon);
            views.appName = (TextView) view.findViewById(R.id.grid_item_app_name);
            views.delete = (ImageView) view.findViewById(R.id.grid_item_app_delete);
            views.appIcon.setImageDrawable(info.getIcon());
            views.appName.setText(info.getAppName());
            if (isDeleting()) {
                view.startAnimation(mAnimation);
            } else if (!isDeleting()) {
                view.clearAnimation();
            }
            if (isDeleting() && !info.isSystem()) {
                views.delete.setVisibility(View.VISIBLE);
            } else {
                views.delete.setVisibility(View.GONE);
            }
            view.setTag(views);
            return view;
        } else {
            AppManagerViews views = (AppManagerViews) convertView.getTag();
            views.appIcon.setImageDrawable(info.getIcon());
            views.appName.setText(info.getAppName());
            if (isDeleting()) {
                convertView.startAnimation(mAnimation);
            } else if (!isDeleting()) {
                convertView.clearAnimation();
            }
            if (isDeleting() && !info.isSystem()) {
                views.delete.setVisibility(View.VISIBLE);
            } else {
                views.delete.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    public int addThridApp(AppInfo singleInfo) {
        int pos = -1;
        if (appInfos != null && singleInfo != null) {
            appInfos.add(singleInfo);
            pos = appInfos.size();
        }
        return pos;
    }

    /**
     * optimization listview
     */
    private class AppManagerViews {
        ImageView appIcon;
        TextView appName;
        ImageView delete;
    }

}
