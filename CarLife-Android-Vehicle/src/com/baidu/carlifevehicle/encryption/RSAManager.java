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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Base64;

/**
 * Created by liucaiquan on 2017/2/22.
 */

public class RSAManager {
    PublicKey mPublicKey;
    PrivateKey mPrivateKey;
    private String mPrivateKeyString = null;
    private String mPublicKeyString = null;

    public RSAManager() {
        keyPairGenerate();
    }

    public String getPublicKeyString() {
        return mPublicKeyString;
    }

    public PublicKey getmPubkey(String key) {
        byte[] keyBytes = Base64.decode(key, Base64.NO_WRAP);
        ;
        PublicKey publicKey = null;

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKey;
    }

    public String getPrivateKeyString() {
        return mPrivateKeyString;
    }

    public PrivateKey getPrivateKey() {
        return mPrivateKey;
    }

    /**
     * Use private key to decrypt
     *
     * @param data
     * @param privateKey
     *
     * @return
     */
    public String decrypt(String data, PrivateKey privateKey) {
        byte[] rst = null;
        String rstStr = null;
        try {
            byte[] encodedContent = Base64.decode(data, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(EncryptConfig.TRANSFORMATION_SETTING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            rst = cipher.doFinal(encodedContent);
            rstStr = new String(rst, "UTF-8");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rstStr;
    }

    /**
     *  encrypted by public key
     *
     * @param data
     * @param publicKey
     *
     * @return
     */
    public String encrypt(String data, PublicKey publicKey) {
        byte[] rst = null;
        String rstStr = null;
        try {
            Cipher cipher = Cipher.getInstance(EncryptConfig.TRANSFORMATION_SETTING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipher.update(data.getBytes("UTF-8"));

            rst = cipher.doFinal();
            rstStr = Base64.encodeToString(rst, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rstStr;
    }

    private void keyPairGenerate() {
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");

            SecureRandom secrand = new SecureRandom();
            keygen.initialize(2048, secrand);
            KeyPair keys = keygen.genKeyPair();

            mPublicKey = keys.getPublic();
            mPrivateKey = keys.getPrivate();

            mPublicKeyString = Base64.encodeToString(mPublicKey.getEncoded(), Base64.NO_WRAP);
            mPrivateKeyString = Base64.encodeToString(mPrivateKey.getEncoded(), Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
