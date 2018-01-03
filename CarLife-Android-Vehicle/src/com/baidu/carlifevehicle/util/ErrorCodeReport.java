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

import java.io.File;

import com.baidu.carlife.protobuf.CarlifeErrorCodeProto.CarlifeErrorCode;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;

import android.content.Context;
import android.os.Message;

public class ErrorCodeReport {

    public static final String TAG = "ErrorCodeReport";
    public static final String ERROR_FILE_NAME = "errorfile.txt";
    public static String errorCode = null;
    public static final int ERROR_CODE_LENGTH = 8;
    private static ErrorCodeReport mInstance = null;

    public File errorFilePath = null;
    public String errorFile = null;
    private FileOperation mFileOperation = null;

    public static ErrorCodeReport getInterface() {
        if (null == mInstance) {
            synchronized (ErrorCodeReport.class) {
                if (null == mInstance) {
                    mInstance = new ErrorCodeReport();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        LogUtil.v(TAG, "ErrorCodeReport init");
        errorFilePath = context.getFilesDir();
        if (null != errorFilePath) {
            LogUtil.v(TAG, "errorFilePath:" + errorFilePath);
            errorFile = errorFilePath + "/" + ERROR_FILE_NAME;
            mFileOperation = new FileOperation(errorFile);
            mFileOperation.init();
        }
    }

    public String errorCodeString() {
        String errorString = null;
        if (0 != mFileOperation.getFileSize()) {
            errorString = readErrorCodeFromeFile();
            clearErrorCodeFile();
        }
        if (null != errorCode) {
            if (null != errorString) {
                errorString = errorString + "," + errorCode;
            } else {
                errorString = errorCode;
            }
            errorCode = null;
        }
        return errorString;
    }

    public void sendErrorCode() {
        String errorCodeId = null;
        errorCodeId = errorCodeString();
        LogUtil.v(TAG, "sendErrorCode()" + errorCodeId);
        if (null == errorCodeId) {
            LogUtil.e(TAG, "errorCodeId is null");
            return;
        }
        try {
            CarlifeCmdMessage command = new CarlifeCmdMessage(true);
            command.setServiceType(CommonParams.MSG_CMD_ERROR_CODE);
            CarlifeErrorCode.Builder builder = CarlifeErrorCode.newBuilder();
            builder.setErrorCode(errorCodeId);

            CarlifeErrorCode errorId = builder.build();
            command.setData(errorId.toByteArray());
            command.setLength(errorId.getSerializedSize());
            Message msgTmp =
                    Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
            ConnectClient.getInstance().sendMsgToService(msgTmp);
        } catch (Exception e) {
            LogUtil.e(TAG, "sendErrorCode fail");
            e.printStackTrace();
        }
    }

    public boolean clearErrorCodeFile() {
        boolean fileBool = false;
        fileBool = mFileOperation.delFile();
        return fileBool;
    }

    /**
     * write error code to memory
     * @param errorId the error code defined in {@link ErrorCodeType}
     */
    public void writeErrorCode(String errorId) {
        long errorIdTime = 0;
        if (null != errorId) {
            errorIdTime = System.currentTimeMillis();
            errorId = errorId + "#" + String.valueOf(errorIdTime);
            LogUtil.v(TAG, "writeErrorCode  --->errorId: " + errorId);
            if (null == errorCode) {
                errorCode = errorId;
            } else {
                errorId = "," + errorId;
                errorCode = errorCode + errorId;
            }
        }
    }

    /**
     * write error code to file
     */
    public void writeErrorCodeToFile() {
        String errorCodeString = "";
        if (null != errorCode) {
            if (0 != mFileOperation.getFileSize()) {
                errorCodeString = ",";
            }
            errorCodeString = errorCodeString + errorCode;
            LogUtil.v(TAG, "writeErrorCodeToFile: " + errorCodeString);
            mFileOperation.writeFileByAppend(errorCodeString);
            errorCode = null;
        }
    }

    public String readErrorCodeFromeFile() {
        int length = 0;
        String errorCode = null;
        length = mFileOperation.getFileSize();

        errorCode = mFileOperation.readFileByChars(length);
        LogUtil.v(TAG, "readErrorCodeFromeFile:" + length + errorCode);
        return errorCode;
    }

}
