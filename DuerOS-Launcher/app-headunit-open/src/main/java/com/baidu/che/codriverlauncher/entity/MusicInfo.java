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
package com.baidu.che.codriverlauncher.entity;

import java.io.Serializable;

/**
 * Music bean
 */

public class MusicInfo implements Serializable {
    public String musicId;
    public int duration;
    public int progress;
    public String musicName;
    public String albumName;
    public String imgUrl;

    public MusicInfo(String musicId, int duration, int progress, String musicName, String albumName,
                     String imgUrl) {
        this.musicId = musicId;
        this.duration = duration;
        this.progress = progress;
        this.musicName = musicName;
        this.albumName = albumName;
        this.imgUrl = imgUrl;
    }
}
