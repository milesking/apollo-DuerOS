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
import com.baidu.carlifevehicle.bluetooth.IBtPairingCallback;
interface IBtPairing {
    boolean registerHfpCallback(IBtPairingCallback callback);
    boolean unregisterHfpCallback(IBtPairingCallback callback);
    //Retrieving the version for AIDL interface, start from 0.1
	String getVersion();
	
	//Retrieving the blueooth address for local adapter
	String getAddress();
	
	//Retrieving the name for local bluetooth
	String getName();
	
	//Retrieving the pin code 
	String getPincode();
	
	//Return TRUE if bluetooth adapter is ready for user currently 
	boolean isEnabled();
	
	//Turning on local bluetooth adapter
	boolean enable();
	
	//Turning off local bluetooth adapter
	boolean disable();
	
	//Get the current connection state of a profile. This function can be used to check whether 
	//the local Bluetooth adapter is connected to any remote device for HFP profile
	//Return value :
	//public static final int STATE_DISCONNECTED  = 0;
    //public static final int STATE_CONNECTING    = 1;
    //public static final int STATE_CONNECTED     = 2;
    //public static final int STATE_DISCONNECTING = 3;
	int getHfpConnectionState();
	
	//Get the bluetooth address for current connected device in terms of HFP profile
	String getConnectedDeviceAddress();
	
	//Disconnect with remote device in case of connecting with other device rather other MD
	//bluetooth adapter should get rid of its current connection
	boolean disconnect();
	//Return the set of Bluetooth device address that are bonded (paired) to the local adapter
	String getBondedDevicesAddress();
	// Set pin during pairing when pairing method is PIN CODE mode
	// This method is not required when Head Unit take the charge for typing pin code as retrieved an incoming pairing request
	boolean setPin(String pin);
	// This method is not required when Head Unit take the charge for accept or reject incoming pairing request.
	boolean setPairingConfirmation(boolean accept);
	
	
}
