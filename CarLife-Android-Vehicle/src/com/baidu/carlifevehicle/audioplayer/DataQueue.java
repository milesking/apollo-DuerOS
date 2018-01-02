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

import com.baidu.carlifevehicle.util.LogUtil;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * module functions:
 * <p>
 * 1.queue management;
 * 
 * @author Liu Caiquan
 * 
 * 
 */
public class DataQueue {
    private static final String TAG = PCMPlayerUtils.AUDIO_MODULE_PREFIX + DataQueue.class.getSimpleName();
    private ArrayBlockingQueue<DataUnit> mBlockingQueue;

    public DataQueue(int arryBlockQueueSize) {
        mBlockingQueue = new ArrayBlockingQueue(arryBlockQueueSize);
    }

    /*
     * return: size of queue:success -1 :faulse
     */
    // initial package
    public int add(PCMPlayerUtils.EPCMPackageType headType, int sampleRate, int channelConfig, int format) {

        try {
            mBlockingQueue.put(new DataUnit(headType, sampleRate, channelConfig, format));
        } catch (InterruptedException e) {
            e.printStackTrace();
            mBlockingQueue.clear();

            return -1;
        }

        return 0;
    }

    // normal data package
    public int add(PCMPlayerUtils.EPCMPackageType headType, int timeStamp, byte[] data, int size) {
        try {
            mBlockingQueue.put(new DataUnit(headType, timeStamp, data, size));
        } catch (InterruptedException e) {
            e.printStackTrace();
            mBlockingQueue.clear();

            return -1;
        }

        return 0;
    }

    // command package
    public int add(PCMPlayerUtils.EPCMPackageType headType) {
        try {
            mBlockingQueue.put(new DataUnit(headType));
        } catch (InterruptedException e) {
            e.printStackTrace();
            mBlockingQueue.clear();

            return -1;
        }

        return 0;
    }

    /*
     * return: positive number: success -1:queue is empty
     */
    public DataUnit pollIndex() {
        return mBlockingQueue.poll();
    }

    // can not use "synchronized" which will result in deadlock
    public DataUnit takeIndex() {
        try {
            return mBlockingQueue.take();
        } catch (InterruptedException e) {
            LogUtil.d(TAG, "takeIndex(): InterruptedException happen");
            e.printStackTrace();
            return null;
        }
    }

    public void clear() {
        mBlockingQueue.clear();
    }

    public int getBufferDataNum() {
        return mBlockingQueue.size();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
