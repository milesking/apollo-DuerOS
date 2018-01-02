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

import java.util.Iterator;

import com.baidu.carlife.protobuf.CarlifeCarGpsProto.CarlifeCarGps;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.util.LogUtil;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Message;

public class GPSListenersMaker {
    private static final int GPS_FORMAT_WGS84 = 1;
    private static final int GPS_FORMAT_GCJ02 = 2;
    private static final String TAG = "GPSListeners";
    private static int mSatellitesNum = 0;
    private CarlifeCarGps.Builder mGpsbuilder = CarlifeCarGps.newBuilder();
    private CarlifeCmdMessage command = new CarlifeCmdMessage(true);
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private GpsStatus.Listener mStatusListener;
    private int gpsFormat = 1;

    public GPSListenersMaker(LocationManager locationManager) {
        this.mLocationManager = locationManager;
        // listen location data
        mLocationListener = new CarLifeLocationListener();
        // listen gps status
        mStatusListener = new CarLifeGPSStatusListener();
    }
    
    public LocationListener getLocationListener() {
        return mLocationListener;
    }
    
    public GpsStatus.Listener getGpsStatusListener() {
        return mStatusListener;
    }

    public void setGPSFormat(int format) {
        gpsFormat = format;
    }

    private class CarLifeGPSStatusListener implements GpsStatus.Listener {
        
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // first time fix
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                // satellite status changed
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        iters.next();
                        count++;
                    }
                    mSatellitesNum = count;
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    break;
                default:
                    break;
            }
        }
    }

    private class CarLifeLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                LogUtil.e(TAG, "GPS location == null");
                return;
            }

            mGpsbuilder = CarlifeCarGps.newBuilder();
            // WGS-84
            if (gpsFormat == GPS_FORMAT_WGS84) {
                mGpsbuilder.setLatitude((int) (location.getLatitude() * 1000000));
                mGpsbuilder.setLongitude((int) (location.getLongitude() * 1000000));
            } else if (gpsFormat == GPS_FORMAT_GCJ02) {
                // GCJ-02 need to convert
                double[] coordinator = CoordinateTransformUtil.wgs84togcj02(location.getLongitude(),
                        location.getLatitude());
                mGpsbuilder.setLatitude((int) (coordinator[1] * 1000000));
                mGpsbuilder.setLongitude((int) (coordinator[0] * 1000000));
            } else {
                // Invalid gps data format, just ignore
                return;
            }
            mGpsbuilder.setSpeed((int) (location.getSpeed() * 100));
            mGpsbuilder.setHeading((int) (location.getBearing() * 10));
            mGpsbuilder.setHeight((int) (location.getAltitude() * 10));
            mGpsbuilder.setAntennaState(0);
            mGpsbuilder.setDay(0);
            mGpsbuilder.setEastSpeed(0);
            mGpsbuilder.setFix(0);
            // HorizontalAccuracy
            mGpsbuilder.setHdop(0);
            mGpsbuilder.setYear(0);
            mGpsbuilder.setHorPosError(0);
            mGpsbuilder.setHrs(0);
            mGpsbuilder.setMin(0);
            mGpsbuilder.setMonth(0);
            mGpsbuilder.setNorthSpeed(0);
            // three-dimensional accuracy
            mGpsbuilder.setPdop((int) (location.getAccuracy() * 10));
            mGpsbuilder.setSatsUsed(mSatellitesNum);
            mGpsbuilder.setSatsVisible(mSatellitesNum);
            mGpsbuilder.setSec(0);
            mGpsbuilder.setSignalQuality(0);
            // vertical accuracy
            mGpsbuilder.setVdop(0);
            mGpsbuilder.setVertPosError(0);
            mGpsbuilder.setVertSpeed(0);
            CarlifeCarGps gpsPb = mGpsbuilder.build();
            command.setData(gpsPb.toByteArray());
            command.setLength(gpsPb.getSerializedSize());
            command.setServiceType(CommonParams.MSG_CMD_CAR_GPS);
            Message msgTmp =
                    Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
            ConnectClient.getInstance().sendMsgToService(msgTmp);
            LogUtil.d(TAG, "report GPS data...");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
                default:
                    break;
            }
        }

        public void onProviderEnabled(String provider) {
            Location location = mLocationManager.getLastKnownLocation(provider);
        }

        public void onProviderDisabled(String provider) {
        }
    }
}
