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
package com.baidu.carlifevehicle.view;

import com.baidu.carlifevehicle.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CarlifeDialog extends Dialog {
    private TextView mTitleBar;
    private FrameLayout mContent;
    private TextView mFirstBtn;
    private TextView mSecondBtn;

    private OnCarlifeClickListener mOnFirstBtnClickListener;
    private OnCarlifeClickListener mOnSecondBtnClickListener;

    private boolean mFirstHasText;
    private boolean mSecondHasText;

    public CarlifeDialog(Activity activity) {
        super(activity, R.style.CarlifeDialog);
        setContentView(R.layout.dialog_carlife);
        mTitleBar = (TextView) findViewById(R.id.dialog_carlife_title_bar);
        mContent = (FrameLayout) findViewById(R.id.dialog_carlife_content);
        mFirstBtn = (TextView) findViewById(R.id.dialog_carlife_first_btn);
        mSecondBtn = (TextView) findViewById(R.id.dialog_carlife_second_btn);

        mFirstBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnFirstBtnClickListener != null) {
                    mOnFirstBtnClickListener.onClick();
                }
                CarlifeDialog.this.dismiss();
            }
        });

        mSecondBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnSecondBtnClickListener != null) {
                    mOnSecondBtnClickListener.onClick();
                }
                CarlifeDialog.this.dismiss();
            }
        });

        mFirstHasText = false;
        mSecondHasText = false;

        mTitleBar.setVisibility(View.GONE);
        mFirstBtn.setVisibility(View.GONE);
        mSecondBtn.setVisibility(View.GONE);

        setCanceledOnTouchOutside(false);
    }

    public CarlifeDialog setTitleText(String text) {
        if (text == null) {
            mTitleBar.setVisibility(View.GONE);
            mTitleBar.setText("", BufferType.SPANNABLE);
        } else {
            mTitleBar.setVisibility(View.VISIBLE);
            mTitleBar.setText(text, BufferType.SPANNABLE);
        }

        return this;
    }

    public CarlifeDialog setTitleText(int resId) {
        String text = getContext().getString(resId);
        return setTitleText(text);
    }

    public CarlifeDialog setFirstBtnText(String text) {
        if (text == null) {
            mFirstHasText = false;
            mFirstBtn.setText("", BufferType.SPANNABLE);
        } else {
            mFirstHasText = true;
            mFirstBtn.setText(text, BufferType.SPANNABLE);
        }
        setBtnVisible();
        return this;
    }

    public CarlifeDialog setFirstBtnText(int resId) {
        String text = getContext().getString(resId);
        return setFirstBtnText(text);
    }

    public CarlifeDialog setFirstBtnTextColorHighLight() {
        mFirstBtn.setTextColor(0xff4384f0);
        return this;
    }

    public CarlifeDialog setSecondBtnTextColorHighLight() {
        mSecondBtn.setTextColor(0xff4384f0);
        return this;
    }

    public CarlifeDialog setSecondBtnText(String text) {
        if (text == null) {
            mSecondHasText = false;
            mSecondBtn.setText("", BufferType.SPANNABLE);
        } else {
            mSecondHasText = true;
            mSecondBtn.setText(text, BufferType.SPANNABLE);
        }
        setBtnVisible();
        return this;
    }

    public CarlifeDialog setSecondBtnText(int resId) {
        String text = getContext().getString(resId);
        return setSecondBtnText(text);
    }

    public CarlifeDialog setContent(View content) {
        mContent.removeAllViews();
        mContent.addView(content);
        return this;
    }

    /**
     * set the content and set the width and height of parent view
     *
     * @param content the content view
     * @param parentViewWidth width to be set
     * @param parentViewHeight height to be set
     * @return this@CarlifeDialog
     */
    public CarlifeDialog setContent(View content, int parentViewWidth, int parentViewHeight) {
        mContent.removeAllViews();
        ViewGroup.LayoutParams layoutParams = mContent.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = parentViewWidth;
            layoutParams.height = parentViewHeight;
            mContent.setLayoutParams(layoutParams);
        }
        mContent.addView(content);
        return this;
    }

    public CarlifeDialog setOnFirstBtnClickListener(OnCarlifeClickListener listener) {
        mOnFirstBtnClickListener = listener;
        return this;
    }

    public CarlifeDialog setOnSecondBtnClickListener(OnCarlifeClickListener listener) {
        mOnSecondBtnClickListener = listener;
        return this;
    }

    public CarlifeDialog setSecondBtnClickDismissless() {
        mSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSecondBtnClickListener != null) {
                    mOnSecondBtnClickListener.onClick();
                }
            }
        });
        return this;
    }

    public CarlifeDialog setContentWidth(int width) {
        LayoutParams params = mContent.getLayoutParams();
        params.width = width;
        mContent.setLayoutParams(params);
        return this;
    }

    public CarlifeDialog setContentHeight(int height) {
        LayoutParams params = mContent.getLayoutParams();
        params.height = height;
        mContent.setLayoutParams(params);
        return this;
    }

    public CarlifeDialog setFirstBtnEnabled(boolean enabled) {
        mFirstBtn.setEnabled(enabled);
        return this;
    }

    public CarlifeDialog setSecondBtnEnabled(boolean enabled) {
        mSecondBtn.setEnabled(enabled);
        return this;
    }

    /**
     * set whether to dismiss dialog
     *
     * @param cancelable set whether dialog is cancelable
     * @return this@CarlifeDialog
     */
    public CarlifeDialog enableBackKey(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    private void setBtnVisible() {
        if (!mFirstHasText) {
            mFirstBtn.setVisibility(View.GONE);
            mSecondBtn.setVisibility(View.GONE);
        } else if (!mSecondHasText) {
            mFirstBtn.setVisibility(View.VISIBLE);
            mSecondBtn.setVisibility(View.GONE);
        } else {
            mFirstBtn.setVisibility(View.VISIBLE);
            mSecondBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Interface definition for a callback to be invoked when carlife view is clicked
     */
    public interface OnCarlifeClickListener {
        /**
         * invoked when carlife view is clicked
         */
        public void onClick();
    }
}
