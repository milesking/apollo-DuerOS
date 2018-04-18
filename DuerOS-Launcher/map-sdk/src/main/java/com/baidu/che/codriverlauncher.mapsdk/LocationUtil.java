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

import java.text.DecimalFormat;

import com.baidu.che.codriverlauncher.util.INoProguard;
import com.baidu.che.codriverlauncher.util.LogUtil;
import com.baidu.che.codriverlauncher.util.SharePreferenceUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.CoordUtil;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.Point;

import android.content.Context;

/**
 * location util
 */
class LocationUtil implements BDLocationListener, INoProguard {
    private static final String TAG = "LocationUtil";

    private static final int SPAN_MIL = 2 * 1000; // ms
    private static final int SAFE_POSITION_DURATION_TIME = 2 * 60;
    private static final int SAFE_POSITION_DURATION_MS = SAFE_POSITION_DURATION_TIME * SPAN_MIL;

    private static final String KEY_LAST_LATITUDE = "last_latitude";
    private static final String KEY_LAST_LONGITUDE = "last_longitude";
    private static final String KEY_LAST_LATITUDE_BD09LL = "last_latitude_bd09ll";
    private static final String KEY_LAST_LONGITUDE_BD09LL = "last_longitude_bd09ll";

    private static final double DEFAULT_LATITUDE = 44.912733;
    private static final double DEFAULT_LONGITUDE = 111.403963;
    private static final double DEFAULT_LATITUDE_BD09LL = 44.912733;
    private static final double DEFAULT_LONGITUDE_BD09LL = 111.403963;

    private static LocationUtil mInstance;
    private Context mContext;
    private LocationClient mLocationClient;
    private BDLocation mLocationGCJ02;
    private BDLocation mLocationBD09LL;

    private OnLocationListener mOnLocationListener;

    private double mLastLatitude;
    private double mLastLongitude;
    private double mLastLatitudeBd09ll;
    private double mLastLongitudeBd09ll;
    private int mRequestLocCnt = 0;

    private LocationUtil() {

    }

    public static LocationUtil getInstance() {
        if (mInstance == null) {
            synchronized (LocationUtil.class) {
                if (mInstance == null) {
                    mInstance = new LocationUtil();
                    return mInstance;
                }
            }
        }

        return mInstance;
    }

    private void init(Context context) {
        mLocationClient = new LocationClient(context.getApplicationContext());
        mLocationClient.setLocOption(initLocationOption());
        mLocationClient.registerLocationListener(this);
    }

    /**
     * start localization
     */
    public void start(Context context) {
        mContext = context;
        if (mLocationClient == null) {
            init(context);
        }
        mLocationClient.start();
        getLastPosition();
    }

    /**
     * stop localization
     */
    public void stop() {
        if (mLocationClient == null) {
            return;
        }
        mLocationClient.unRegisterLocationListener(this);
        mLocationClient.stop();
    }

    public void safeLastPosition() {
        LogUtil.d(TAG, "safe position to shared_prefs");
        SharePreferenceUtil.setLong(mContext, KEY_LAST_LATITUDE, (long) (mLastLatitude * 1E6));
        SharePreferenceUtil.setLong(mContext, KEY_LAST_LONGITUDE, (long) (mLastLongitude * 1E6));
        SharePreferenceUtil.setLong(mContext, KEY_LAST_LATITUDE_BD09LL, (long) (mLastLatitudeBd09ll * 1E6));
        SharePreferenceUtil.setLong(mContext, KEY_LAST_LONGITUDE_BD09LL, (long) (mLastLongitudeBd09ll * 1E6));
    }

    public void getLastPosition() {
        mLastLatitude = SharePreferenceUtil.getLong(mContext, KEY_LAST_LATITUDE,
                (long) (DEFAULT_LATITUDE * 1E6));
        mLastLatitude = mLastLatitude / 1E6;
        mLastLongitude = SharePreferenceUtil.getLong(mContext, KEY_LAST_LONGITUDE,
                (long) (DEFAULT_LONGITUDE * 1E6));
        mLastLongitude = mLastLongitude / 1E6;

        mLastLatitudeBd09ll = SharePreferenceUtil.getLong(mContext, KEY_LAST_LATITUDE_BD09LL,
                (long) (DEFAULT_LATITUDE_BD09LL * 1E6));
        mLastLatitudeBd09ll = mLastLatitudeBd09ll / 1E6;
        mLastLongitudeBd09ll = SharePreferenceUtil.getLong(mContext, KEY_LAST_LONGITUDE_BD09LL,
                (long) (DEFAULT_LONGITUDE_BD09LL * 1E6));
        mLastLongitudeBd09ll = mLastLongitudeBd09ll / 1E6;
    }

    /**
     * result of the location is empty
     */
    public boolean isReady() {
        if (mLocationGCJ02 == null) {
            return false;
        }

        return true;
    }

    /**
     * get city name (it has closed this function in initLocationOption,
     * and should modify init params when use this method）
     */
    public String getCity() {
        if (mLocationGCJ02 != null && mLocationGCJ02.getCity() != null) {
            return mLocationGCJ02.getCity();
        } else {
            LogUtil.e(TAG, "mLocationGCJ02 is null or city is null!");
            return "";
        }
    }

    /**
     * latitude（gcj02 coordinate system）
     */
    public double getLatitude() {
        if (mLocationGCJ02 != null) {
            mLastLatitude = mLocationGCJ02.getLatitude();
        } else {
            LogUtil.e(TAG, "mLocationGCJ02 is null!");
        }

        LogUtil.d(TAG, "mLastLatitude = " + mLastLatitude);
        return mLastLatitude;
    }

    /**
     * longitude（gcj02 coordinate system）
     */
    public double getLongitude() {
        if (mLocationGCJ02 != null) {
            mLastLongitude = mLocationGCJ02.getLongitude();
        } else {
            LogUtil.e(TAG, "mLocationGCJ02 is null!");
        }

        LogUtil.d(TAG, "mLastLongitude = " + mLastLongitude);
        return mLastLongitude;
    }

    /**
     * calculate distance to target，bd09ll coordinate system
     *
     * @param lat
     * @param lng
     *
     * @return distance，unit:meter
     */
    public double calculateDistance(double lat, double lng) {
        LatLng from = new LatLng(getLatitude(), getLongitude());
        LatLng to = new LatLng(lat, lng);
        return getDistance(from, to);
    }

    /**
     * latitude（bd09ll coordinate system）
     */
    public double getLatitudeBd09ll() {
        if (mLocationBD09LL != null) {
            mLastLatitudeBd09ll = mLocationBD09LL.getLatitude();
        } else {
            LogUtil.e(TAG, "mLocationBD09LL is null!");
        }
        return mLastLatitudeBd09ll;
    }

    /**
     * longitude（bd09ll coordinate system）
     */
    public double getLongitudeBd09ll() {
        if (mLocationBD09LL != null) {
            mLastLongitudeBd09ll = mLocationBD09LL.getLongitude();
        } else {
            LogUtil.e(TAG, "mLocationBD09LL is null!");
        }

        return mLastLongitudeBd09ll;
    }

    /**
     * get speed
     * unit：km/h
     */
    public double getSpeed() {

        if (mLocationGCJ02 != null && mLocationGCJ02.getSpeed() >= 0) {
            return mLocationGCJ02.getSpeed();
        } else {
            return -1;
        }
    }

    /**
     * get altitude
     */
    public double getHeight() {

        if (mLocationGCJ02 != null && mLocationGCJ02.getAltitude() >= 0) {
            DecimalFormat df = new DecimalFormat("#.00");
            return Double.parseDouble(df.format(mLocationGCJ02.getAltitude()));
        } else {
            return -1;
        }
    }

    /**
     * get direction
     */
    public int getDirection() {

        if (mLocationGCJ02 != null && mLocationGCJ02.getDirection() >= 0) {
            return (int) mLocationGCJ02.getDirection();
        } else {
            return -1;
        }
    }

    /**
     * get city name
     */
    public double getRadius() {
        if (mLocationGCJ02 != null && mLocationGCJ02.getRadius() >= 0) {
            return mLocationGCJ02.getRadius();
        } else {
            return -1;
        }
    }

    private LocationClientOption initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        // optional,default is high accuracy. low power ,only on the device
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // optional，default is gcj02，later can return converted coordinate system type
        option.setCoorType("gcj02");
        // optional，default is 0，only location once,
        // it is work when inteval time between location request is greater than or equal to 1000ms
        option.setScanSpan(SPAN_MIL);
        // optional，default is false
        option.setIsNeedAddress(false);
        // optional，default is false,whether use gps
        option.setOpenGps(true);
        // optional，default is false，whether output GPS results acording to 1S1 frequency when gps is valid
        option.setLocationNotify(false);
        // optional，default is false，whether need the describe of the location
        option.setIsNeedLocationDescribe(false);
        // optional，default is false，whether need POI result which can get in BDLocation.getPoiList
        option.setIsNeedLocationPoiList(false);
        // optional，default is true，there is a service in the loaction sdk,
        // and it run in a independent process, whether kill the service when stop
        option.setIgnoreKillProcess(true);
        // optional，default is false，whether collect crash info
        option.SetIgnoreCacheException(false);
        // optional，default is false，weather filter the result of the gps simulation
        option.setEnableSimulateGps(true);

        return option;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) { // GPS Location
            LogUtil.d(TAG, "getLocType: TypeGpsLocation");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) { // network location
            LogUtil.d(TAG, "getLocType: TypeNetWorkLocation");
        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) { // offline location
            LogUtil.d(TAG, "getLocType: TypeOffLineLocation");
        } else {
            LogUtil.e(TAG, "getLocType: Type Error, type=" + bdLocation.getLocType());
            return;
        }

        mLocationGCJ02 = bdLocation;
        mLocationBD09LL = convertToBD09LL(bdLocation);

        mLastLongitude = bdLocation.getLongitude();
        mLastLatitude = bdLocation.getLatitude();
        mLastLongitudeBd09ll = mLocationBD09LL.getLongitude();
        mLastLatitudeBd09ll = mLocationBD09LL.getLatitude();
        if ((mRequestLocCnt++) == SAFE_POSITION_DURATION_TIME) {
            safeLastPosition();
            mRequestLocCnt = 0;
        }
        if (mOnLocationListener != null) {
            mOnLocationListener.onLocationChange(mLocationBD09LL.getLatitude(), mLocationBD09LL.getLongitude(),
                    mLocationBD09LL.getRadius(), mLocationBD09LL.getDirection());
        }
    }

    /**
     * transform to coordinate system
     *
     * @param bdLocation source coordinate：gcj02 coordinate system
     *
     * @return converted coordinate system：bd09ll coordinate system
     */
    private BDLocation convertToBD09LL(BDLocation bdLocation) {
        return LocationClient.getBDLocationInCoorType(bdLocation, BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
    }

    /**
     * transform to coordinate system
     *
     * @param bdLocation source coordinate：gcj02 coordinate system
     *
     * @return converted coordinate system：bd09mc coordinate system
     */
    private BDLocation convertToBD09MC(BDLocation bdLocation) {
        return LocationClient.getBDLocationInCoorType(bdLocation, BDLocation.BDLOCATION_GCJ02_TO_BD09);
    }

    public static double getDistance(LatLng var0, LatLng var1) {
        if (var0 != null && var1 != null) {
            Point var2 = CoordUtil.ll2point(var0);
            Point var3 = CoordUtil.ll2point(var1);
            return var2 != null && var3 != null ? CoordUtil.getDistance(var2, var3) : -1.0D;
        } else {
            return -1.0D;
        }
    }

    @Override
    public void onConnectHotSpotMessage(String arg0, int arg1) {
        LogUtil.d(TAG, "onConnectHotSpotMessage:arg0=" + arg0 + ";arg1=" + arg1);

    }

    public void setCruiseChangeListener(OnLocationListener onLocationListener) {
        mOnLocationListener = onLocationListener;
    }
}
