package com.qiao.voice.activity;

import android.app.Activity;
import android.os.Bundle;

import com.jaeger.library.StatusBarUtil;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetStatusBar();
    }

    public void SetStatusBar(){
        StatusBarUtil.setColor(this, 0x00ffffff, 0);
        StatusBarUtil.setDarkMode(this);
    }
}
