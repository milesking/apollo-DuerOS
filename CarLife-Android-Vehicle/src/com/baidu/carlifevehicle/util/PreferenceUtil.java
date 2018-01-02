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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.baidu.carlifevehicle.CommonParams;

public class PreferenceUtil {

    private static final String TAG = "PreferenceUtil";

    private static PreferenceUtil mInstance = null;
    private SharedPreferences mPreferences = null;
    private Editor mEditor = null;

    private SharedPreferences mJarPreferences = null;
    private Editor mJarEditor = null;
    public static final String FIRST_INSTALL_KEY = "first_install_key";
    public static final String CONNECT_TYPE_KEY = "connect_type_key";

    public static synchronized PreferenceUtil getInstance() {
        if (null == mInstance) {
            synchronized (PreferenceUtil.class) {
                if (null == mInstance) {
                    mInstance = new PreferenceUtil();
                }
            }
        }
        return mInstance;
    }

    private PreferenceUtil() {

    }

    public void init(final Context context) {
        if (context == null) {
            return;
        }
        mPreferences = context.getSharedPreferences(CommonParams.CARLIFE_NORMAL_PREFERENCES,
                Activity.MODE_PRIVATE);
        mEditor = mPreferences.edit();

        Context carlifeContext = null;
        try {
            carlifeContext = context.createPackageContext(context.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);
            mJarPreferences = carlifeContext.getSharedPreferences(
                    CommonParams.CONNECT_STATUS_SHARED_PREFERENCES, Context.MODE_WORLD_WRITEABLE
                            | Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
            mJarEditor = mJarPreferences.edit();
        } catch (Exception e) {
            LogUtil.e(TAG, "init jar sp fail");
            e.printStackTrace();
        }
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public SharedPreferences getPreferences(String sp) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES)) {
            return mJarPreferences;
        }
        return null;
    }

    public Map<String, ?> getAll() {
        if (mPreferences == null) {
            return null;
        }
        return mPreferences.getAll();
    }

    public Map<String, ?> getAll(String sp) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getAll();
        }
        return null;
    }

    public boolean contains(String key) {
        if (mPreferences == null) {
            return false;
        }
        return mPreferences.contains(key);
    }

    public boolean contains(String sp, String key) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.contains(key);
        }
        return false;
    }

    public boolean getBoolean(String key, boolean defValue) {
        if (mPreferences == null) {
            return false;
        }
        return mPreferences.getBoolean(key, defValue);
    }

    public boolean getBoolean(String sp, String key, boolean defValue) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getBoolean(key, defValue);
        }
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        if (mPreferences == null) {
            return defValue;
        }
        return mPreferences.getFloat(key, defValue);
    }

    public float getFloat(String sp, String key, float defValue) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getFloat(key, defValue);
        }
        return defValue;
    }

    public int getInt(String key, int defValue) {
        if (mPreferences == null) {
            return defValue;
        }
        return mPreferences.getInt(key, defValue);
    }

    public int getInt(String sp, String key, int defValue) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getInt(key, defValue);
        }
        return defValue;
    }

    public long getLong(String key, long defValue) {
        if (mPreferences == null) {
            return defValue;
        }
        return mPreferences.getLong(key, defValue);
    }

    public long getLong(String sp, String key, long defValue) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getLong(key, defValue);
        }
        return defValue;
    }

    public String getString(String key, String defValue) {
        if (mPreferences == null) {
            return defValue;
        }
        return mPreferences.getString(key, defValue);
    }

    public String getString(String sp, String key, String defValue) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            return mJarPreferences.getString(key, defValue);
        }
        return defValue;
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (mPreferences == null) {
            return;
        }
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void registerOnSharedPreferenceChangeListener(String sp,
                                                         OnSharedPreferenceChangeListener listener) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            mJarPreferences.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (mPreferences == null) {
            return;
        }
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(String sp,
                                                           OnSharedPreferenceChangeListener listener) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarPreferences != null) {
            mJarPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    public boolean putBoolean(String key, boolean b) {
        if (mEditor == null) {
            return false;
        }
        mEditor.putBoolean(key, b);
        return mEditor.commit();
    }

    public boolean putBoolean(String sp, String key, boolean b) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.putBoolean(key, b);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean putInt(String key, int i) {
        if (mEditor == null) {
            return false;
        }
        mEditor.putInt(key, i);
        return mEditor.commit();
    }

    public boolean putInt(String sp, String key, int i) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.putInt(key, i);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean putFloat(String key, float f) {
        if (mEditor == null) {
            return false;
        }
        mEditor.putFloat(key, f);
        return mEditor.commit();
    }

    public boolean putFloat(String sp, String key, float f) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.putFloat(key, f);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean putLong(String key, long l) {
        if (mEditor == null) {
            return false;
        }
        mEditor.putLong(key, l);
        return mEditor.commit();
    }

    public boolean putLong(String sp, String key, long l) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.putLong(key, l);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean putString(String key, String s) {
        if (mEditor == null) {
            return false;
        }
        mEditor.putString(key, s);
        return mEditor.commit();
    }

    public boolean putString(String sp, String key, String s) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.putString(key, s);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean remove(String key) {
        if (mEditor == null) {
            return false;
        }
        mEditor.remove(key);
        return mEditor.commit();
    }

    public boolean remove(String sp, String key) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            mJarEditor.remove(key);
            return mJarEditor.commit();
        }
        return false;
    }

    public boolean putObjectList(String sp, HashMap<String, Object> map) {
        if (sp.equals(CommonParams.CONNECT_STATUS_SHARED_PREFERENCES) && mJarEditor != null) {
            if (map != null && map.size() > 0) {
                Iterator<String> iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    Object value = map.get(key);
                    if (value instanceof String) {
                        mJarEditor.putString(key, (String) value);
                    } else if (value instanceof Integer) {
                        mJarEditor.putInt(key, (Integer) value);
                    } else if (value instanceof Boolean) {
                        mJarEditor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Long) {
                        mJarEditor.putLong(key, (Long) value);
                    } else if (value instanceof Float) {
                        mJarEditor.putFloat(key, (Integer) value);
                    }
                }
                return mJarEditor.commit();
            }
        }
        return false;
    }
}
