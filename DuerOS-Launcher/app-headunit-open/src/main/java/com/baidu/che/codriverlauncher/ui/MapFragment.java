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

import static android.R.attr.direction;
import static android.content.Context.SENSOR_SERVICE;

import com.baidu.che.codriverlauncher.LauncherApp;
import com.baidu.che.codriverlauncher.R;
import com.baidu.che.codriverlauncher.mapsdk.MapSdkWrapper;
import com.baidu.che.codriverlauncher.mapsdk.OnLocationListener;
import com.baidu.che.codriverlauncher.receiver.DateChangeReceiver;
import com.baidu.che.codriverlauncher.receiver.NetworkConnectChangedReceiver;
import com.baidu.che.codriverlauncher.receiver.OnDateChangeListener;
import com.baidu.che.codriverlauncher.receiver.OnNetworkChangeListener;
import com.baidu.che.codriverlauncher.util.FileUtil;
import com.baidu.che.codriverlauncher.util.LauncherUtil;
import com.baidu.che.codriverlauncher.util.LogUtil;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import android.app.Fragment;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

/**
 * show map
 */
public class MapFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "MapFragment";

    private static final int accuracyCircleFillColor = 0;
    private static final int accuracyCircleStrokeColor = 0;
    public static final int UPDATE_MAP = 1;

    private static final String FILE_PATH =
            Environment.getExternalStorageDirectory().toString() + "/" + "codriver_skin";
    private static final String FILE_CAROS_BLACK = "custom_config_black";
    private static final String FILE_PATH_NAME = FILE_PATH + "/" + FILE_CAROS_BLACK;

    public static final String BAIDU_NAVIAUTO_START_ACTIVITY_ACTION = "com.baidu.naviauto.action.START";
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private FrameLayout framelayout;
    private View mapRecover;
    private MyLocationData locData;
    private RelativeLayout mProgressRl;

    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    private SensorManager mSensorManager;
    private double mCurrentLat = 0;
    private double mCurrentLon = 0;
    private float mCurrentAccracy = 0;
    private boolean isLoadMap = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initFile();
        MapSdkWrapper.setCruiseChangeListener(onLocationListener);
        NetworkConnectChangedReceiver.setNetworkLtListener(onNetworkChangeListener);
        DateChangeReceiver.setDateChangeListener(onDateChangeListener);
        initView(view);
        initMap();
        return view;
    }

    private void initView(View view) {
        mapRecover = view.findViewById(R.id.map_view_recover);
        mapRecover.setOnClickListener(this);

        framelayout = (FrameLayout) view.findViewById(R.id.map_frame);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.map_rl_progress);

        mSensorManager = (SensorManager) LauncherApp.getInstance().getSystemService(SENSOR_SERVICE);

        // set some custom config ,before mapview init
        if (FileUtil.isFileExists(FILE_PATH_NAME)) {
            MapView.setCustomMapStylePath(FILE_PATH_NAME);
        }
        mMapView = new MapView(getActivity());
        framelayout.addView(mMapView);
    }

    private void initMap() {
        // set button which can change large or small
        mMapView.setMapCustomEnable(true);
        View child = mMapView.getChildAt(1);
        // remove logo
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
            // ((ImageView)child).setImageDrawable(getResources().getDrawable(R.drawable.app));
        }
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);

        mBaiduMap = mMapView.getMap();
        // enable location layer
        mBaiduMap.setMyLocationEnabled(true);
        // unable traffic picture
        mBaiduMap.setTrafficEnabled(false);
        mBaiduMap.setBaiduHeatMapEnabled(false);
        mBaiduMap.setIndoorEnable(false);
        mBaiduMap.setBuildingsEnabled(false);

        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        // set custom icon、marker
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.car_point);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));

        mCurrentLat = MapSdkWrapper.getLatitudeBd09ll();
        mCurrentLon = MapSdkWrapper.getLongitudeBd09ll();
        // get direction info，clockwise 0-360
        locData = new MyLocationData.Builder().accuracy((float) mCurrentAccracy)
                .direction((float) direction).latitude(mCurrentLat).longitude(mCurrentLon).build();
        mBaiduMap.setMyLocationData(locData);
        LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll);
        // you can customize the size. level=19,the default scale is 14--100 meter
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(mBaiduMap.getMaxZoomLevel() - 5);
        mBaiduMap.animateMapStatus(u);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mBaiduMap.setOnMapRenderCallbadk(onMapRenderCallback);
        LogUtil.i(TAG, "Map is init");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    private void initFile() {
        FileUtil.makeDir(FILE_PATH);
        FileUtil.copyFromAssetsToSdcard(true, FILE_CAROS_BLACK, FILE_PATH + "/" + FILE_CAROS_BLACK);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPause() {
        LogUtil.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        LogUtil.d(TAG, "onStop");
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        // remove listener when destroy
        MapSdkWrapper.setCruiseChangeListener(null);
        NetworkConnectChangedReceiver.setNetworkLtListener(null);
        DateChangeReceiver.setDateChangeListener(null);
        // close location layer
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        mHandler.removeMessages(UPDATE_MAP);
        super.onDestroy();
    }

    public boolean doubleEqual(double a) {
        return Math.abs(a) < 0.000001;
    }

    public OnLocationListener onLocationListener = new OnLocationListener() {

        @Override
        public void onLocationChange(double latBd09ll, double lonBd09ll, double radBd09ll, double direction) {
            if (mMapView == null || mBaiduMap == null) {
                return;
            }
            if (doubleEqual(mCurrentLat - latBd09ll) && doubleEqual(mCurrentLon - lonBd09ll)) {
                return;
            }
            mCurrentAccracy = (float) radBd09ll;
            mCurrentLat = latBd09ll;
            mCurrentLon = lonBd09ll;
            locData = new MyLocationData.Builder().accuracy((float) radBd09ll)
                    .direction((float) direction).latitude(latBd09ll).longitude(lonBd09ll).build();
            mBaiduMap.setMyLocationData(locData);
        }

    };

    private OnDateChangeListener onDateChangeListener = new OnDateChangeListener() {
        @Override
        public void onDateChange() {
            LogUtil.d(TAG, "onDateChange");
            mHandler.removeMessages(UPDATE_MAP);
            mHandler.sendEmptyMessage(UPDATE_MAP);
        }
    };

    private OnNetworkChangeListener onNetworkChangeListener = new OnNetworkChangeListener() {
        @Override
        public void onNetworkChange() {
            LogUtil.d(TAG, "onNetworkChange");
            mHandler.removeMessages(UPDATE_MAP);
            mHandler.sendEmptyMessage(UPDATE_MAP);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_view_recover:
                gotoMapAuto();
                break;
            default:
                break;
        }

    }

    private void gotoMapAuto() {
        LogUtil.d(TAG, "go to map auto");
        Uri uri = Uri.parse("codriver://");
        Intent intent = new Intent(BAIDU_NAVIAUTO_START_ACTIVITY_ACTION, uri);
        LauncherUtil.startActivitySafely(intent);
    }

    private BaiduMap.OnMapRenderCallback onMapRenderCallback = new BaiduMap.OnMapRenderCallback() {
        @Override
        public void onMapRenderFinished() {
            LogUtil.i(TAG, "onMapRenderFinished");
            mProgressRl.setVisibility(View.GONE);
            mHandler.removeMessages(UPDATE_MAP);
            isLoadMap = true;
        }
    };

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_MAP:
                    mProgressRl.setVisibility(View.VISIBLE);
                    initMap();
                    if (!isLoadMap) {
                        mHandler.removeMessages(UPDATE_MAP);
                        mHandler.sendEmptyMessageDelayed(UPDATE_MAP, 8000);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
