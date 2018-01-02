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
package com.baidu.carlifevehicle.vehicledata;

import static com.baidu.carlifevehicle.vehicledata.CarDataManager.MODULE_GPS_DATA;

import com.baidu.carlife.protobuf.CarlifeVehicleInfoProto;
import com.baidu.carlifevehicle.R;
import com.baidu.carlifevehicle.util.CarlifeConfUtil;
import com.baidu.carlifevehicle.util.LogUtil;

import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Created by yangjie11 on 2016/11/14.
 */
public class GpsDataReporter extends CarDataBase {
    private static final String TAG = "CarDataGps";
    private static GpsDataReporter mInstance = null;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private GpsStatus.Listener mStatusListener;
    private Context mContext = null;
    public int gpsFormat = 1;

    public static GpsDataReporter getInstance() {

        if (mInstance == null) {
            synchronized (GpsDataReporter.class) {
                if (null == mInstance) {
                    mInstance = new GpsDataReporter();
                }
            }
        }
        return mInstance;
    }

    public boolean init(Context cx) {
        mContext = cx;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        LogUtil.d(TAG, "init GPS data reporter");
        if (mStatusListener == null || mLocationListener == null) {
            GPSListenersMaker gpsLM = new GPSListenersMaker(mLocationManager);
            mStatusListener = gpsLM.getGpsStatusListener();
            mLocationListener = gpsLM.getLocationListener();
            gpsFormat = CarlifeConfUtil.getInstance().getIntProperty(CarlifeConfUtil.KEY_INT_GPS_FORMAT);
            LogUtil.d(TAG, "gps reporter format = " + gpsFormat);
            gpsLM.setGPSFormat(gpsFormat);
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(mContext, R.string.gps_data_tips, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public CarlifeVehicleInfoProto.CarlifeVehicleInfo buildResponse() {
        CarlifeVehicleInfoProto.CarlifeVehicleInfo.Builder gpsCarDataBuilder =
                CarlifeVehicleInfoProto.CarlifeVehicleInfo.newBuilder();

        gpsCarDataBuilder.setModuleID(MODULE_GPS_DATA);
        // GCJ-02
        gpsCarDataBuilder.setFlag(gpsFormat);
        // report on change
        gpsCarDataBuilder.setFrequency(0);
        return gpsCarDataBuilder.build();

    }


    public static boolean isReportable() {
        if (CarlifeConfUtil.getInstance().getBooleanProperty(CarlifeConfUtil.KEY_BOOL_VEHICLE_GPS)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startReport(int freq) {
        LogUtil.d(TAG, "start GPS data reporter");
        mLocationManager.addGpsStatusListener(mStatusListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                mLocationListener);
    }

    @Override
    public void stopReport() {
        LogUtil.d(TAG, "stop GPS data reporter");
        mLocationManager.removeGpsStatusListener(mStatusListener);
        mLocationManager.removeUpdates(mLocationListener);

    }
}
