/*
 * 循环等待的主Fragment
 */
package com.baidu.carlifevehicle.fragment;

import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.LogUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class NCMRequestHelpFragment extends BaseFragment {

    private static final String TAG = "NCMRequestHelpFragment";

    private ImageButton mBackBtn;
    private TextView mTitle;

    private static NCMRequestHelpFragment mNCMRequestHelpFragment;

    public static NCMRequestHelpFragment getInstance() {
        if (mNCMRequestHelpFragment == null) {
            mNCMRequestHelpFragment = new NCMRequestHelpFragment();
        }

        return mNCMRequestHelpFragment;
    }

    public NCMRequestHelpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        LogUtil.d(TAG, "onCreateView");
        mContentView = (ViewGroup) LayoutInflater.from(mActivity)
                .inflate(R.layout.frag_ncm_help, null);

        mBackBtn = (ImageButton) mContentView.findViewById(R.id.ib_left);
        mBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        mTitle.setText(getString(R.string.ncm_request_title));
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
    }

    @Override
    public boolean onBackPressed() {
        if (mFragmentManager != null) {
            mFragmentManager.showFragment(HelpAppleNCMFragment.getInstance());
        }
        return true;
    }
}