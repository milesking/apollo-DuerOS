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

import android.bluetooth.BluetoothDevice;

/**
 * Interface definition for Bluetooth connection.
 */
interface BtInterfaceBase {
    /**
     *
     * @param cb {@link BtCallback}
     */
    void init(BtCallback cb);

    /**
     *
     * @param cb {@link BtCallback}
     */
    void uninit(BtCallback cb);

    @Deprecated
    String getAddress();
    @Deprecated
    String getName();
    @Deprecated
    String getPincode();

    /**
     *
     * @return true for enabled, false for disabled
     */
    boolean isEnable();

    /**
     *
     * @return true for enabled, false for error
     */
    boolean enable();

    /**
     *
     * @return ture for disabled, false for error
     */
    boolean disable();

    /**
     *
     * @return the {@link android.bluetooth.BluetoothProfile} connect state, can be one of follows:<br/>
     *         {@link android.bluetooth.BluetoothProfile#STATE_CONNECTED}<br/>
     *         {@link android.bluetooth.BluetoothProfile#STATE_CONNECTING}<br/>
     *         {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTED}<br/>
     *         {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTING}
     */
    int getHfpConnectionState();

    /**
     * get a list of hardware addresses of connected devices
     * @return a list of hardware addresses as string
     */
    String getConnectedDeviceAddress();

    /**
     *
     * @return true for disconnected, false for error
     */
    boolean disconnect();

    /**
     * get a list of hardware addresses of bonded devices
     * @return a list of hardware addresses as string
     */
    String getBondedDevicesAddress();

    /**
     *
     * @param pin pin code
     * @return true for success, false for error
     */
    boolean setPin(String pin);

    /**
     * a wrapper for {@link BtUtils#setPairingConfirmation(Class, BluetoothDevice, boolean)}
     * @param accept not used
     * @param device {@link BluetoothDevice}
     * @return true for pair confirmed, false for error
     */
    boolean setPairingConfirmation(boolean accept, BluetoothDevice device);


    /**
     * Interface definition for a callback to be invoked when bluetooth pair state updated
     */
    interface BtCallback {
        /**
         * Called to notify the client when the proxy object has been
         * connected to the service.
         * @param started true for success, false for fail
         */
        void onServiceStart(boolean started);

        /**
         * Called when receive {@link BluetoothDevice#EXTRA_PAIRING_VARIANT}
         * @param mode pairing method used. It can be one of followings:<br/>
         *             {@link BtUtils#PAIRING_VARIANT_PIN}<br/>
         *             {@link BtUtils#PAIRING_VARIANT_PASSKEY}<br/>
         *             {@link BtUtils#PAIRING_VARIANT_PASSKEY_CONFIRMATION}
         * @param device {@link android.bluetooth.BluetoothDevice}
         */
        void onReceivedPairingRequest(int mode, final BluetoothDevice device);

        /**
         * Called when receive {@link BluetoothDevice#ACTION_BOND_STATE_CHANGED}
         * @param state {@link BluetoothDevice#EXTRA_BOND_STATE}
         * @param device {@link BluetoothDevice}
         */
        void onReceivedBondStateChange(int state, final BluetoothDevice device);

        /**
         * Called when receive {@link BluetoothDevice#ACTION_BOND_STATE_CHANGED}
         * @param state {@link BluetoothDevice#EXTRA_BOND_STATE}
         * @param device {@link BluetoothDevice}
         */
        void onReceivedHfpConnectionStateChanged(int state, final BluetoothDevice device);
    }
    
}