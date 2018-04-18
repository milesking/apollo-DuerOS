/******************************************************************************
 * Copyright 2018 The Baidu Authors. All Rights Reserved.
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

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.util.LogUtil;
import com.baidu.che.codriverlauncher.util.WeatherUtil;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final String TAG = "InfoFragment";
    public static final int REFRESH_TIME = 100;
    public static final String TRAFFIC_REQUEST_ACTION = "com.baidu.che.codriver.request_traffic";
    public static final String TRAFFIC_RESPONSE_ACTION = "com.baidu.che.codriver.response_traffic";

    private TextView mTimeText;
    private TextView mDateText;
    private TextView mWeekText;
    private TextView mWeatherText;
    private ImageView mWeatherImage;
    private View mSpaceView;
    private TextView mTrafficView;
    private boolean mShowYear = true;

    private final Handler mHandler = new CustomHandler(this);

    private static class CustomHandler extends Handler {
        private WeakReference<InfoFragment> weakReference;
        private InfoFragment fragment;

        public CustomHandler(InfoFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case REFRESH_TIME:
                    fragment.updateTime();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, null);
        mTimeText = (TextView) view.findViewById(R.id.tv_time);
        mDateText = (TextView) view.findViewById(R.id.tv_date);
        mWeekText = (TextView) view.findViewById(R.id.tv_week);
        mWeatherText = (TextView) view.findViewById(R.id.tv_weather);
        mWeatherImage = (ImageView) view.findViewById(R.id.img_weather);
        mSpaceView = view.findViewById(R.id.space_view);
        mTrafficView = (TextView) view.findViewById(R.id.tv_traffic);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTime();
        WeatherUtil.getInstance().addObserver(this);
        WeatherUtil.getInstance().init(getActivity().getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TRAFFIC_RESPONSE_ACTION);
        getActivity().registerReceiver(mReceiver, intentFilter);
        getActivity().sendBroadcast(new Intent(TRAFFIC_REQUEST_ACTION));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(REFRESH_TIME);
        WeatherUtil.getInstance().deleteObserver(this);
        getActivity().unregisterReceiver(mReceiver);
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

        String[] weeks = getResources().getStringArray(R.array.info_week);

        switch (week) {
            case Calendar.MONDAY:
                weekDay = weeks[0];
                break;
            case Calendar.TUESDAY:
                weekDay = weeks[1];
                break;
            case Calendar.WEDNESDAY:
                weekDay = weeks[2];
                break;
            case Calendar.THURSDAY:
                weekDay = weeks[3];
                break;
            case Calendar.FRIDAY:
                weekDay = weeks[4];
                break;
            case Calendar.SATURDAY:
                weekDay = weeks[5];
                break;
            case Calendar.SUNDAY:
                weekDay = weeks[6];
                break;
            default:
                weekDay = "";
                break;
        }

        mTimeText.setText(hour + ":" + minute);
        if (mShowYear) {
            mDateText.setText(year + getResources().getString(R.string.info_year)
                    + month + getResources().getString(R.string.info_month)
                    + date + getResources().getString(R.string.info_day));
        } else {
            mDateText.setText(month + getResources().getString(R.string.info_month)
                    + date + getResources().getString(R.string.info_day));
        }
        mWeekText.setText(getResources().getString(R.string.info_week) + weekDay);
        mHandler.removeMessages(REFRESH_TIME);
        mHandler.sendEmptyMessageDelayed(REFRESH_TIME, 1000);
    }

    @Override
    public void update(Observable observable, Object data) {
        LogUtil.d(TAG, "update()");
        WeatherUtil.WeatherData weatherData = (WeatherUtil.WeatherData) data;
        switch (weatherData.mType) {
            case CLOUD:
                mWeatherImage.setImageResource(R.drawable.cloudy);
                break;
            case SUN:
                mWeatherImage.setImageResource(R.drawable.sun);
                break;
            case WIND:
                mWeatherImage.setImageResource(R.drawable.wind);
                break;
            case RAIN:
                mWeatherImage.setImageResource(R.drawable.rain);
                break;
            case SNOW:
                mWeatherImage.setImageResource(R.drawable.snow);
                break;
            case FOG:
                mWeatherImage.setImageResource(R.drawable.fog);
                break;
            case SAND_STORM:
                mWeatherImage.setImageResource(R.drawable.sand_storm);
                break;
            default:
                mWeatherImage.setImageResource(R.drawable.cloudy);
                break;
        }
        mWeatherText.setText(weatherData.mName);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onReceive(): " + action);
            if (TRAFFIC_RESPONSE_ACTION.equals(action)) {
                if (intent.getStringExtra("number") != null) {
                    String number = intent.getStringExtra("number");
                    LogUtil.d(TAG, "traffic: " + number);
                    if (number.length() >= 2) {
                        mShowYear = false;
                        updateTime();
                        mSpaceView.setVisibility(View.VISIBLE);
                        mTrafficView.setVisibility(View.VISIBLE);
                        mTrafficView.setText(getResources().getString(R.string.info_limit_go)
                                + number.charAt(0) + "/" + number.charAt(1));
                    }
                }
            }
        }
    };
}
