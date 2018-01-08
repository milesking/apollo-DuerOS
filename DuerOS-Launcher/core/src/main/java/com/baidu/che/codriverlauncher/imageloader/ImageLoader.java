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
package com.baidu.che.codriverlauncher.imageloader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import android.content.Context;
import android.widget.ImageView;

/*
 * Glide wrapper class , Used to load round and ordinary pictures
 */

public final class ImageLoader {

    public static void load(Context context, ImageView imageView, String url) {
        load(context, imageView, url, 0);
    }

    public static void load(Context context, ImageView imageView, String url, int placeHolder) {
        // options
        RequestOptions options = new RequestOptions()
                .placeholder(placeHolder)
                .error(placeHolder)
                .dontTransform();
        // load
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    public static void loadCircle(Context context, ImageView imageView, String url) {
        loadCircle(context, imageView, url, 0);

    }

    public static void loadCircle(Context context, ImageView imageView, String url, int placeHolder) {
        // options
        RequestOptions options = new RequestOptions()
                .placeholder(placeHolder)
                .error(placeHolder)
                .bitmapTransform(new CircleCrop());
        // load
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

}
