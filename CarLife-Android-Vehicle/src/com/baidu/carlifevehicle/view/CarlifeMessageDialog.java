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
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class CarlifeMessageDialog extends CarlifeDialog {
    private TextView mTextView;

    public CarlifeMessageDialog(Activity activity) {
        super(activity);
        View view = getLayoutInflater().inflate(R.layout.dialog_carlife_message, null);
        mTextView = (TextView) view.findViewById(R.id.carlife_msg_text_view);
        setContent(view);
    }

    public CarlifeMessageDialog setMessage(String text) {
        mTextView.setText(text, BufferType.SPANNABLE);
        return this;
    }

    public CarlifeMessageDialog setMessage(int resId) {
        String text = getContext().getString(resId);
        return setMessage(text);
    }

    public CarlifeMessageDialog setMessageWidth(int width) {
        LayoutParams params = mTextView.getLayoutParams();
        params.width = width;
        mTextView.setLayoutParams(params);
        return this;
    }

    public CarlifeMessageDialog setMessageHeight(int height) {
        LayoutParams params = mTextView.getLayoutParams();
        params.height = height;
        mTextView.setLayoutParams(params);
        return this;
    }

    public CarlifeMessageDialog setTitleText(String text) {
        super.setTitleText(text);
        return this;
    }

    public CarlifeMessageDialog setTitleText(int resId) {
        super.setTitleText(resId);
        return this;
    }

    public CarlifeMessageDialog setFirstBtnText(String text) {
        super.setFirstBtnText(text);
        return this;
    }

    public CarlifeMessageDialog setFirstBtnText(int resId) {
        super.setFirstBtnText(resId);
        return this;
    }

    public CarlifeMessageDialog setSecondBtnText(String text) {
        super.setSecondBtnText(text);
        return this;
    }

    public CarlifeMessageDialog setSecondBtnText(int resId) {
        super.setSecondBtnText(resId);
        return this;
    }

    public CarlifeMessageDialog setContent(View content) {
        super.setContent(content);
        return this;
    }

    public CarlifeMessageDialog setOnFirstBtnClickListener(OnCarlifeClickListener listener) {
        super.setOnFirstBtnClickListener(listener);
        return this;
    }

    public CarlifeMessageDialog setOnSecondBtnClickListener(OnCarlifeClickListener listener) {
        super.setOnSecondBtnClickListener(listener);
        return this;
    }

    public CarlifeMessageDialog setContentWidth(int width) {
        super.setContentWidth(width);
        return this;
    }

    public CarlifeMessageDialog setContentHeight(int height) {
        super.setContentHeight(height);
        return this;
    }

    public CarlifeMessageDialog setFirstBtnEnabled(boolean enabled) {
        super.setFirstBtnEnabled(enabled);
        return this;
    }

    public CarlifeMessageDialog setSecondBtnEnabled(boolean enabled) {
        super.setSecondBtnEnabled(enabled);
        return this;
    }

    public CarlifeMessageDialog enableBackKey(boolean cancelable) {
        super.enableBackKey(cancelable);
        return this;
    }

    public CarlifeMessageDialog setFirstBtnTextColorHighLight() {
        super.setFirstBtnTextColorHighLight();
        return this;
    }

    public CarlifeMessageDialog setSecondBtnTextColorHighLight() {
        super.setSecondBtnTextColorHighLight();
        return this;
    }
}
