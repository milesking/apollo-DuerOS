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
import com.baidu.carlifevehicle.bluetooth.IHfpClientCallback;
interface IHfpClient {
    boolean registerHfpCallback(IHfpClientCallback callback);
    boolean unregisterHfpCallback(IHfpClientCallback callback);
	boolean dial(String number);
	boolean acceptCall();
	boolean rejectCall();
	boolean terminateCall();
	boolean sendDTMF(byte code);
	//Set mic status to mute or unmute,(TRUE: mute, FALSE: unmute)
	boolean setMic(boolean enable);
	//Get mic status, this function shall return current status for mic (TRUE: mute or FALSE: unmute)
	boolean getMic();
	//Service version, For example "0.1" as initial version,may changed as APIs modified
	//block or unblock native telephone app,TRUE: block, FALSE: unblock
	boolean blockNativeTelephone(boolean enable);
	String getVersion();
	int getConnectionState(); 
}
