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

import java.util.ArrayList;
import java.util.List;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.model.AppInfo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * show all app
 */

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private List<AppInfo> mAppList;

    private boolean isDeleting = false;

    public interface OnItemClickListener {
        void onItemClick(int position, AppInfo appInfo);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, AppInfo appInfo);
    }

    public AppListAdapter(Context context) {
        mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setData(List<AppInfo> appList) {
        if (appList != null) {
            mAppList = new ArrayList<>();
            mAppList.addAll(appList);
        }
    }

    public void setDeleting(boolean isDeleting) {
        this.isDeleting = isDeleting;
    }

    public boolean isDeleting() {
        return isDeleting;
    }

    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_more, parent, false);
        AppListAdapter.ViewHolder viewHolder = new AppListAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AppListAdapter.ViewHolder holder, final int position) {
        final AppInfo item = getItem(position);
        if (item != null) {
            holder.icon.setImageDrawable(item.getIcon());
            holder.title.setText(item.getAppName());
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, item);
                    }
                }
            });
            holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        mOnItemLongClickListener.onItemLongClick(position, item);
                    }
                    return true;
                }
            });
            if (isDeleting() && !item.isSystem()) {
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.delete.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mAppList == null ? 0 : mAppList.size();
    }

    public AppInfo getItem(int position) {
        return mAppList == null ? null : mAppList.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View root;
        ImageView icon;
        ImageView delete;
        TextView title;

        public ViewHolder(View view) {
            super(view);
            root = view.findViewById(R.id.more_recyclerview_item_root);
            icon = (ImageView) view.findViewById(R.id.more_recyclerview_item_icon);
            delete = (ImageView) view.findViewById(R.id.more_recyclerview_item_delete);
            title = (TextView) view.findViewById(R.id.more_recyclerview_item_title);
        }
    }
}