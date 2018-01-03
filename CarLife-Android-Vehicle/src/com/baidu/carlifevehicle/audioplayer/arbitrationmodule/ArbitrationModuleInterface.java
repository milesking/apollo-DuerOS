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
package com.baidu.carlifevehicle.audioplayer.arbitrationmodule;

import com.baidu.carlifevehicle.audioplayer.PCMPlayerUtils;

/**
 * interface for arbitration module
 * 
 * @author liucaiquan
 * 
 */
public interface ArbitrationModuleInterface {
    void priorityArbitrationProcessor(PCMPlayerUtils.EPCMPackageType packageType, int sampleRate, int channelConfig,
            int format);

    void writeAudioTrack(byte[] data, int offset, int size);

    void writeTTSAudioTrack(byte[] data, int offset, int size);

    void writeMusicAudioTrack(byte[] data, int offset, int size);

    PCMPlayerUtils.EAMPStatus getAMPStatus();

    void informMediaPlayThreadRelease();

    public int requestVRAudioFocus();

    public int abandonVRAudioFocus();

}
