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

public class JniEanMethod {
    static {
        System.loadLibrary("iPhone_ean");
    }
    // Open EA
    public static native int openEan();
    // Close EA
    public static native void closeEan();
    // Read data from the EAP port
    public static native int eanRead(byte[] rxArray, int dataLength);
    // Write data to the EAP port
    public static native int eanWrite(byte[] wrArray, int dataLength);
    // EAP port control command
    public static native int eanIoctl();

}
