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
package com.baidu.carlifevehicle.connect;

import java.util.LinkedList;
import java.util.List;

import com.baidu.carlifevehicle.util.LogUtil;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

/**
 *
 * The format of ConnectService declaration in AndroidManifest.xml
 * <service android:name="com.baidu.carlifevehicle.connect.ConnectService">
 *          <intent-filter>
 *              <action android:name="com.baidu.carlifevehicle.connect.ConnectService" />
 *          </intent-filter>
 *          <meta-data android:name="verCode" android:value="1"/>
 * </service>
 *
 */
public class ConnectService extends Service {

    public static final int MSG_SEND_DISCARD = -1;

    private static final String TAG = "ConnectService";
    private static final int SERVICE_CACHED_MSG_LIMIT = 100;

    private ConnectServiceProxy mConnectServiceProxy = null;
    private Handler mConnectServiceProxyHandler = null;
    private final ConnectServiceHandler mConnectServiceHandler = new ConnectServiceHandler();

    private final Messenger mMessenger = new Messenger(mConnectServiceHandler);
    private List<Message> mCachedMessage = new LinkedList<Message>();

    private ConnectManager mConnectManager = null;

    private class ConnectServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mConnectServiceProxyHandler != null) {
                mConnectServiceProxyHandler.handleMessage(msg);

                if (mCachedMessage.size() > 0) {
                    Message cachedMsg = mCachedMessage.remove(0);
                    mConnectServiceHandler.sendMessage(cachedMsg);
                }
            } else {
                if (mCachedMessage.size() >= SERVICE_CACHED_MSG_LIMIT) {
                    Message oldMsg = mCachedMessage.remove(0);
                    Message replayMsg = Message.obtain(null, MSG_SEND_DISCARD, oldMsg);
                    try {
                        LogUtil.e(
                                TAG,
                                "Send MSG_SEND_DISCARD, oldMsg what = "
                                        + Integer.toString(oldMsg.what));
                        oldMsg.replyTo.send(replayMsg);
                    } catch (Throwable t) {
                        LogUtil.e(TAG, "Send MSG_SEND_DISCARD Error");
                        t.printStackTrace();
                    }
                }
                Message tmp = Message.obtain(msg);
                mCachedMessage.add(tmp);

                createConnectService();
            }
        }
    }

    private void createConnectService() {
        try {
            if (mConnectServiceProxy == null || mConnectServiceProxyHandler == null) {
                mConnectServiceProxy = new ConnectServiceProxy(this);
                mConnectServiceProxyHandler = mConnectServiceProxy.getHandler();
            }
            if (mCachedMessage.size() > 0) {
                Message cachedMsg = mCachedMessage.remove(0);
                mConnectServiceHandler.sendMessage(cachedMsg);
            }

            mConnectManager = ConnectManager.getInstance();
            mConnectManager.startConnectThread();
        } catch (Throwable t) {
            mConnectServiceProxy = null;
            mConnectServiceProxyHandler = null;
            t.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "ConnectService onBind()");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "ConnectService onUnbind()");
        return super.onUnbind(intent);
    }

    public void onRebind(Intent intent) {
        LogUtil.d(TAG, "ConnectService onRebind()");
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "ConnectService onCreate()");
        super.onCreate();
        createConnectService();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        LogUtil.d(TAG, "ConnectService onStart(), startId = " + startId);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "ConnectService onDestroy()");
        super.onDestroy();
    }
}
