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
package com.baidu.carlifevehicle.bluetooth;

import java.util.List;
import java.util.Set;

import com.baidu.carlife.protobuf.CarlifeBTPairInfoProto.CarlifeBTPairInfo;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class BtManager {
    private static final String TAG = "MyBluetoothManager";
    private static BtManager mInstance = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mRemoteBTAddress = null;
    private Context mContext = null;
    private int checkConnectingTime = 0;
    public static final int BLUETOOTH_READY = 1;
    public static final int BLUETOOTH_IS_CONNECTING = 2;
    public static final int BLUETOOTH_STATUS_IDLE = 0;
    public static final int BLUETOOTH_STATUS_READY_TO_PAIR = 1;
    public static final int BLUETOOTH_STATUS_ALREADY_CONNECTED = 2;
    public static final String PACKAGENAME = "com.baidu.carlifevehicle";
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BLUETOOTH_READY:
                    CarlifeBTPairInfo buildBluetoothInfo =
                            BtManager.getInstance().buildBluetoothInfo(BLUETOOTH_STATUS_READY_TO_PAIR);

                    if (buildBluetoothInfo != null) {
                        LogUtil.i(TAG, "send status=" + buildBluetoothInfo.getStatus() + "to mobile device to start "
                                + "connect");
                        BtManager.getInstance().sendBluetoothInfoToMd(buildBluetoothInfo);
                    }
                    break;
                case BLUETOOTH_IS_CONNECTING:
                    if (checkConnectingTime < 10) {
                        prepareToPairMd(mRemoteBTAddress);
                        checkConnectingTime++;
                        return;
                    }
                    checkConnectingTime = 0;
                    break;
                default:
                    break;
            }
        }

    };

    private BtManager() {

    }

    public static BtManager getInstance() {
        if (null == mInstance) {
            synchronized (BtManager.class) {
                if (null == mInstance) {
                    mInstance = new BtManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
        registerReceiver();
        if (!checkBTAvailable()) {
            boolean isOpen = openBluetooth();
            LogUtil.i(TAG, "isOpen=" + isOpen);
        }
    }

    public void prepareToPairMd(String remoteBTAddress) {
        this.mRemoteBTAddress = remoteBTAddress;
        if (!checkBTAvailable()) {
            openBluetooth();
        }
        int connectionState = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        LogUtil.i(TAG, "connectionState=" + connectionState);
        switch (connectionState) {
            case BluetoothProfile.STATE_DISCONNECTED:
                Message msg = new Message();
                msg.what = BLUETOOTH_READY;
                mHandler.sendMessage(msg);
                break;
            case BluetoothProfile.STATE_CONNECTED:
                break;
            case BluetoothProfile.STATE_CONNECTING:
            case BluetoothProfile.STATE_DISCONNECTING:
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = BLUETOOTH_IS_CONNECTING;
                        mHandler.sendMessage(msg);
                    }
                }, 2000);
                break;

            default:
                break;
        }
        mBluetoothAdapter.getProfileProxy(mContext, btProfileListener, BluetoothProfile.HEADSET);
    }

    public boolean checkIsPaired(String remoteBTAddress) {
        boolean isPaired = false;
        LogUtil.i(TAG, "check is paired");
        if (mBluetoothAdapter == null) {
            return false;
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device == null) {
                    continue;
                }
                String address = device.getAddress();
                if (!TextUtils.isEmpty(address) && address.equals(remoteBTAddress)) {
                    isPaired = true;
                    break;
                }

            }
        }

        return isPaired;
    }

    public boolean checkBTAvailable() {
        LogUtil.i(TAG, "checkBTAvailable");
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    public boolean openBluetooth() {
        LogUtil.i(TAG, "openBluetooth");
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.enable();
        }
        return false;
    }

    public boolean closeBluetooth() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.disable();
        }
        return false;
    }

    public void sendBluetoothInfoToMd(CarlifeBTPairInfo carlifeBTPairInfo) {
        if (carlifeBTPairInfo == null) {
            return;
        }
        CarlifeCmdMessage btCommand = new CarlifeCmdMessage(true);
        btCommand.setServiceType(CommonParams.MSG_CMD_HU_BT_OOB_INFO);
        btCommand.setData(carlifeBTPairInfo.toByteArray());
        btCommand.setLength(carlifeBTPairInfo.getSerializedSize());
        Message msgBt =
                Message.obtain(null, btCommand.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, btCommand);
        ConnectClient.getInstance().sendMsgToService(msgBt);

    }

    private CarlifeBTPairInfo buildBluetoothInfo(int status) {
        if (mBluetoothAdapter == null) {
            return null;
        }
        CarlifeBTPairInfo.Builder builder = CarlifeBTPairInfo.newBuilder();
        if (builder == null) {
            return null;
        }
        String address = mBluetoothAdapter.getAddress();
        if (!TextUtils.isEmpty(address)) {
            builder.setAddress(address);
        } else {
            builder.setAddress("");
        }
        String name = mBluetoothAdapter.getName();
        if (!TextUtils.isEmpty(name)) {
            builder.setName(name);
        } else {
            builder.setName("");
        }
        LogUtil.i(TAG, "address=" + address + ",name=" + name);
        builder.setStatus(status);
        builder.setUuid("00001101-0000-1000-8000-00805F9B34FB");
        builder.setPassKey("1234");
        builder.setRandomizer("1234");
        CarlifeBTPairInfo bluetoothInfo = builder.build();
        return bluetoothInfo;
    }

    public void uninit() {
        unRegisterReceiver();
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BtUtils.ACTION_PAIRING_REQUEST);
        filter.addAction(BtUtils.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (mContext != null) {
            mContext.registerReceiver(mBluetoothReceiver, filter);
        }
    }

    private void unRegisterReceiver() {
        if (mContext != null && mBluetoothReceiver != null) {
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i(TAG, "action=" + action);
            if (BtUtils.ACTION_PAIRING_REQUEST.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BtUtils.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                final String address = device.getAddress();
                switch (type) {
                    case BtUtils.PAIRING_VARIANT_PIN:
                        if (address != null && address.equals(mRemoteBTAddress)) {
                            mHandler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        BtUtils.setPin(device.getClass(), device, "0000");
                                        BtUtils.cancelPairingUserInput(device.getClass(), device);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1000);

                        }
                        break;
                    case BtUtils.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (address != null && address.equals(mRemoteBTAddress)) {
                                    try {
                                        boolean isConfirmed =
                                                BtUtils.setPairingConfirmation(device.getClass(), device, true);
                                        LogUtil.i(TAG, "isConfirmed=" + isConfirmed);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, 300);
                        break;
                    default:
                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.BOND_NONE);
                LogUtil.i(TAG, "ACTION_BOND_STATE_CHANGED*********" + device.getAddress() + ",state" + type);
                String address = device.getAddress();
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        LogUtil.d(TAG, "bonding......");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        LogUtil.d(TAG, "bonded");
                        break;
                    case BluetoothDevice.BOND_NONE:
                    default:
                        break;
                }
            }
        }
    };

    private BluetoothProfile.ServiceListener btProfileListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceDisconnected(int profile) {
            LogUtil.i(TAG, "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            LogUtil.i(TAG, "onServiceConnected");
            if (profile == BluetoothProfile.HEADSET) {
                List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();
                for (BluetoothDevice bluetoothDevice : connectedDevices) {
                    if (bluetoothDevice == null) {
                        continue;
                    }
                    String address = bluetoothDevice.getAddress();
                    if (address != null && address.equals(mRemoteBTAddress)) {
                        CarlifeBTPairInfo buildBluetoothInfo =
                                BtManager.getInstance().buildBluetoothInfo(BLUETOOTH_STATUS_ALREADY_CONNECTED);
                        if (buildBluetoothInfo != null) {
                            BtManager.getInstance().sendBluetoothInfoToMd(buildBluetoothInfo);
                        }
                    }

                }
            }

        }
    };
}
