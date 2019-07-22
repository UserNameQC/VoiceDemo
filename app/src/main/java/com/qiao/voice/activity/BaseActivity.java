package com.qiao.voice.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.jaeger.library.StatusBarUtil;

public class BaseActivity extends Activity {

    public String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public boolean hasToken;
    public String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetStatusBar();
        requestPermission();
        initAccessToken();
    }

    public void SetStatusBar(){
        StatusBarUtil.setColor(this, 0x00ffffff, 0);
        StatusBarUtil.setDarkMode(this);
    }

    public void requestPermission() {
        for (int i = 0; i < permissions.length; i++) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, 100);
                }
            }
        }
    }

    /**
     * 以license文件方式初始化
     */
    public void initAccessToken() {
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                hasToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Log.e(TAG, "licence方式获取token失败" + error.getMessage());
            }
        }, getApplicationContext());
    }

    public boolean checkTokenStatus() {
        if (!hasToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasToken;
    }
}
