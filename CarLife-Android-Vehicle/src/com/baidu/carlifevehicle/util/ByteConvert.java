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
package com.baidu.carlifevehicle.util;

public class ByteConvert {

    /**
     * convert long to network byte order byte[]
     * @param n the long value to be converted
     * @return byte[]
     */
    public static byte[] longToBytes(long n) {
        byte[] result = new byte[8];
        result[7] = (byte) (n & 0xff);
        result[6] = (byte) (n >> 8 & 0xff);
        result[5] = (byte) (n >> 16 & 0xff);
        result[4] = (byte) (n >> 24 & 0xff);
        result[3] = (byte) (n >> 32 & 0xff);
        result[2] = (byte) (n >> 40 & 0xff);
        result[1] = (byte) (n >> 48 & 0xff);
        result[0] = (byte) (n >> 56 & 0xff);
        return result;
    }

    /**
     * convert long to network byte order byte array and store the result to a specific byte array
     * @param n the long value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void longToBytes(long n, byte[] destination, int offset) {
        destination[7 + offset] = (byte) (n & 0xff);
        destination[6 + offset] = (byte) (n >> 8 & 0xff);
        destination[5 + offset] = (byte) (n >> 16 & 0xff);
        destination[4 + offset] = (byte) (n >> 24 & 0xff);
        destination[3 + offset] = (byte) (n >> 32 & 0xff);
        destination[2 + offset] = (byte) (n >> 40 & 0xff);
        destination[1 + offset] = (byte) (n >> 48 & 0xff);
        destination[0 + offset] = (byte) (n >> 56 & 0xff);
    }

    /**
     * convert network byte order byte array to long
     * @param source the byte array to be converted
     * @return the long result
     */
    public static long bytesToLong(byte[] source) {
        return ((((long) source[0] & 0xff) << 56) | (((long) source[1] & 0xff) << 48)
                | (((long) source[2] & 0xff) << 40) | (((long) source[3] & 0xff) << 32)
                | (((long) source[4] & 0xff) << 24) | (((long) source[5] & 0xff) << 16)
                | (((long) source[6] & 0xff) << 8) | (((long) source[7] & 0xff) << 0));
    }

    /**
     * convert network byte order byte array to long at specific offset
     * @param source the byte array to convert
     * @param offset the start offset
     * @return the long result
     */
    public static long bytesToLong(byte[] source, int offset) {
        return ((((long) source[offset + 0] & 0xff) << 56)
                | (((long) source[offset + 1] & 0xff) << 48)
                | (((long) source[offset + 2] & 0xff) << 40)
                | (((long) source[offset + 3] & 0xff) << 32)
                | (((long) source[offset + 4] & 0xff) << 24)
                | (((long) source[offset + 5] & 0xff) << 16)
                | (((long) source[offset + 6] & 0xff) << 8) | (((long) source[offset + 7] & 0xff) << 0));
    }

    /**
     * convert int to network byte order byte[]
     * @param n the int value to be converted
     * @return byte[]
     */
    public static byte[] intToBytes(int n) {
        byte[] result = new byte[4];
        result[3] = (byte) (n & 0xff);
        result[2] = (byte) (n >> 8 & 0xff);
        result[1] = (byte) (n >> 16 & 0xff);
        result[0] = (byte) (n >> 24 & 0xff);
        return result;
    }

    /**
     * convert int to network byte order byte array and store the result to a specific byte array
     * @param n the int value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void intToBytes(int n, byte[] destination, int offset) {
        destination[3 + offset] = (byte) (n & 0xff);
        destination[2 + offset] = (byte) (n >> 8 & 0xff);
        destination[1 + offset] = (byte) (n >> 16 & 0xff);
        destination[offset] = (byte) (n >> 24 & 0xff);
    }

    /**
     * convert network byte order byte array to int
     * @param source the byte array to be converted
     * @return the int result
     */
    public static int bytesToInt(byte[] source) {
        return source[3] & 0xff | (source[2] & 0xff) << 8 | (source[1] & 0xff) << 16 | (source[0] & 0xff) << 24;
    }

    /**
     * convert network byte order byte array to int at specific offset
     * @param source the byte array to store result
     * @param offset the start offset
     * @return the int result
     */
    public static int bytesToInt(byte[] source, int offset) {
        return source[offset + 3] & 0xff | (source[offset + 2] & 0xff) << 8 | (source[offset + 1] & 0xff) << 16
                | (source[offset] & 0xff) << 24;
    }

    /**
     * convert unsigned int to network byte order byte[]
     * @param n the unsigned int value to be converted
     * @return byte[]
     */
    public static byte[] uintToBytes(long n) {
        byte[] result = new byte[4];
        result[3] = (byte) (n & 0xff);
        result[2] = (byte) (n >> 8 & 0xff);
        result[1] = (byte) (n >> 16 & 0xff);
        result[0] = (byte) (n >> 24 & 0xff);

        return result;
    }

    /**
     * convert unsigned int to network byte order byte array and store the result to a specific byte array
     * @param n the unsigned int value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void uintToBytes(long n, byte[] destination, int offset) {
        destination[3 + offset] = (byte) (n);
        destination[2 + offset] = (byte) (n >> 8 & 0xff);
        destination[1 + offset] = (byte) (n >> 16 & 0xff);
        destination[offset] = (byte) (n >> 24 & 0xff);
    }

    /**
     * convert network byte order byte array to unsigned int
     * @param source the byte array to be converted
     * @return the int result
     */
    public static long bytesToUint(byte[] source) {
        return ((long) (source[3] & 0xff)) | ((long) (source[2] & 0xff)) << 8
                | ((long) (source[1] & 0xff)) << 16 | ((long) (source[0] & 0xff)) << 24;
    }

    /**
     * convert network byte order byte array to unsigned int at specific offset
     * @param source the byte array to store result
     * @param offset the start offset
     * @return the unsigned int result
     */
    public static long bytesToUint(byte[] source, int offset) {
        return ((long) (source[offset + 3] & 0xff)) | ((long) (source[offset + 2] & 0xff)) << 8
                | ((long) (source[offset + 1] & 0xff)) << 16 | ((long) (source[offset] & 0xff)) << 24;
    }

    /**
     * convert short to network byte order byte[]
     * @param n the short value to be converted
     * @return byte[]
     */
    public static byte[] shortToBytes(short n) {
        byte[] result = new byte[2];
        result[1] = (byte) (n & 0xff);
        result[0] = (byte) ((n >> 8) & 0xff);
        return result;
    }

    /**
     * convert short to network byte order byte array and store the result to a specific byte array
     * @param n the short value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void shortToBytes(short n, byte[] destination, int offset) {
        destination[offset + 1] = (byte) (n & 0xff);
        destination[offset] = (byte) ((n >> 8) & 0xff);
    }

    /**
     * convert network byte order byte array to short
     * @param source the byte array to be converted
     * @return the short result
     */
    public static short bytesToShort(byte[] source) {
        return (short) (source[1] & 0xff | (source[0] & 0xff) << 8);
    }

    /**
     * convert network byte order byte array to short at specific offset
     * @param source the byte array to store result
     * @param offset the start offset
     * @return the short result
     */
    public static short bytesToShort(byte[] source, int offset) {
        return (short) (source[offset + 1] & 0xff | (source[offset] & 0xff) << 8);
    }

    /**
     * convert short to network byte order byte[]
     * @param n the short value to be converted
     * @return byte[]
     */
    public static byte[] ushortToBytes(int n) {
        byte[] result = new byte[2];
        result[1] = (byte) (n & 0xff);
        result[0] = (byte) ((n >> 8) & 0xff);
        return result;
    }

    /**
     * convert short to network byte order byte array and store the result to a specific byte array
     * @param n the short value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void ushortToBytes(int n, byte[] destination, int offset) {
        destination[offset + 1] = (byte) (n & 0xff);
        destination[offset] = (byte) ((n >> 8) & 0xff);
    }

    /**
     * convert network byte order byte array to short
     * @param source the byte array to be converted
     * @return the short result
     */
    public static int bytesToUshort(byte[] source) {
        return source[1] & 0xff | (source[0] & 0xff) << 8;
    }

    /**
     * convert network byte order byte array to short at specific offset
     * @param source the byte array to store result
     * @param offset the start offset
     * @return the short result
     */
    public static int bytesToUshort(byte[] source, int offset) {
        return source[offset + 1] & 0xff | (source[offset] & 0xff) << 8;
    }

    /**
     * convert byte to network byte order byte[]
     * @param n the byte value to be converted
     * @return byte[]
     */
    public static byte[] ubyteToBytes(int n) {
        byte[] result = new byte[1];
        result[0] = (byte) (n & 0xff);
        return result;
    }

    /**
     * convert byte to byte array and store the result to a specific byte array
     * @param n the byte value to be converted
     * @param destination the byte array to store the result
     * @param offset the start offset
     */
    public static void ubyteToBytes(int n, byte[] destination, int offset) {
        destination[0] = (byte) (n & 0xff);
    }

    /**
     * convert network byte order byte array to byte
     * @param source the byte array to be converted
     * @return the byte result
     */
    public static int bytesToUbyte(byte[] source) {
        return source[0] & 0xff;
    }

    /**
     * convert network byte order byte array to byte at specific offset
     * @param source the byte array to store result
     * @param offset the start offset
     * @return the byte result
     */
    public static int bytesToUbyte(byte[] source, int offset) {
        return source[offset] & 0xff;
    }
}