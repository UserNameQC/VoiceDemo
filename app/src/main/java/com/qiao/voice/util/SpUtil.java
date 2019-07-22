package com.qiao.voice.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {

    private SharedPreferences preferences;

    public final static String URL = "URL";

    public SpUtil(Context context){
        preferences = context.getSharedPreferences("Voice", Context.MODE_PRIVATE);
    }

    public void putString(String key, String value){
        preferences.edit().putString(key, value).apply();
    }

    public String getString(String key){
        return preferences.getString(key, "https:m.baidu.com");
    }
}
