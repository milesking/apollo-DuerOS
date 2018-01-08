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
package com.baidu.che.codriverlauncher.ui;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.util.LogUtil;
import com.baidu.che.codriverlauncher.util.WeatherUtil;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * show weather,time and so on
 */
public class InfoFragment extends Fragment implements Observer {
    public static final String TAG = "InfoFragment";
    public static final int REFRESH_TIME = 100;

    private TextView mTimeText;
    private TextView mDateText;
    private TextView mWeekText;
    private TextView mWeatherText;
    private ImageView mWeatherImage;
    private boolean mShowYear = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_TIME:
                    updateTime();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, null);
        mTimeText = (TextView) view.findViewById(R.id.info_tv_time);
        mDateText = (TextView) view.findViewById(R.id.info_tv_date);
        mWeekText = (TextView) view.findViewById(R.id.info_tv_week);
        mWeatherText = (TextView) view.findViewById(R.id.info_tv_weather);
        mWeatherImage = (ImageView) view.findViewById(R.id.info_img_weather);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTime();
        WeatherUtil.getInstance().addObserver(this);
        WeatherUtil.getInstance().init(getActivity().getApplicationContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(REFRESH_TIME);
        WeatherUtil.getInstance().deleteObserver(this);
    }

    public void updateTime() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int date = c.get(Calendar.DATE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int week = c.get(Calendar.DAY_OF_WEEK);

        String hour;
        String minute;
        String weekDay;

        if (hours < 10) {
            hour = "0" + hours;
        } else {
            hour = "" + hours;
        }
        if (minutes < 10) {
            minute = "0" + minutes;
        } else {
            minute = "" + minutes;
        }

        switch (week) {
            case Calendar.MONDAY:
                weekDay = getString(R.string.one);
                break;
            case Calendar.TUESDAY:
                weekDay = getString(R.string.two);
                break;
            case Calendar.WEDNESDAY:
                weekDay = getString(R.string.three);
                break;
            case Calendar.THURSDAY:
                weekDay = getString(R.string.four);
                break;
            case Calendar.FRIDAY:
                weekDay = getString(R.string.five);
                break;
            case Calendar.SATURDAY:
                weekDay = getString(R.string.six);
                break;
            case Calendar.SUNDAY:
                weekDay = getString(R.string.sunday);
                break;
            default:
                weekDay = "";
                break;
        }

        mTimeText.setText(hour + ":" + minute);
        if (mShowYear) {
            mDateText.setText(year + getString(R.string.year) + month + getString(R.string.month)
                    + date + getString(R.string.day));
        } else {
            mDateText.setText(month + getString(R.string.month) + date + getString(R.string.day));
        }
        mWeekText.setText(getString(R.string.week) + weekDay);
        mHandler.removeMessages(REFRESH_TIME);
        mHandler.sendEmptyMessageDelayed(REFRESH_TIME, 1000);
    }

    @Override
    public void update(Observable observable, Object data) {
        LogUtil.d(TAG, "update()");
        WeatherUtil.WeatherData weatherData = (WeatherUtil.WeatherData) data;
        switch (weatherData.mType) {
            case CLOUD:
                mWeatherImage.setImageResource(R.drawable.ic_cloudy);
                break;
            case SUN:
                mWeatherImage.setImageResource(R.drawable.ic_sun);
                break;
            case WIND:
                mWeatherImage.setImageResource(R.drawable.ic_wind);
                break;
            case RAIN:
                mWeatherImage.setImageResource(R.drawable.ic_rain);
                break;
            case SNOW:
                mWeatherImage.setImageResource(R.drawable.ic_snow);
                break;
            case FOG:
                mWeatherImage.setImageResource(R.drawable.ic_fog);
                break;
            case SAND_STORM:
                mWeatherImage.setImageResource(R.drawable.ic_sand_storm);
                break;
            default:
                mWeatherImage.setImageResource(R.drawable.ic_cloudy);
                break;
        }
        mWeatherText.setText(weatherData.mName);
    }
}
