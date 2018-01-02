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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;
import com.baidu.carlifevehicle.util.CarlifeUtil;
import com.baidu.carlifevehicle.util.DigitalTrans;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

public class ConnectClient {

    private static final String TAG = "ConnectClient";
    private static final String CONNECT_CLIENT_HANDLER_THREAD_NAME = "ConnectClientHandlerThread";

    private Context mContext = null;
    private ConnectServiceReceiver mConnectServiceReceiver = null;
    private UsbConnectStateReceiver mUsbConnectStateReceiver = null;
    private AOAAccessoryReceiver mUsbAccessoryReceiver = null;

    private ConnectClientHandler mConnectClientHandler = null;

    private Messenger mConnectService = null;
    private Messenger mConnectClient = null;

    private boolean isUsbConnected = false;
    private boolean isConnecting = false;
    private boolean isConnected = false;
    private boolean isBound = false;

    private static ConnectClient mInstance = null;

    private class ConnectClientHandler extends Handler {
        public ConnectClientHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (null == msg) {
                return;
            }
            switch (msg.what) {
                case CommonParams.MSG_USB_STATE_MSG:
                    if (msg.arg1 == CommonParams.MSG_USB_STATE_MSG_ON) {
                        isUsbConnected = true;
                        LogUtil.e(TAG, "USB Cable is connected!");
                    } else if (msg.arg1 == CommonParams.MSG_USB_STATE_MSG_OFF) {
                        isUsbConnected = false;
                        LogUtil.e(TAG, "USB Cable is disconnected!");
                    }
                    break;
                case CommonParams.MSG_CONNECT_SERVICE_MSG:
                    if (msg.arg1 == CommonParams.MSG_CONNECT_SERVICE_MSG_START) {
                        bindConnectService();
                    } else if (msg.arg1 == CommonParams.MSG_CONNECT_SERVICE_MSG_STOP) {
                        unbindConnectService();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            LogUtil.d(TAG, "onServiceConnected");
            isBound = true;

            mConnectService = new Messenger(service);

            Message msg = Message.obtain(null, CommonParams.MSG_REC_REGISTER_CLIENT);
            sendMsgToService(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            LogUtil.d(TAG, "onServiceDisconnected");
            isBound = false;
            mConnectService = null;
        }
    };

    public static ConnectClient getInstance() {
        if (null == mInstance) {
            synchronized (ConnectClient.class) {
                if (null == mInstance) {
                    mInstance = new ConnectClient();
                }
            }
        }
        return mInstance;
    }

    private ConnectClient() {

    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");

        mContext = context;
        HandlerThread handlerThread = new HandlerThread(CONNECT_CLIENT_HANDLER_THREAD_NAME);
        handlerThread.start();
        mConnectClientHandler = new ConnectClientHandler(CarlifeUtil.getLooper(handlerThread));
        mConnectClient = new Messenger(mConnectClientHandler);

        mConnectServiceReceiver = new ConnectServiceReceiver(context, mConnectClientHandler);
        mUsbConnectStateReceiver = new UsbConnectStateReceiver(context, mConnectClientHandler);
        try {
            registerConnectServiceReceiver();
            registerUsbConnectStateReceiver();
            bindConnectService();
        } catch (Exception e) {
            LogUtil.e(TAG, "UsbConnectStateManager init fail");
            e.printStackTrace();
        }
    }

    public void uninit() {
        LogUtil.d(TAG, "uninit");
        try {
            unregisterConnectServiceReceiver();
            unregisterUsbConnectStateReceiver();
            unbindConnectService();
        } catch (Exception e) {
            LogUtil.e(TAG, "UsbConnectStateManager uninit fail");
            e.printStackTrace();
        }
    }

    private void startConnectService() {
        LogUtil.d(TAG, "start ConnectService");
        Intent startIntent = new Intent(mContext, ConnectService.class);
        mContext.startService(startIntent);
    }

    private void stopConnectService() {
        LogUtil.d(TAG, "stop ConnectService");
        Intent stopIntent = new Intent(mContext, ConnectService.class);
        mContext.stopService(stopIntent);
    }

    private void bindConnectService() {
        LogUtil.d(TAG, "bind ConnectService");
        Intent bindIntent = new Intent(mContext, ConnectService.class);
        mContext.bindService(bindIntent, mConnection, android.content.Context.BIND_AUTO_CREATE);
    }

    private void unbindConnectService() {
        LogUtil.d(TAG, "unbind ConnectService");
        mContext.unbindService(mConnection);

        Message msg = Message.obtain(null, CommonParams.MSG_REC_UNREGISTER_CLIENT);
        sendMsgToService(msg);
    }

    private void registerConnectServiceReceiver() {
        if (null != mConnectServiceReceiver) {
            mConnectServiceReceiver.registerReceiver();
            LogUtil.d(TAG, "register ConnectServiceReceiver");
        }
    }

    private void registerUsbConnectStateReceiver() {
        if (null != mUsbConnectStateReceiver) {
            mUsbConnectStateReceiver.registerReceiver();
            LogUtil.d(TAG, "register UsbConnectStateReceiver");
        }
    }


    private void unregisterConnectServiceReceiver() {
        if (null != mConnectServiceReceiver) {
            mConnectServiceReceiver.unregisterReceiver();
            LogUtil.d(TAG, "unregister ConnectServiceReceiver");
        }
    }

    private void unregisterUsbConnectStateReceiver() {
        if (null != mUsbConnectStateReceiver) {
            mUsbConnectStateReceiver.unregisterReceiver();
            LogUtil.d(TAG, "unregister UsbConnectStateReceiver");
        }
    }

    private void unregisterUsbAccessoryReceiver() {
        if (null != mUsbAccessoryReceiver) {
            mUsbAccessoryReceiver.unregisterReceiver();
            LogUtil.d(TAG, "unregister UsbAccessoryReceiver");
        }
    }


    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean sendMsgToService(Message msg) {
        LogUtil.d(TAG, "Send Msg to Service, what = 0x" + DigitalTrans.algorismToHEXString(msg.what, 8));
        if (mConnectService == null) {
            LogUtil.e(TAG, "mConnectService is null");
            return false;
        }

        if (mConnectClient == null) {
            LogUtil.e(TAG, "mConnectClient is null");
            return false;
        }

        try {
            msg.replyTo = mConnectClient;
            mConnectService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public synchronized void setIsConnecting(boolean is) {
        if (!isConnecting && is) {
            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_CONNECTING);
        }
        isConnecting = is;
    }

    public synchronized void setIsConnected(boolean is) {
        if (isConnected && !is) {
            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_DISCONNECTED);
        } else if (!isConnected && is) {
            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_CHANGE_PROGRESS_NUMBER, 100,
                    0, null);
            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_CONNECT_STATUS_CONNECTED);
        }
        isConnected = is;
    }

    public boolean isCarlifeConnecting() {
        return isConnecting;
    }

    public boolean isCarlifeConnected() {
        return isConnected;
    }

}
