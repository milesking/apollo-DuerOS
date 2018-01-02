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
package com.baidu.carlifevehicle.encryption;

/**
 * Created by liucaiquan on 2017/2/13.
 */

public class EncryptConfig {
    /**
     * 1 Raw mode (unencrypted mode):
     * DEBUG_ENABLE=false;
     * 2 AES Encryption Test Mode (Initially both parties agree that all messages and data be encrypted using AES)
     * DEBUG_ENABLE = true;
     * AES_ENCRYPT_AS_BEGINE = true;
     * 3 RSA interactive testing (complete encrypted transmission, exchange of keys via RSA, encryption using AES)
     * DEBUG_ENABLE = true;
     * AES_ENCRYPT_AS_BEGINE = false;
     */
    // Debug mode Turn on the switch (debug mode is set to true, unencrypted mode is set to false)
    public static final boolean DEBUG_ENABLE = true;
    // Both ends of the phone and the vehicle are AES-encrypted at the beginning (set to true during debug use and set
    // to false during normal use)
    public static final boolean AES_ENCRYPT_AS_BEGINE = false;

    // RSA Generates a seed for a random key pair
    public static String RSA_GEN_SEED = "woshisuijizifuchuan";
    // RSA conversion settings
    public static final String TRANSFORMATION_SETTING = "RSA/ECB/PKCS1Padding";
}
