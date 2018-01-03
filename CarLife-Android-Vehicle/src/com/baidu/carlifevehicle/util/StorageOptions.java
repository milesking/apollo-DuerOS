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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

/**
 * find all storage
 */
@TargetApi(9)
public class StorageOptions {
    public static final String EXTERNAL_SD_CARD = "外置存储卡";

    public static final String INTERNAL_STORAGE = "内置存储卡";

    private static final String DEV_MOUNT = "dev_mount";

    private static final String SYSTEM_ETC_VOLD_FSTAB = "/system/etc/vold.fstab";

    private static final String DEV_BLOCK_VOLD = "/dev/block/vold/";

    private static final String PROC_MOUNTS = "/proc/mounts";

    private static String MNT_SDCARD = "";

    private static ArrayList<String> mMounts = new ArrayList<String>();

    private static ArrayList<String> mVold = new ArrayList<String>();

    public static String[] labels;

    public static String[] paths;

    public static String[] sizes;

    public static int count = 0;

    public static void determineStorageOptions(Context context) {
        MNT_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

        // pre-install
        boolean ret = false;

        if (Build.VERSION.SDK_INT >= 14) {
            ret = getStoragePaths(context);
        }

        if (!ret) {
            // pre-install - end

            readMountsFile();

            readVoldFile();

            compareMountsWithVold();

            testAndCleanMountsList();

            setProperties();
        }
        // pre-install - end
    }

    /**
     * scan /proc/mounts file，lookup /dev/block/vold and get sdcard path /dev/block/vold/179:1
     * /mnt/sdcard vfat
     * rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015
     * ,fmask=0602,dmask
     * =0602,allow_utime=0020,codepage=cp437,iocharset=iso8859-1
     * ,shortname=mixed,utf8,errors=remount-ro 0 0
     */
    private static void readMountsFile() {
        // /mnt/sdcard as default path
        mMounts.add(MNT_SDCARD);

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(PROC_MOUNTS), "UTF-8");
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith(DEV_BLOCK_VOLD)) {
                    line = line.replace('\t', ' ');
                    String[] lineElements = line.split(" ");
                    if ( null != lineElements && 1 < lineElements.length ) {
                        String element = lineElements[1];

                        if (!element.equals(MNT_SDCARD)) {
                            mMounts.add(element);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("", e.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
                scanner = null;
            }
        }
    }

    /**
     * scan '/system/etc/vold.fstab'，find dev_mount and parse it，get dev_mount sdcard
     * /mnt/sdcard 1 /devices/platform/s3c-sdhci.0/mmc_host/mmc0
     */
    private static void readVoldFile() {
        mVold.add(MNT_SDCARD);
        File file = new File(SYSTEM_ETC_VOLD_FSTAB);
        if (!file.exists()) {
            return;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith(DEV_MOUNT)) {
                    line = line.replace('\t', ' ');
                    String[] lineElements = line.split(" ");
                    if ( null != lineElements && 2 < lineElements.length ) {
                        String element = lineElements[2];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }
                        // skip if it is default path
                        if (!element.equals(MNT_SDCARD)) {
                            mVold.add(element);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("", e.toString());
        } finally {
            if (scanner != null) {
                scanner.close();
                scanner = null;
            }
        }
    }

    private static void compareMountsWithVold() {
        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            if (!mVold.contains(mount)) {
                mMounts.remove(i--);
            }
        }
        mVold.clear();
    }

    private static void testAndCleanMountsList() {
        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);
            File root = new File(mount);
            if (!root.exists() || !root.isDirectory() || !root.canWrite()) {
                mMounts.remove(i--);
            }
        }
    }

    @TargetApi(9)
    private static void setProperties() {
        ArrayList<String> mLabels = new ArrayList<String>();
        ArrayList<String> mSizes = new ArrayList<String>();

        if (mMounts.size() > 0) {
            if (Build.VERSION.SDK_INT < 9) {
                mLabels.add("Auto");
            }
            mLabels.add(INTERNAL_STORAGE);
            mSizes.add(getAvailaleSize(mMounts.get(0)));
            if (mMounts.size() > 1) {
                for (int i = 1; i < mMounts.size(); i++) {
                    mLabels.add(EXTERNAL_SD_CARD);
                    mSizes.add(getAvailaleSize(mMounts.get(i)));
                }
            }
        }

        labels = new String[mLabels.size()];
        mLabels.toArray(labels);

        paths = new String[mMounts.size()];
        mMounts.toArray(paths);

        sizes = new String[mMounts.size()];
        mSizes.toArray(sizes);

        count = Math.min(labels.length, paths.length);
        mMounts.clear();
    }

    private static String getAvailaleSize(String path) {
        String strSize = "未知大小";
        try {
            StatFs stat = new StatFs(path);

            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();

            long nSize = availableBlocks * blockSize;

            java.text.DecimalFormat df = new java.text.DecimalFormat();
            
            if (nSize < 1024) {
                strSize = nSize + "B";
            } else if (nSize < 1048576) {
                // 1024*1024
                df.applyPattern("0");
                double d = (double) nSize / 1024;
                strSize = df.format(d) + "K";
            }
            else if (nSize < 1073741824) {
                // 1024*1024*1024
                df.applyPattern("0.0");
                double d = (double) nSize / 1048576;
                strSize = df.format(d) + "M";
            }
            else { // >1G
                df.applyPattern("0.0");
                double d = (double) nSize / 1073741824;
                strSize = df.format(d) + "G";
            }
        } catch (IllegalArgumentException e) {
            LogUtil.e("", e.toString());
        }
        return strSize;
    }

    private static boolean getStoragePathsV1(Context context) {
        paths = new String[1];
        paths[0] = Environment.getExternalStorageDirectory().getAbsolutePath();
        return true;
    }
    
    private static boolean getStoragePathsV2(Context context) {
        Object manager = context.getSystemService("storage");
        if (manager != null) {
            Class<?> clsStorageVolume = null;
            Method mthdGetVolumeList = null;
            Method mthdGetVolumeState = null;
            Method mthdGetPath = null;
            Method mthdIsRemovable = null;
            Object[] volumes = null;

            ArrayList<String> internalList = new ArrayList<String>();
            ArrayList<String> externalList = new ArrayList<String>();
            ArrayList<String> labelList = new ArrayList<String>();
            ArrayList<String> sizeList = new ArrayList<String>();

            try {
                clsStorageVolume = Class.forName("android.os.storage.StorageVolume");
                mthdGetVolumeList = manager.getClass().getMethod("getVolumeList");
                mthdGetVolumeState = manager.getClass().getMethod("getVolumeState",
                        String.class);
                mthdIsRemovable = clsStorageVolume.getMethod("isRemovable");
                mthdGetPath = clsStorageVolume.getMethod("getPath");

                volumes = (Object[]) mthdGetVolumeList.invoke(manager);

                boolean removable = false;

                for (int i = 0; i < volumes.length; i++) {
                    String path = (String) mthdGetPath.invoke(volumes[i]);
                    removable = (Boolean) mthdIsRemovable.invoke(volumes[i]);

                    if (path != null && path.length() > 0) {
                        String state = (String) mthdGetVolumeState.invoke(manager, path);

                        if (state != null && state.equals("mounted")) {
                            if (removable) {
                                externalList.add(path);
                            }
                            else {
                                internalList.add(path);
                            }
                        }
                    }
                }

                for (int i = 0; i < internalList.size(); i++) {
                    mMounts.add(internalList.get(i));
                    labelList.add(INTERNAL_STORAGE);
                    sizeList.add(getAvailaleSize(internalList.get(i)));
                }

                for (int i = 0; i < externalList.size(); i++) {
                    mMounts.add(externalList.get(i));
                    labelList.add(EXTERNAL_SD_CARD);
                    sizeList.add(getAvailaleSize(externalList.get(i)));
                }

                labels = new String[labelList.size()];
                labelList.toArray(labels);

                paths = new String[mMounts.size()];
                mMounts.toArray(paths);

                sizes = new String[mMounts.size()];
                sizeList.toArray(sizes);

                count = Math.min(labels.length, paths.length);
                mMounts.clear();

                return true;
            } catch (ClassNotFoundException ex) {
                LogUtil.e("", ex.toString());
            } catch (NoSuchMethodException ex) {
                LogUtil.e("", ex.toString());
            } catch (IllegalArgumentException ex) {
                LogUtil.e("", ex.toString());
            } catch (IllegalAccessException ex) {
                LogUtil.e("", ex.toString());
            } catch (InvocationTargetException ex) {
                LogUtil.e("", ex.toString());
            }
        }
        else {
            return getStoragePathsV1(context);
        }
        return false;
    }
    
    private static boolean getStoragePathsV3(Context context) {

        ArrayList<String> internalList = new ArrayList<String>();
        ArrayList<String> externalList = new ArrayList<String>();
        ArrayList<String> labelList = new ArrayList<String>();
        ArrayList<String> sizeList = new ArrayList<String>();
        Object manager = context.getSystemService("storage");
        if (manager != null) {
            Class<?> clsStorageVolume = null;
            Method mthdGetVolumeList = null;
            Method mthdGetVolumeState = null;
            Method mthdGetPath = null;
            Method mthdIsRemovable = null;
            Object[] volumes = null;

            try {
                clsStorageVolume = Class.forName("android.os.storage.StorageVolume");
                mthdGetVolumeList = manager.getClass().getMethod("getVolumeList");
                mthdGetVolumeState = manager.getClass().getMethod("getVolumeState",
                        String.class);
                mthdIsRemovable = clsStorageVolume.getMethod("isRemovable");

                mthdGetPath = clsStorageVolume.getMethod("getPath");

                volumes = (Object[]) mthdGetVolumeList.invoke(manager);

                boolean removable = false;

                for (int i = 0; i < volumes.length; i++) {
                    String path = (String) mthdGetPath.invoke(volumes[i]);
                    removable = (Boolean) mthdIsRemovable.invoke(volumes[i]);

                    if (path != null && path.length() > 0) {
                        String state = (String) mthdGetVolumeState.invoke(manager, path);

                        if (state != null && state.equals("mounted") && !removable) {
                            internalList.add(path);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                LogUtil.e("", ex.toString());
            } catch (NoSuchMethodException ex) {
                LogUtil.e("", ex.toString());
            } catch (IllegalArgumentException ex) {
                LogUtil.e("", ex.toString());
            } catch (IllegalAccessException ex) {
                LogUtil.e("", ex.toString());
            } catch (InvocationTargetException ex) {
                LogUtil.e("", ex.toString());
            }
        }

        File[] externalStorageFiles = null;
        try {
            Method mthdGetExternalFilesDirs = Context.class.getMethod("getExternalFilesDirs", String.class);
            if (mthdGetExternalFilesDirs == null) {
                return false;
            }

            externalStorageFiles = (File[]) mthdGetExternalFilesDirs.invoke(context, "");
            if (externalStorageFiles == null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (externalStorageFiles == null) {
            return true;
        }

        for (int i = 0; i < externalStorageFiles.length && null != externalStorageFiles[i] ; i++) {
            String path = externalStorageFiles[i].getAbsolutePath();
            if ( null == path ) {
                continue;
            }
            boolean isPrimary = false;
            for (String pathInPaths : internalList) {
                if (path.startsWith(pathInPaths)) {
                    isPrimary = true;
                    break;
                }
            }
            if (!isPrimary && path.indexOf(context.getPackageName()) != -1) {
                externalList.add(path);
            }
        }
        
        for (int i = 0; i < internalList.size(); i++) {
            mMounts.add(internalList.get(i));
            labelList.add(INTERNAL_STORAGE);
            sizeList.add(getAvailaleSize(internalList.get(i)));
        }

        for (int i = 0; i < externalList.size(); i++) {
            mMounts.add(externalList.get(i));
            labelList.add(EXTERNAL_SD_CARD);
            sizeList.add(getAvailaleSize(externalList.get(i)));
        }

        labels = new String[labelList.size()];
        labelList.toArray(labels);

        paths = new String[mMounts.size()];
        mMounts.toArray(paths);

        sizes = new String[mMounts.size()];
        sizeList.toArray(sizes);

        count = Math.min(labels.length, paths.length);
        mMounts.clear();
        
        return true;
    }
    
    private static boolean getStoragePaths(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 9) {
            return getStoragePathsV1(context);
        }
        else if (Build.VERSION.SDK_INT < 19) {
            return getStoragePathsV2(context);
        } else {
            return getStoragePathsV3(context);
        }
    }
}

