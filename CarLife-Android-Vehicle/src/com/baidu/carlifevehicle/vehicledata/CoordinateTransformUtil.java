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

/**
 * util for coordinate transform
 *
 * @author geosmart
 */
public class CoordinateTransformUtil {
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    /**
     * π
     */
    static double pi = 3.1415926535897932384626;
    /**
     * Semi-major Axis
     */
    static double a = 6378245.0;
    /**
     * flattening
     */
    static double ee = 0.00669342162296594323;

    /**
     * BD-09 to WGS84
     *
     * @param lng BD-09 longitude
     * @param lat BD-09 latitude
     *
     * @return WGS84 coordinates
     */
    public static double[] bd09towgs84(double lng, double lat) {
        double[] gcj = bd09togcj02(lng, lat);
        double[] wgs84 = gcj02towgs84(gcj[0], gcj[1]);
        return wgs84;
    }

    /**
     * WGS84 to BD-09
     *
     * @param lng WGS84 longitude
     * @param lat WGS84 latitude
     *
     * @return BD-09 coordinates
     */
    public static double[] wgs84tobd09(double lng, double lat) {
        double[] gcj = wgs84togcj02(lng, lat);
        double[] bd09 = gcj02tobd09(gcj[0], gcj[1]);
        return bd09;
    }

    /**
     * GCJ-02 to BD-09
     *
     * @param lng GCJ02 longitude
     * @param lat GCJ02 latitude
     *
     * @return BD-09 coordinates
     *
     */
    public static double[] gcj02tobd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_pi);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[] {bdLng, bdLat};
    }

    /**
     * BD-09 to GCJ-02
     *
     * @param bdLat BD-09 latitude
     * @param bdLon BD-09 longitude
     * @return GCJ-02 coordinates
     *
     */
    public static double[] bd09togcj02(double bdLon, double bdLat) {
        double x = bdLon - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double ggLng = z * Math.cos(theta);
        double ggLat = z * Math.sin(theta);
        return new double[] {ggLng, ggLat};
    }

    /**
     * WGS84 to GCJ02
     *
     * @param lng WGS84 longitude
     * @param lat WGS84 latitude
     *
     * @return WGS84 coordinates
     */
    public static double[] wgs84togcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[] {lng, lat};
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] {mglng, mglat};
    }

    /**
     * GCJ02 to GPS84
     *
     * @param lng GCJ02 longitude
     * @param lat GCJ02 latitude
     *
     * @return WGS84 coordinates
     */
    public static double[] gcj02towgs84(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[] {lng, lat};
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] {lng * 2 - mglng, lat * 2 - mglat};
    }

    /**
     * transform latitude
     *
     * @param lng longitude
     * @param lat latitude
     *
     * @return the transformed latitude
     */
    public static double transformlat(double lng, double lat) {
        double ret =
                -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * pi) + 320 * Math.sin(lat * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * transform longitude
     *
     * @param lng longitude
     * @param lat latitude
     *
     * @return the transformed longitude
     */
    public static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * pi) + 40.0 * Math.sin(lng / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * pi) + 300.0 * Math.sin(lng / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * whether out of china
     *
     * @param lng longitude
     * @param lat latitude
     *
     * @return true for out of china, false for in china
     */
    public static boolean outOfChina(double lng, double lat) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        } else if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }
}
