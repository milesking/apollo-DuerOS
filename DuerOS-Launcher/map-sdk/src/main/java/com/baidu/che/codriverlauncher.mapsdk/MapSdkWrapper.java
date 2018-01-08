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
package com.baidu.che.codriverlauncher.mapsdk;

import com.baidu.mapapi.SDKInitializer;

import android.content.Context;

/**
 * map sdk wrapper
 */

public class MapSdkWrapper {

    public static void init(Context context) {
        SDKInitializer.initialize(context);
        LocationUtil.getInstance().start(context);
    }

    public static void setCruiseChangeListener(OnLocationListener onLocationListener) {
        LocationUtil.getInstance().setCruiseChangeListener(onLocationListener);
    }

    public static double getLongitude() {
        return LocationUtil.getInstance().getLongitude();
    }

    public static double getLatitude() {
        return LocationUtil.getInstance().getLatitude();
    }

    public static double getLatitudeBd09ll() {
        return LocationUtil.getInstance().getLatitudeBd09ll();

    }

    public static double getLongitudeBd09ll() {
        return LocationUtil.getInstance().getLongitudeBd09ll();
    }

}


