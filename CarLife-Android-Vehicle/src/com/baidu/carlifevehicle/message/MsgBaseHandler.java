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
package com.baidu.carlifevehicle.message;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Handler;
import android.os.Looper;

/**
 * base class for message handler
 * @author ouyangnengjun
 * 
 */
public abstract class MsgBaseHandler extends Handler {

    /**
     * interested messages
     */
    private ArrayList<Integer> mInterests = new ArrayList<Integer>();

    /**
     * subclass should add interested messages in this method.
     */
    public abstract void careAbout();

    public MsgBaseHandler(Looper looper) {
        super(looper);
        careAbout();
    }

    public MsgBaseHandler() {
        super();
        careAbout();
    }

    /**
     * add msgId to interest list
     * @param msgID {@link android.os.Message#what}
     */
    public void addMsg(int msgID) {
        for (Integer i : mInterests) {
            if (i == msgID) {
                return;
            }
        }

        mInterests.add(msgID);
    }

    /**
     * remove msgId from interest list
     * @param msgID {@link android.os.Message#what}
     */
    public void removeMsg(int msgID) {
        for (Iterator<Integer> it = mInterests.iterator(); it.hasNext();) {
            Integer i = it.next();
            if (i == msgID) {
                it.remove();
            }
        }
    }

    /**
     * whether msgId is added to interest list
     * @param msgID {@link android.os.Message#what}
     * @return true for added, false for not
     */
    public boolean isAdded(int msgID) {
        if (mInterests == null) {
            return false;
        }
        for (int i = 0; i < mInterests.size(); i++) {
            if (mInterests.get(i) == msgID) {
                return true;
            }
        }

        return false;
    }

}
