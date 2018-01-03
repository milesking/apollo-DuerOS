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

import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.bluetooth.BtHfpManager;
import com.baidu.carlifevehicle.util.CarlifeUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class IncallFragment extends BaseFragment implements OnClickListener {

    private static final String TAG = IncallFragment.class.getSimpleName();
    private static IncallFragment mFragment;
    private View mIncomingLayout;
    private TextView mIncomingNameTV;
    private View mIncomingAnswerBtn;
    private View mIncomingBreakBtn;
    private View mOffhookLayout;
    private TextView mOffhookNameTV;
    private View mOffhookBreakBtn;

    public static IncallFragment getInstance() {
        if (mFragment == null) {
            mFragment = new IncallFragment();
        }
        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.frag_incall, null);
        mIncomingLayout = mContentView.findViewById(R.id.incall_layout_incoming);
        mIncomingNameTV = (TextView) mContentView.findViewById(R.id.incall_tv_incoming_name);
        mIncomingAnswerBtn = mContentView.findViewById(R.id.incall_ib_incoming_answer);
        mIncomingAnswerBtn.setOnClickListener(this);
        mIncomingBreakBtn = mContentView.findViewById(R.id.incall_ib_incoming_break);
        mIncomingBreakBtn.setOnClickListener(this);
        mOffhookLayout = mContentView.findViewById(R.id.incall_layout_offhook);
        mOffhookNameTV = (TextView) mContentView.findViewById(R.id.incall_tv_offhook_name);
        mOffhookBreakBtn = mContentView.findViewById(R.id.incall_ib_offhook_break);
        mOffhookBreakBtn.setOnClickListener(this);
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragment = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.incall_ib_incoming_answer:
                mIncomingAnswerBtn.setEnabled(!BtHfpManager.getInstance().answerCallNative());
                break;
            case R.id.incall_ib_incoming_break:
                mIncomingBreakBtn.setEnabled(!BtHfpManager.getInstance().rejectCallNative());
                break;
            case R.id.incall_ib_offhook_break:
                mOffhookBreakBtn.setEnabled(!BtHfpManager.getInstance().rejectCallNative());
                break;
            default:
                break;
        }
        CarlifeUtil.sendGotoCarlife();

    }

    @Override
    public boolean onBackPressed() {
        if (mActivity != null) {
            mActivity.openExitAppDialog();
        }
        return true;
    }

    public void showIncomingLayout(String str) {
        mIncomingLayout.setVisibility(View.VISIBLE);
        mOffhookLayout.setVisibility(View.GONE);
        mIncomingNameTV.setText(str);
    }

    public void showOutgoingLayout(String str) {
        mIncomingLayout.setVisibility(View.GONE);
        mOffhookLayout.setVisibility(View.VISIBLE);
        mOffhookNameTV.setText(str);
    }
}