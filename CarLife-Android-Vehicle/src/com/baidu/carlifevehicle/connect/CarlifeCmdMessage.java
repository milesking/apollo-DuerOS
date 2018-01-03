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

import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.util.ByteConvert;
import com.baidu.carlifevehicle.util.LogUtil;

public class CarlifeCmdMessage {

    private static final String TAG = "CarlifeCmdMessage";
    private static int total_index = 0;

    private int index = CommonParams.MSG_CMD_DEFAULT_VALUE;

    /*
     * length       0~15    16  The length of the data.
     * reserved     16~31   16  Keep the field
     * service type 32~63   32  The service type of the message.
     * data         64~n    0~m The stream of bytes after PB serialization
     */
    private int length = CommonParams.MSG_CMD_DEFAULT_VALUE;
    private int reserved = CommonParams.MSG_CMD_TYPE_RESERVED;
    private int serviceType = CommonParams.MSG_CMD_DEFAULT_VALUE;

    byte[] data = null;

    public CarlifeCmdMessage(boolean isSend) {
        if (isSend) {
            index = ++total_index;
        }
    }

    public boolean fromByteArray(byte[] msg) {
        if (msg.length != CommonParams.MSG_CMD_HEAD_SIZE_BYTE) {
            LogUtil.e(TAG, "fromByteArray fail: length not equal");
            return false;
        }
        int tmpParam = 0;
        byte tmpByte = 0;

        try {
            tmpParam = (int) ByteConvert.bytesToShort(new byte[] {msg[0], msg[1]});
            setLength(tmpParam);

            tmpParam = (int) ByteConvert.bytesToShort(new byte[] {msg[2], msg[3]});
            setReserved(tmpParam);

            tmpParam = (int) ByteConvert.bytesToInt(new byte[] {msg[4], msg[5], msg[6], msg[7]});
            setServiceType(tmpParam);
        } catch (Exception e) {
            LogUtil.e(TAG, "fromByteArray fail: get exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[CommonParams.MSG_CMD_HEAD_SIZE_BYTE];
        byte[] tmpBytes = null;
        int tmpParam = 0;
        int i = 0;
        try {
            tmpBytes = ByteConvert.intToBytes(length);
            bytes[i++] = tmpBytes[2];
            bytes[i++] = tmpBytes[3];

            tmpBytes = ByteConvert.intToBytes(reserved);
            bytes[i++] = tmpBytes[2];
            bytes[i++] = tmpBytes[3];

            tmpBytes = ByteConvert.intToBytes(serviceType);
            bytes[i++] = tmpBytes[0];
            bytes[i++] = tmpBytes[1];
            bytes[i++] = tmpBytes[2];
            bytes[i++] = tmpBytes[3];
        } catch (Exception e) {
            LogUtil.e(TAG, "toByteArray fail: get exception");
            e.printStackTrace();
            return null;
        }

        return bytes;
    }

    public void setIndex(int ind) {
        if (ind < 0) {
            LogUtil.e(TAG, "set index fail: %d", ind);
            return;
        }
        index = ind;
    }

    public int getIndex() {
        return index;
    }

    public void setReserved(int ty) {
        if (ty < 0) {
            LogUtil.e(TAG, "set reserved fail: %d", ty);
            return;
        }
        reserved = ty;
    }

    public int getReserved() {
        return reserved;
    }

    public void setServiceType(int ty) {
        if (ty < 0) {
            LogUtil.e(TAG, "set service type fail: %d", ty);
            return;
        }
        serviceType = ty;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setLength(int len) {
        if (len < 0 || len > CommonParams.MSG_CMD_MAX_DATA_LEN) {
            LogUtil.e(TAG, "set data len fail: %d", len);
            return;
        }
        length = len;
    }

    public int getLength() {
        return length;
    }

    public void setData(byte[] obj) {
        data = obj;
    }

    public byte[] getData() {
        return data;
    }

}
