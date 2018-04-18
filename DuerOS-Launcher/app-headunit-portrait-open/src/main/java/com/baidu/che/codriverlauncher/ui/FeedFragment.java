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

import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.imageloader.ImageLoader;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * music information fragment
 */
public class FeedFragment extends Fragment {
    private static final String TAG = "FeedFragment";
    private static final String CAR_RADIO_PLAY_INFO = "baidu.car.radio.play.info";
    private static final String CAR_RADIO_START_ACTION = "baidu.car.radio.action.start";

    private static final String CAR_RADIO_PLAY_INFO_START_ACTION = "baidu.car.radio.play.info.start";
    private static final String CAR_RADIO_PLAY_INFO_PAUSE_ACTION = "baidu.car.radio.play.info.pause";

    private static final String BAIDU_CAR_RADIO_INTENT_KEY = "car_radio_key";
    private static final String BAIDU_CAR_RADIO_INTENT_PLAY = "car_radio_play";

    private TextView mTitle;
    private TextView mSubTitle;
    private ProgressBar mProgress;
    private ImageView mImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, null);
        mTitle = (TextView) view.findViewById(R.id.feed_title);
        mSubTitle = (TextView) view.findViewById(R.id.feed_subtitle);
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mImage = (ImageView) view.findViewById(R.id.feed_img);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CAR_RADIO_START_ACTION);
                intent.putExtra(BAIDU_CAR_RADIO_INTENT_KEY, BAIDU_CAR_RADIO_INTENT_PLAY);
                LauncherUtil.startActivitySafely(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(CAR_RADIO_PLAY_INFO);
        getActivity().registerReceiver(mFeedReceiver, intentFilter);

        Intent intent = new Intent(CAR_RADIO_PLAY_INFO_START_ACTION);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mFeedReceiver);
        Intent intent = new Intent(CAR_RADIO_PLAY_INFO_PAUSE_ACTION);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private BroadcastReceiver mFeedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CAR_RADIO_PLAY_INFO.equals(action)) {
                // Receive play information
                int duration = intent.getIntExtra("duration", Integer.MAX_VALUE);
                if (duration == 0) {
                    duration = Integer.MAX_VALUE;
                }
                int currentPosition = intent.getIntExtra("currentPosition", 0);
                mProgress.setProgress(currentPosition * 100 / duration);

                String musicId = intent.getStringExtra("musicId");
                LogUtil.d(TAG, "onReceive musicId:" + musicId + " duration:"
                        + duration + " currentPosition:" + currentPosition);

                if (musicId != null) {
                    String musicName = intent.getStringExtra("musicName");
                    String imageUrl = intent.getStringExtra("musicPic");
                    String albumName = intent.getStringExtra("albumName");
                    mTitle.setText(musicName);
                    mSubTitle.setText(albumName);
                    ImageLoader.load(getActivity(), mImage, imageUrl, R.drawable.feed_default);

                    LogUtil.d(TAG, "onReceive musicName:" + musicName + " albumName:"
                            + albumName + " imageUrl:" + imageUrl);
                }
            }
        }
    };
}
