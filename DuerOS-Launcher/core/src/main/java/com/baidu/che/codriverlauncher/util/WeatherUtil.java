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

import java.util.Observable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/*
 * weather tool
 */
public class WeatherUtil extends Observable {
    private static final String TAG = "WeatherUtil";
    public static final String REQUEST_UPDATE_WEATHER_ACTION = "com.hkmc.intent.action.request_weather_update";
    public static final String UPDATE_WEATHER_ACTION = "com.hkmc.intent.action.weather_update";
    public static final String KEY_WEATHER_CONDITION = "com.hkmc.extras.weather.weather_condition";
    public static final String KEY_WEATHER_NAME = "com.hkmc.extras.weather.weather_name";

    private static WeatherUtil mInstance;

    public enum WEATHER {
        UNKNOWN, SUN, RAIN, SNOW, CLOUD, WIND, FOG, SAND_STORM
    }

    private WeatherUtil() {

    }

    public static WeatherUtil getInstance() {
        if (mInstance == null) {
            synchronized (WeatherUtil.class) {
                if (mInstance == null) {
                    mInstance = new WeatherUtil();
                    return mInstance;
                }
            }
        }

        return mInstance;
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init()");
        IntentFilter intentFilter = new IntentFilter(UPDATE_WEATHER_ACTION);
        context.registerReceiver(mReceiver, intentFilter);
        context.sendBroadcast(new Intent(REQUEST_UPDATE_WEATHER_ACTION));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "onReceive()");
            if (intent != null) {
                parseWeatherType(intent.getIntExtra(KEY_WEATHER_CONDITION, 0), intent.getStringExtra(KEY_WEATHER_NAME));
            }
        }
    };

    private void parseWeatherType(int type, String name) {
        WEATHER weatherType;
        if (type == 1) {
            weatherType = WEATHER.SUN;
        } else if (type == 2 || type == 3) {
            weatherType = WEATHER.CLOUD;
        } else if (type >= 4 && type <= 13 || type == 20 || type >= 22 && type <= 26) {
            weatherType = WEATHER.RAIN;
        } else if (type >= 14 && type <= 18 || type >= 27 && type <= 29) {
            weatherType = WEATHER.SNOW;
        } else if (type == 19 || type == 33) {
            weatherType = WEATHER.FOG;
        } else if (type == 21 || type >= 30 && type <= 32) {
            weatherType = WEATHER.SAND_STORM;
        } else if (name != null && name.contains("雨")) {
            weatherType = WEATHER.RAIN;
        } else if (name != null && name.contains("雪")) {
            weatherType = WEATHER.SNOW;
        } else {
            weatherType = WEATHER.UNKNOWN;
        }

        LogUtil.d(TAG, "mWeatherType=" + weatherType.name());
        setChanged();
        notifyObservers(new WeatherData(weatherType, name));
    }

    public static class WeatherData {

        public WeatherData(WEATHER type, String name) {
            this.mType = type;
            this.mName = name;
        }

        public WEATHER mType;
        public String mName;
    }

}
