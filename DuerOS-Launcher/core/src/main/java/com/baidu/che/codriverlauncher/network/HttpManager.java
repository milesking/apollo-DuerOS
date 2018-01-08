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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baidu.che.codriverlauncher.network.callback.DownloadCallback;
import com.baidu.che.codriverlauncher.network.callback.HttpCallback;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.os.Handler;
import android.os.Looper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * httpmanager, should be called in ui thread
 */
public final class HttpManager {

    private static final String TAG = "network";

    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private CookieJarImpl mCookieJarImpl;

    // single instance start
    private static HttpManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        public static final HttpManager INSTANCE = new HttpManager();
    }

    private HttpManager() {
        LogUtil.i(TAG, "init http manager");
        mCookieJarImpl = new CookieJarImpl();
        //        X509TrustManager trustManager = createTrustManager();
        //        SSLSocketFactory sslSocketFactory = createSSLSocketFactory(trustManager);
        //        HostnameVerifier hostnameVerifier = createHostnameVerifier();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(HttpConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HttpConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HttpConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cookieJar(mCookieJarImpl);
        //        if (trustManager != null && sslSocketFactory != null) {
        //            builder.sslSocketFactory(sslSocketFactory, trustManager)
        //                    .hostnameVerifier(hostnameVerifier);
        //        } else {
        //            LogUtil.e(TAG, "trustManager==null || sslSocketFactory==null");
        //        }
        mOkHttpClient = builder.build();
        mHandler = new Handler(Looper.getMainLooper());
    }
    // single instance end

    public static OkHttpClient getOkHttpClient() {
        return getInstance().mOkHttpClient;
    }

    public static void cancelAllRequests() {
        LogUtil.i(TAG, "cancel all requests");
        getInstance().mOkHttpClient.dispatcher().cancelAll();
    }

    public static void clearCookies() {
        LogUtil.i(TAG, "clear cookies");
        getInstance().mCookieJarImpl.clear();
    }

    public static void get(String url, HttpCallback callback) {
        Request request = HttpManagerUtil.buildGetRequest(url);
        getInstance().execute(request, callback);
    }

    public static void post(String url, Map<String, String> params, HttpCallback callback) {
        Request request = HttpManagerUtil.buildPostRequest(url, params);
        getInstance().execute(request, callback);
    }

    public static void post(String url, String postJson, HttpCallback callback) {
        Request request = HttpManagerUtil.buildPostRequest(url, postJson);
        getInstance().execute(request, callback);
    }

    public static void downloadFile(String url, String destFileDir, long startPosition, DownloadCallback callback) {
        getInstance().download(url, destFileDir, startPosition, callback);
    }

    private void execute(Request request, final HttpCallback callback) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, final Response response) {
                String url = call.request().url().toString();
                if (callback == null) {
                    LogUtil.e(TAG, "HttpCallback is null");
                }
                try {
                    // cookies
                    List<String> cookies = response.headers().values("Set-Cookie");
                    cookieCallback(callback, url, cookies);
                    // response
                    int statusCode = response.code();
                    if (response.isSuccessful()) {  // code >= 200 && code < 300
                        String body = response.body().string();
                        successCallback(callback, url, statusCode, body);
                    } else {
                        errorCallback(callback, url, "statusCode=" + statusCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errorCallback(callback, url, e.toString());
                } finally {
                    response.close();
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                errorCallback(callback, call.request().url().toString(), e.toString());
            }
        });
    }

    private void successCallback(final HttpCallback callback, final String url, final int statusCode, final String
            response) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(url, statusCode, response);
                }
            }
        });
    }

    private void cookieCallback(final HttpCallback callback, final String url, final List<String> cookies) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onCookies(url, cookies);
                }
            }
        });
    }

    private void errorCallback(final HttpCallback callback, final String url, final String errMsg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onError(url, errMsg);
                }
            }
        });
    }

    /**
     * download file
     *
     * @param url              download uri
     * @param destFileDir      storage dir
     * @param startPosition
     * @param downloadCallback
     */
    private void download(final String url, final String destFileDir, final long startPosition,
                          final DownloadCallback downloadCallback) {
        final Request request = HttpManagerUtil.buildDownloadRequest(url, startPosition);
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) {
                String url = call.request().url().toString();
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    String fileName = HttpManagerUtil.getFileNameFromUrl(url);
                    if (fileName == null) {
                        LogUtil.e(TAG, "get file name from url failed! url=" + url);
                        fileName = "temp" + System.currentTimeMillis() + ".bat";
                    }
                    File file = new File(destFileDir, fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    LogUtil.i(TAG, "cacheFile = " + file.getAbsolutePath());
                    downloadStartCallback(downloadCallback, url, file.getAbsolutePath());
                    long contentLength = response.body().contentLength();
                    long downloadLength = 0L;
                    byte[] buffer = new byte[1024 * 8];
                    int len;
                    int index = 0; // index can reduce the callback frequency of loading progress
                    fileOutputStream = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        downloadLength += len;
                        if (index % 20 == 0) {
                            downloadProgressCallback(downloadCallback, url, downloadLength, contentLength);
                        }
                        index++;
                    }
                    fileOutputStream.flush();
                    downloadCompleteCallback(downloadCallback, url, file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    downloadErrorCallback(downloadCallback, url, e.toString());
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (request != null) {
                        try {
                            response.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                e.printStackTrace();
                downloadErrorCallback(downloadCallback, call.request().url().toString(), e.toString());
            }

        });
    }

    private void downloadCompleteCallback(final DownloadCallback downloadCallback, final String url,
                                          final String filePath) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (downloadCallback != null) {
                    downloadCallback.onDownloadComplete(url, filePath);
                }
            }
        });
    }

    private void downloadStartCallback(final DownloadCallback downloadCallback, final String url,
                                       final String filePath) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (downloadCallback != null) {
                    downloadCallback.onDownloadStart(url, filePath);
                }
            }
        });
    }

    private void downloadProgressCallback(final DownloadCallback downloadCallback, final String url,
                                          final long downloadSize,
                                          final long size) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (downloadCallback != null) {
                    downloadCallback.onDownloadProgress(url, downloadSize, size);
                }
            }
        });
    }

    private void downloadErrorCallback(final DownloadCallback downloadCallback, final String url, final String errMsg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (downloadCallback != null) {
                    downloadCallback.onDownloadError(url, errMsg);
                }
            }
        });
    }
}
