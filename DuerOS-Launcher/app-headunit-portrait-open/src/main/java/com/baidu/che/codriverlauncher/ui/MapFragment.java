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

import static android.R.attr.direction;
import static android.content.Context.SENSOR_SERVICE;

import java.lang.ref.WeakReference;

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
import com.baidu.che.codriverlauncher.util.NetworkUtil;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

/**
 * show map
 */
public class MapFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "MapFragment";

    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    private SensorManager mSensorManager;
    private double mCurrentLat = 0;
    private double mCurrentLon = 0;
    private float mCurrentAccracy = 0;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private FrameLayout framelayout;
    private View mapRecover;
    private MyLocationData locData;
    private RelativeLayout mProgressRl;
    private boolean isLoadMap = false;

    // Set the surrounding color of the location
    private static final int accuracyCircleFillColor = 0;
    private static final int accuracyCircleStrokeColor = 0;

    // custom map
    private static final String FILE_PATH =
            Environment.getExternalStorageDirectory().toString() + "/" + "codriver_skin";
    private static final String FILE_CAROS_BLACK = "custom_config_black";
    private static final String FILE_PATH_NAME = FILE_PATH + "/" + FILE_CAROS_BLACK;

    public static final String BAIDU_NAVIAUTO_START_ACTIVITY_ACTION = "com.baidu.naviauto.action.START";
    public static final int UPDATE_MAP = 1;
    public static final int REMOVE_COMPASS = 2;
    public static final int UPDATE_COARSE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initFile();
        MapSdkWrapper.setCruiseChangeListener(onLocationListener);
        NetworkConnectChangedReceiver.setNetworkLtListener(onNetworkChangeListener);
        DateChangeReceiver.setDateChangeListener(onDateChangeListener);

        initView(view);
        if (mMapView != null) {
            mBaiduMap = mMapView.getMap();
        }
        if (NetworkUtil.isNetworkConnected(getActivity())) {
            initMap();
        } else if (mBaiduMap != null) {
            MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(mBaiduMap.getMaxZoomLevel() - 5);
            mBaiduMap.animateMapStatus(u);
        }
        return view;
    }

    private void initView(View view) {
        bearinglist = getActivity().getResources().getStringArray(R.array.bearing_list_text);

        mapRecover = view.findViewById(R.id.map_view_recover);
        mapRecover.setOnClickListener(this);

        framelayout = (FrameLayout) view.findViewById(R.id.bmap_frame);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.progress_rl);
        compassView = (ImageView) view.findViewById(R.id.im_compass);
        tvDushu = (TextView) view.findViewById(R.id.tv_degree);
        directionView = (TextView) view.findViewById(R.id.tv_direction);
        tvLa = (TextView) view.findViewById(R.id.tv_weidu);
        tvLn = (TextView) view.findViewById(R.id.tv_longitude);
        updateCompassView(compassView, bearing);
        // Get sensor management services
        mSensorManager = (SensorManager) LauncherApp.getInstance().getSystemService(SENSOR_SERVICE);

        // We need to make personalized configuration before MapView initialization
        if (FileUtil.isFileExists(FILE_PATH_NAME)) {
            MapView.setCustomMapStylePath(FILE_PATH_NAME);
        }
        mMapView = new MapView(getActivity());
        framelayout.addView(mMapView);
    }

    private void initMap() {
        // Set the enlargement and reduction button
        mMapView.setMapCustomEnable(true);
        View child = mMapView.getChildAt(1);
        // delete logo
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
            // ((ImageView)child).setImageDrawable(getResources().getDrawable(R.drawable.app));
        }
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);

        mBaiduMap = mMapView.getMap();
        // Open the location layer
        mBaiduMap.setMyLocationEnabled(true);
        // Close the traffic map
        mBaiduMap.setTrafficEnabled(false);
        mBaiduMap.setBaiduHeatMapEnabled(false);
        mBaiduMap.setIndoorEnable(false);
        mBaiduMap.setBuildingsEnabled(false);

        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
        // set custom logo
        // Modify it to custom marker
        mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.car_point);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker,
                accuracyCircleFillColor, accuracyCircleStrokeColor));

        mCurrentLat = MapSdkWrapper.getLatitudeBd09ll();
        mCurrentLon = MapSdkWrapper.getLongitudeBd09ll();
        // set the direction information that developers get，clockwise:0-360
        locData = new MyLocationData.Builder().accuracy(mCurrentAccracy)
                .direction((float) direction).latitude(mCurrentLat).longitude(mCurrentLon).build();
        mBaiduMap.setMyLocationData(locData);
        LatLng ll = new LatLng(mCurrentLat, mCurrentLon);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
    }

    @Override
    public void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();
        // Register listener for the system direction sensor
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        LogUtil.d(TAG, "onStop");
        // Unregister listener
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        unRegister();
        // Destroy the position when exiting
        MapSdkWrapper.setCruiseChangeListener(null);
        NetworkConnectChangedReceiver.setNetworkLtListener(null);
        DateChangeReceiver.setDateChangeListener(null);
        // Close location layer
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
            if (tvLa != null && tvLn != null) {
                Message message = Message.obtain();
                message.what = UPDATE_COARSE;
                Bundle bundle = new Bundle();
                bundle.putDouble("la", latBd09ll);
                bundle.putDouble("ln", lonBd09ll);
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
            if (mMapView == null || mBaiduMap == null || (doubleEqual(mCurrentLat - latBd09ll) && doubleEqual(
                    mCurrentLon - lonBd09ll))) {
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

        @Override
        public void onShowCompass() {
            mProgressRl.setVisibility(View.VISIBLE);
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
            mHandler.removeMessages(REMOVE_COMPASS);
            mHandler.removeMessages(UPDATE_MAP);
            if (NetworkUtil.isNetworkConnected(getActivity())) {
                mHandler.sendEmptyMessageDelayed(REMOVE_COMPASS, 1000);
            } else {
                mProgressRl.setVisibility(View.VISIBLE);
            }
            isLoadMap = true;
            initMapAcount = 0;
        }
    };

    Handler mHandler = new CustomHandler(this);

    private static class CustomHandler extends Handler {

        private WeakReference<MapFragment> weakReference;
        private MapFragment fragment;

        public CustomHandler(MapFragment fragment) {
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
                case UPDATE_MAP:
                    fragment.mProgressRl.setVisibility(View.VISIBLE);
                    fragment.initMap();
                    if (!fragment.isLoadMap && fragment.initMapAcount < 3) {
                        fragment.initMapAcount++;
                        fragment.mHandler.removeMessages(UPDATE_MAP);
                        fragment.mHandler.sendEmptyMessageDelayed(UPDATE_MAP, 8000);
                    }
                    break;
                case REMOVE_COMPASS:
                    fragment.mProgressRl.setVisibility(View.GONE);
                    break;
                case UPDATE_COARSE:
                    fragment.updateMyGpsView(msg.getData().getDouble("la"), msg.getData().getDouble("ln"));
                    break;
                default:
                    break;
            }
        }
    }

    private int bearing = 0;
    private ImageView compassView;
    private CompassBroadcast compassBroadcast = null;
    private String[] bearinglist;
    private TextView directionView;
    private int initMapAcount = 0;
    private TextView tvDushu;
    private TextView tvLa;
    private TextView tvLn;

    private class CompassBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.flyaudio.location_bearing".equals(intent.getAction())) {
                bearing = intent.getIntExtra("bearingvalue", 0);
                Log.d("CompassBroadcast", "bearing=" + bearing);
                if (compassView != null) {
                    updateCompassView(compassView, bearing);
                }
            }
        }
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.flyaudio.location_bearing");
        compassBroadcast = new CompassBroadcast();
        this.getActivity().registerReceiver(compassBroadcast, filter);
    }

    private void unRegister() {
        this.getActivity().unregisterReceiver(compassBroadcast);
    }

    private void updateCompassView(ImageView v, int changeBearing) {
        float degrees = changeBearing;
        String directionString = inWhichDir(changeBearing);
        v.setRotation(180 - degrees);
        if (directionView != null) {
            directionView.setText(directionString);
        }
        if (tvDushu != null) {
            tvDushu.setText((int) degrees + getActivity().getResources().getString(R.string.degree));
        }
    }

    private String inWhichDir(float degrees) {
        if (degrees < 0 || degrees > 360) {
            return "";
        }
        int ret = 0;
        if (337.5 <= degrees && degrees <= 360) {
            ret = 0; // N
        } else if (0 <= degrees && degrees <= 22.5) {
            ret = 0; // N
        } else if (22.5 < degrees && degrees <= 67.5) {
            ret = 1; // NE
        } else if (67.5 < degrees && degrees <= 112.5) {
            ret = 2; // E
        } else if (112.5 < degrees && degrees <= 157.5) {
            ret = 3; // SE
        } else if (157.5 < degrees && degrees <= 202.5) {
            ret = 4; // S
        } else if (202.5 < degrees && degrees <= 247.5) {
            ret = 5; // SW
        } else if (247.5 < degrees && degrees <= 292.5) {
            ret = 6; // W
        } else if (292.5 < degrees && degrees <= 337.5) {
            ret = 7; // NW
        } else {
            return "";
        }

        return bearinglist[(ret)];
    }

    private void updateMyGpsView(double la, double ln) {
        String du = getString(R.string.degree);
        String beiornan;
        String dongorxi;
        int weizheng = (int) Math.floor(la);
        int jingzheng = (int) Math.floor(ln);
        if (weizheng > 0) {
            beiornan = getResources().getString(R.string.map_north_latitude);
        } else {
            beiornan = getResources().getString(R.string.map_south_latitude);
        }

        if (jingzheng > 0) {
            dongorxi = getResources().getString(R.string.map_east_longitude);
        } else {
            dongorxi = getResources().getString(R.string.map_west_longitude);
        }
        int weifen = (int) Math.floor((la - (int) la) * 60);
        int jingfen = (int) Math.floor((ln - (int) ln) * 60);

        int weimiao = (int) Math.floor((((la - (int) la) * 60) - weifen) * 60);
        int jingmiao = (int) Math.floor((((ln - (int) ln) * 60) - jingfen) * 60);

        tvLa.setText(beiornan + weizheng + du + (weifen + "\'") + (weimiao + "\""));
        tvLn.setText(dongorxi + jingzheng + du + (jingfen + "\'") + (jingmiao + "\""));
    }

}
