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

import com.baidu.carlifevehicle.util.LogUtil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;


public class BtNativeInterface implements BtInterfaceBase {
    private static final String TAG = BtNativeInterface.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothHeadset mBluetoothHeadset = null;
    private Context mContext = null;
    private BtInterfaceBase.BtCallback mBtCallback = null;
    
    public BtNativeInterface(Context cx) {
        mContext = cx;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BtUtils.ACTION_PAIRING_REQUEST);
        filter.addAction(BtUtils.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        if (mContext != null) {
            mContext.registerReceiver(mBluetoothReceiver, filter);
        }
    }
    
    private void unregisterReceiver() {
        if (mContext != null && mBluetoothAdapter != null) {
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
    }
    
    private void getHeadsetProxy() {
        if (mBluetoothAdapter != null) {
            try {
                boolean ret = mBluetoothAdapter.getProfileProxy(mContext, new BluetoothProfile.ServiceListener() {
                    @Override
                    public void onServiceDisconnected(int profile) {
                        mBluetoothHeadset = null;
                        if (mBtCallback != null) {
                            mBtCallback.onServiceStart(false);
                        }
                        LogUtil.d(TAG, "Disconnect headset proxy!!!");
                    }

                    @Override
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {
                        if (profile == BluetoothProfile.HEADSET && proxy != null) {
                            mBluetoothHeadset = (BluetoothHeadset) proxy;
                            if (mBtCallback != null) {
                                mBtCallback.onServiceStart(true);
                            }
                            LogUtil.d(TAG, "Get headset proxy: " + mBluetoothHeadset);
                        }
                    }
                }, BluetoothProfile.HEADSET);
                LogUtil.d(TAG, "getProfileProxy ret = " + ret);
            } catch (Exception e) {
                e.printStackTrace();
                if (mBtCallback != null) {
                    mBtCallback.onServiceStart(false);
                }
                LogUtil.d(TAG, "getProfileProxy Exception");
            }
            
        }
    }
    
    
    private boolean isPaired(String address) {
        String pairedListString = getBondedDevicesAddress();
        if (pairedListString != "" && pairedListString.contains(address)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void init(BtInterfaceBase.BtCallback callback) {
        mBtCallback = callback;
        registerReceiver();
        getHeadsetProxy();
        
    }

    @Override
    public void uninit(BtInterfaceBase.BtCallback callback) {
        
        unregisterReceiver();
        if (mBluetoothHeadset != null && mBluetoothAdapter != null) {
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            LogUtil.d(TAG, "Close headset proxy");
        }
        mBtCallback = null;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getPincode() {
        return null;
    }

    @Override
    public boolean isEnable() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        }
        
        return false;
    }

    @Override
    public boolean enable() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.enable();
        }
        
        return false;
    }

    @Override
    public boolean disable() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.disable();
        }
        return false;
    }

    @Override
    public int getHfpConnectionState() {
        int connState = BluetoothProfile.STATE_DISCONNECTED;
        
        if (mBluetoothAdapter != null) {
            connState = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
            return connState;
        }
        
        return connState;
    }

    @Override
    public String getConnectedDeviceAddress() {
        String addressListString = "";
        if (mBluetoothHeadset != null) {
            List<BluetoothDevice> connectedDevices = mBluetoothHeadset.getConnectedDevices();
            for (BluetoothDevice device : connectedDevices) {
                if (device == null) {
                    continue;
                }
                
                String address = device.getAddress();
                if (!TextUtils.isEmpty(address)) {
                    addressListString += address + '#';
                }
            }
        }
        return addressListString;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

    
    @Override
    public String getBondedDevicesAddress() {
        String pairedListString = "";
        if (mBluetoothAdapter == null) {
            return pairedListString;
        }
        Set<BluetoothDevice> pairedSet = mBluetoothAdapter.getBondedDevices();
        if ((pairedSet != null) && pairedSet.size() > 0) {
            for (BluetoothDevice device : pairedSet) {
                if (device == null) {
                    continue;
                }
                
                String address = device.getAddress();
                if (!TextUtils.isEmpty(address)) {
                    pairedListString += address + '#';
                }
            }
        }
        
        return pairedListString;
    }

    @Override
    public boolean setPin(String pin) {
        
        return false;
    }

    @Override
    public boolean setPairingConfirmation(boolean accept, BluetoothDevice device) {
        
        try {
            boolean isConfirmed = BtUtils.setPairingConfirmation(mBluetoothHeadset.getClass(), device, true);
            LogUtil.i(TAG, "isConfirmed=" + isConfirmed);
            return isConfirmed;
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return false;
    }
    
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "Bluetooth Broadcase receiver: action = " + action);
            if (BtUtils.ACTION_PAIRING_REQUEST.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BtUtils.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                final String address = device.getAddress();
                if (mBtCallback != null) {
                    mBtCallback.onReceivedPairingRequest(type, device);
                }
                

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                BluetoothDevice bondDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String addr = bondDevice.getAddress();
                if (mBtCallback != null) {
                    mBtCallback.onReceivedBondStateChange(state, bondDevice);
                } 

            } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                String addr = device.getAddress();
                LogUtil.d(TAG, "BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: remote addr = " + addr + "state = "
                        + state);
                if (mBtCallback != null) {
                    mBtCallback.onReceivedHfpConnectionStateChanged(state, device);
                } 
            }

        }

    };
    
}
