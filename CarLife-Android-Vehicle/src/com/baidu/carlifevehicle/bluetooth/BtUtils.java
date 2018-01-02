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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.util.Log;

/**
 * Utils for Bluetooth pair
 * 
 * @author liaoruihua
 * 
 */

public class BtUtils {
    private static final String TAG = "BluetoothDevice";

    public static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";

    public static final String EXTRA_PAIRING_KEY = "android.bluetooth.device.extra.PAIRING_KEY";

    public static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";

    public static final String ACTION_BOND_STATE_CHANGED = "android.bluetooth.device.action.BOND_STATE_CHANGED";

    public static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";

    /**
     * The user will be prompted to enter a pin
     */
    public static final int PAIRING_VARIANT_PIN = 0;

    /**
     * The user will be prompted to enter a passkey
     */
    public static final int PAIRING_VARIANT_PASSKEY = 1;

    /**
     * The user will be prompted to confirm the passkey displayed on the screen
     */
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;


    public static String getBtAddress() {
        String btaddr = "";
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            btaddr = btAdapter.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(btaddr)) {
                btaddr = "";
            }
        }

        return btaddr;
    }

    /**
     * Start the bonding (pairing) process with the remote device.
     * <p>
     * This is an asynchronous call, it will return immediately. Register for
     * {@link #ACTION_BOND_STATE_CHANGED} intents to be notified when the
     * bonding process completes, and its result.
     * <p>
     * Android system services will handle the necessary user interactions to
     * confirm and complete the bonding process.
     * <p>
     * Requires {@link android.Manifest.permission#BLUETOOTH_ADMIN}.
     *
     * @return false on immediate error, true if bonding will begin
     */
    @SuppressLint("NewApi")
    public static boolean createBond(Class<?> btClass, BluetoothDevice btDevice) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return btDevice.createBond();
        } else {
            Method createBondMethod = btClass.getDeclaredMethod("createBond");
            Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        }

    }

    /**
     * a wrapper method for {@link BluetoothDevice#setPin(byte[])}
     * @param btClass if the platform is lower than 19, btClass is used to reflect BluetoothDevice to invoke setPin()
     * @param btDevice if the platform is 19 or higher, btDevice is used to invoke setPin()
     * @param pin the pin to be set
     * @return true pin has been set
     *         false for error
     * @throws Exception
     */
    @SuppressLint("NewApi")
    public static boolean setPin(Class<?> btClass, BluetoothDevice btDevice, String pin)
            throws Exception {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return btDevice.setPin( convertPinToBytes(pin));
            } else {
                Method setPinMethod = btClass.getDeclaredMethod("setPin", new Class[] {byte[].class});
                Boolean returnValue = (Boolean) setPinMethod.invoke(btDevice, convertPinToBytes(pin));
                return returnValue.booleanValue();
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * a wrapper method for {@link BluetoothDevice#setPairingConfirmation(boolean)}
     * @param btClass if the platform is lower than 19, btClass is used to reflect BluetoothDevice to invoke setPairingConfirmation()
     * @param btDevice if the platform is 19 or higher, btDevice is used to invoke setPairingConfirmation()
     * @param confirm whether to confirm passkey
     * @return true confirmation has been sent out
     *         false for error
     * @throws Exception
     */
    @SuppressLint("NewApi")
    public static boolean setPairingConfirmation(Class<?> btClass, BluetoothDevice btDevice,
            boolean confirm) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return btDevice.setPairingConfirmation(confirm);
        } else {
            Method setPairingConfirmationMethod = btClass.getDeclaredMethod("setPairingConfirmation",
                    new Class[] {boolean.class});
            Boolean returnValue = (Boolean) setPairingConfirmationMethod.invoke(btDevice, confirm);
            return returnValue.booleanValue();
        }
    }

    /**
     * a reflection invocation wrapper for BluetoothDevice.cancelPairingUserInput()
     * @param btClass BluetoothDevice.class, used for reflection
     * @param btDevice the BluetoothDevice instance
     * @return true for cancel success
     *         false for error
     * @throws Exception
     */
    public static boolean cancelPairingUserInput(Class<?> btClass,
            BluetoothDevice btDevice) throws Exception {
        Method cancelPairingUserInputMethod = btClass
                .getDeclaredMethod("cancelPairingUserInput");
        Boolean returnValue = (Boolean) cancelPairingUserInputMethod
                .invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * a reflection invocation wrapper for BluetoothDevice.disconnect(BluetoothProfile)
     * @param btClass BluetoothDevice.class, used for reflection
     * @param proxy BluetoothProfile parameter
     * @param btDevice the BluetoothDevice instance
     * @return true for disconnect success
     *         false for error
     * @throws Exception
     */
    public static boolean disconnectBT(Class btClass, BluetoothProfile proxy,
            BluetoothDevice btDevice) throws Exception {
        Method disconnectMethod = btClass.getDeclaredMethod("disconnect",
                BluetoothDevice.class);
        disconnectMethod.setAccessible(true);
        Boolean returnValue = (Boolean) disconnectMethod
                .invoke(proxy, btDevice);
        return returnValue.booleanValue();
    }

    /**
     * a reflection invocation wrapper for BluetoothDevice.connect(BluetoothProfile)
     * @param btClass BluetoothDevice.class, used for reflection
     * @param proxy BluetoothProfile parameter
     * @param btDevice the BluetoothDevice instance
     * @return true for connect success
     *         false for error
     * @throws Exception
     */
    public boolean connectBT(Class btClass, BluetoothProfile proxy,
            BluetoothDevice btDevice) throws Exception {
        Method connectMethod = btClass.getDeclaredMethod("connect",
                BluetoothDevice.class);
        connectMethod.setAccessible(true);
        Boolean returnValue = (Boolean) connectMethod.invoke(proxy, btDevice);
        return returnValue.booleanValue();
    }

    /**
     * UUID for serial service
     */
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * Check that a pin is valid and convert to byte array.
     *
     * Bluetooth pin's are 1 to 16 bytes of UTF-8 characters.
     *
     * @param pin
     *            pin as java String
     * @return the pin code as a UTF-8 byte array, or null if it is an invalid
     *         Bluetooth pin.
     */
    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "UTF-8 not supported?!?");
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }
}
