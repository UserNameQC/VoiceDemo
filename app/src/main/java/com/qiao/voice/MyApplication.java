package com.qiao.voice;

import android.app.Application;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

public class MyApplication extends Application {

    public String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //initOcr();
    }

    /**
     * 初始化OCR
     */
    public void initOcr(){
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
                Log.e(TAG, "获取Token 失败");
            }
        }, getApplicationContext());

    }
}
