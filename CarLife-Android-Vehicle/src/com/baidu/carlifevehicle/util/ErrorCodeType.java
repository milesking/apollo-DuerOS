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
package com.baidu.carlifevehicle.util;
/**
 * connect error code is represented by string of 8 char:<br/>
 * char 0: 0 for mobile device, 1 for head unit <br/>
 * char 1, 2:<br/>
 *           01: connect module<br/>
 *           02: video module<br/>
 *           03: audio module<br/>
 *           04: vr module<br/>
 *           05: touch module<br/>
 *           06: cmd module<br/>
 *           07: others<br/>
 * char 3, 4:<br/>
 *           01: ADB<br/>
 *           02: AOA<br/>
 *           03: WIFI<br/>
 *           04: NCM<br/>
 * char 5, 6, 7: for concrete error code define by module
 */
public class ErrorCodeType {
    public static final String CARLIFE_CONNECT_CONNECTED = "10100000";

    /**
     * ADB connecting
     */
    public static final String ADB_ERROR_IS_CONNECTING = "10101001";
    /**
     * ADB connected
     */
    public static final String ADB_ERROR_IS_CONNECTED = "10101002";
    /**
     * ADB socket create error
     */
    public static final String ADB_ERROR_SOCKET_EXCEPTION = "10101003";
    /**
     * ADB get device info
     */
    public static final String ADB_ERROR_GET_DEVICE = "10101004";
    /**
     * ADB execSocketForward
     */
    public static final String ADB_ERROR_SOCKET_FORWARD = "10101005";
    /**
     * ADB GET_SDK_VERSION_FAIL
     */
    public static final String ADB_ERROR_GET_SDK_VERSION_FAIL = "10101006";
    /**
     * ADB GET_SDK_VERSION_FAIL_LOW
     */
    public static final String ADB_ERROR_GET_SDK_VERSION_FAIL_LOW = "10101007";
    /**
     * ADB execStartBdim
     */
    public static final String ADB_ERROR_START_BDIM = "10101008";
    /**
     * ADB execStartCalife start error
     */
    public static final String ADB_ERROR_START_FAIL = "10101009";
    /**
     * ADB execStartCarlife connect error
     */
    public static final String ADB_ERROR_START__CONNECT_FAIL = "10101010";
    /**
     * ADB execKillBdsc
     */
    public static final String ADB_ERROR_KILL_BDSC = "10101011";
    /**
     * ADB execStartBdsc
     */
    public static final String ADB_ERROR_START__BDSC = "10101012";



    /**
     * AOA connecting
     */
    public static final String AOA_ERROR_IS_CONNECTING = "10102001";
    /**
     * AOA connected
     */
    public static final String AOA_ERROR_IS_CONNECTED = "10102002";
    /**
     * AOA socket create error
     */
    public static final String AOA_ERROR_SOCKET_EXCEPTION = "10102003";

    /**
     * ios wifi connecting
     */
    public static final String IOS_WIFI_ERROR_IS_CONNECTING = "10103001";
    /**
     * ios wifi connected
     */
    public static final String IOS_WIFI_ERROR_IS_CONNECTED = "10103002";
    /**
     * ios wifi socket create error
     */
    public static final String IOS_WIFI_ERROR_SOCKET_EXCEPTION = "10103003";



    /**
     * NCM connecting
     */
    public static final String IOS_NCM_ERROR_IS_CONNECTING = "10104001";
    /**
     * NCM connected
     */
    public static final String IOS_NCM_ERROR_IS_CONNECTED = "10104002";
    /**
     * NCM socket create error
     */
    public static final String IOS_NCM_ERROR_SOCKET_EXCEPTION = "10104003";
    /**
     * NCM resolve ip fail
     */
    public static final String IOS_NCM_ERROR_RESOLVE_IP = "10104004";
    /**
     * NCM ip unreachable
     */
    public static final String IOS_NCM_ERROR_IP_INACCESSIBLE = "10104005";
    /**
     * NCM ipv6 socket connect error
     */
    public static final String IOS_NCM_ERROR_CONNECT_SOCKET = "10104006";
    /**
     * NCM bonjour discovery error
     */
    public static final String IOS_NCM_ERROR_DISCOVERY = "10104007";
    /**
     * NCM connect thread error
     */
    public static final String IOS_NCM_ERROR_EXCEPTION = "10104008";
    


}
