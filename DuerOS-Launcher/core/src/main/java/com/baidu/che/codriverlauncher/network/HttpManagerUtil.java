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
package com.baidu.che.codriverlauncher.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.che.codriverlauncher.util.LogUtil;

import android.text.TextUtils;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * http manager util
 */
final class HttpManagerUtil {

    private static final String TAG = "network";

    private static Request.Builder requestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("User-Agent", HttpConfig.DEFAULT_USER_AGENT);
    }

    static Request buildGetRequest(String url) {
        LogUtil.i(TAG, "GET URL: " + url);
        return requestBuilder(url).build();
    }

    static Request buildDownloadRequest(String url, long startPosition) {
        LogUtil.i(TAG, "DOWNLOAD URL: " + url + " , startPosition=" + startPosition);
        return requestBuilder(url).addHeader("RANGE", "bytes=" + startPosition + "-").build();
    }

    static Request buildPostRequest(String url, String postJson) {
        LogUtil.i(TAG, "POST URL: " + url);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, postJson);
        return requestBuilder(url).post(requestBody).build();
    }

    static Request buildPostRequest(String url, Map<String, String> params) {
        LogUtil.i(TAG, "POST URL: " + url);
        // build post body
        if (params == null) {
            params = new HashMap<String, String>();
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            LogUtil.i(TAG, "POST PARAM: " + entry.getKey() + "=" + entry.getValue());
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = formBodyBuilder.build();

        return requestBuilder(url).post(requestBody).build();
    }

    static String getFileNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        url = url.split("\\?")[0];
        Pattern pattern = Pattern.compile("^.*/(.*)$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
