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
package com.baidu.che.codriverlauncher.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 *Queen for caching statistics data
 */
public class Cache<T> {
    private static final int MAX_UPLOAD_NUM_EACH_MINUTE = 200;
    private int mMaxsize = MAX_UPLOAD_NUM_EACH_MINUTE;
    private Queue<T> mQueue;

    public Cache(int maxsize) {
        mMaxsize = maxsize;
        mQueue = new LinkedBlockingQueue();
    }

    public Cache() {
        mQueue = new LinkedBlockingQueue();
    }

    public synchronized int size() {
        return mQueue.size();
    }

    public synchronized void insert(T model) {
        if (mQueue.size() == mMaxsize) {
            mQueue.remove();
        }

        mQueue.add(model);
    }

    public synchronized void insertAll(List<T> model) {

        for (int i = 0; i < model.size(); i++) {
            if (mQueue.size() == mMaxsize) {
                mQueue.remove();
            }
            mQueue.add(model.get(i));
        }

    }

    public synchronized List<T> take(int num) {
        List<T> list = new LinkedList();
        for (int i = 0; i < num; i++) {
            list.add(mQueue.poll());
        }

        return list;
    }

    public synchronized List<T> takeAll() {
        List<T> list = new LinkedList();
        int queueSize = mQueue.size();
        for (int i = 0; i < queueSize; i++) {
            list.add(mQueue.poll());
        }

        return list;
    }
}
