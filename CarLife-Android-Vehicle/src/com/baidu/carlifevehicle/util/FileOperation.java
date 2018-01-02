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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileOperation {
    public static final String TAG = "ErrorCodeReport";
    public File file = null;
    private String filePath = null;
    private Reader fileReader;
    private FileInputStream fileInputStream;
    private FileWriter writer;

    public FileOperation(String path) {
        LogUtil.v(TAG, "creat FileOperation class :" + path);
        if (null != path) {
            filePath = path;
        }
    }

    public void init() {
        try {
            if (null == filePath) {
                LogUtil.e(TAG, "filePath is null ");
                return;
            }
            file = new File(filePath);
            if (!file.exists()) {
                try {
                    LogUtil.v(TAG, "create file :" + file.getAbsolutePath());
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getFileSize() {
        int fileSize = 0;
        if (null == filePath) {
            LogUtil.e(TAG, "filePath is null ");
            return fileSize;
        }
        if (file.exists() && file.isFile()) {
            fileSize = (int) file.length();
        } else {
            LogUtil.v(TAG, "file doesn't exist or is not a file");
        }
        return fileSize;
    }

    public boolean delFile() {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public synchronized String readFileByChars(int length) {
        char[] tempBytes = new char[length];
        String errorCode;
        int charRead;
        if (null == filePath) {
            LogUtil.e(TAG, "filePath is null ");
            return null;
        }
        try {
            fileInputStream = new FileInputStream(file);
            fileReader = new InputStreamReader(fileInputStream);
            charRead = fileReader.read(tempBytes, 0, length);
            if (charRead != length) {
                LogUtil.e(TAG, "The length of the read  : " + charRead);
            }
            fileReader.close();
            fileInputStream.close();
            errorCode = new String(tempBytes);
            return errorCode;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileReader = null;
            fileInputStream = null;
        }
        return null;
    }

    public synchronized void writeFileByAppend(String content) {
        if (null == filePath) {
            LogUtil.e(TAG, "filePath is null ");
            return;
        }
        try {
            writer = new FileWriter(filePath, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer = null;
        }
    }

}
