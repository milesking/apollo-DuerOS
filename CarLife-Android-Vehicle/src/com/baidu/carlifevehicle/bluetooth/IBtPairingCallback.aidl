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

interface IBtPairingCallback {
    //mode could be following value:
	//	public static final int PAIRING_VARIANT_PIN = 0;
	//	public static final int PAIRING_VARIANT_PASSKEY = 1;
	//	public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
	//address is for remote paired device
    void onPairingRequest(in int mode, in String address);
    
    //state could be following value:
    //public static final int BOND_NONE = 10;
    //public static final int BOND_BONDING = 11; Indicates bonding (pairing) is in progress with the remote device
    //public static final int BOND_BONDED = 12;Indicates the remote device is bonded (paired)
    void onBondStateChange(in int state);
    
    //state could be below value:
    //public static final int STATE_DISCONNECTED  = 0;
    //public static final int STATE_CONNECTING    = 1;
    //public static final int STATE_CONNECTED     = 2;
    //public static final int STATE_DISCONNECTING = 3;
    void onHfpConnectionStateChanged(in int state);
}