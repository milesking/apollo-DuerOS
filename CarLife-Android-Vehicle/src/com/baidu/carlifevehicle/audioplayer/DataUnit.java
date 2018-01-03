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
package com.baidu.carlifevehicle.audioplayer;

/**
 * used by data queue;
 * 
 * @author liucaiquan
 * 
 */
public class DataUnit {
    private PCMPlayerUtils.EPCMPackageType audioDataUnitHeadType;
    private int audioDataUnitTimeStamp;
    private int audioDataUnitSampleRate;
    private int audioDataUnitChannelConfig;
    private int audioDataUnitFormat;
    private byte[] audioDataUnitPcmData;
    private int audioDataUnitDataSize;

    // initial package
    public DataUnit(PCMPlayerUtils.EPCMPackageType headType, int sampleRate, int channelConfig, int format) {
        audioDataUnitHeadType = headType;
        audioDataUnitSampleRate = sampleRate;
        audioDataUnitChannelConfig = channelConfig;
        audioDataUnitFormat = format;
    }

    // normal data package
    public DataUnit(PCMPlayerUtils.EPCMPackageType headType, int timeStamp, byte[] data, int size) {
        audioDataUnitHeadType = headType;

        audioDataUnitTimeStamp = timeStamp;

        audioDataUnitDataSize = size;
        audioDataUnitPcmData = new byte[size];
        System.arraycopy(data, 0, audioDataUnitPcmData, 0, size);
    }

    // command package
    public DataUnit(PCMPlayerUtils.EPCMPackageType headType) {
        audioDataUnitHeadType = headType;
    }

    //
    public void setHeadType(PCMPlayerUtils.EPCMPackageType headType) {
        audioDataUnitHeadType = headType;
    }

    public PCMPlayerUtils.EPCMPackageType getHeadType() {
        return audioDataUnitHeadType;
    }

    //
    public void setTimeStamp(int timeStamp) {
        audioDataUnitTimeStamp = timeStamp;
    }

    public int getTimeStamp() {
        return audioDataUnitTimeStamp;
    }

    //
    public void setSampleRate(int sampleRate) {
        audioDataUnitSampleRate = sampleRate;
    }

    public int getSampleRate() {
        return audioDataUnitSampleRate;
    }

    //
    public void setChannelConfig(int channelConfig) {
        audioDataUnitChannelConfig = channelConfig;
    }

    public int getChannelConfig() {
        return audioDataUnitChannelConfig;
    }

    //
    public void setFormat(int format) {
        audioDataUnitFormat = format;
    }

    public int getFormat() {
        return audioDataUnitFormat;
    }

    public void setPCMData(byte[] data, int size) {
        if (size > 0) {
            if (audioDataUnitPcmData == null) {
                audioDataUnitPcmData = new byte[size];
            }

            System.arraycopy(data, 0, audioDataUnitPcmData, 0, size);
        }
    }

    public byte[] getPCMData() {
        return audioDataUnitPcmData;
    }

    //
    public void setDataSize(int size) {
        audioDataUnitDataSize = size;
    }

    public int getDataSize() {
        return audioDataUnitDataSize;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
