package com.qiao.voice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.qiao.voice.api.mainApi;
import com.qiao.voice.util.FileUtil;
import com.qiao.voice.http.HttpUtil;
import com.qiao.voice.R;
import com.qiao.voice.databinding.ActivityMainBinding;
import com.qiao.voice.util.TopMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements mainApi {

    public final int REQUEST_CODE_ACCURATE_BASIC = 100;

    public boolean hasToken = false;
    public final String TAG = MainActivity.class.getSimpleName();
    public ActivityMainBinding binding;
    public HttpUtil httpUtil;
    public String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public StringBuffer buffer = new StringBuffer();

    public boolean isEditText = true;
    public boolean isToCamera = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        httpUtil = new HttpUtil(this);
        requestPermission();
        initAccessToken();
        initListener();
    }

    public void startCameraActivity(){
        if (!checkTokenStatus()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE_ACCURATE_BASIC);
    }

    public void initListener(){
        binding.headTitleLayout.headAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopMenu.showMenu(MainActivity.this, binding.headTitleLayout.headView);
                TopMenu.setItemListener(new TopMenu.ItemClickListener() {
                    @Override
                    public void takePhotos() {
                        if (isToCamera) {
                            startCameraActivity();
                            isToCamera = false;
                        } else {

                        }
                    }

                    @Override
                    public void edit() {
                        if (isEditText) {
                            binding.content.setEnabled(true);
                            isEditText = false;
                        } else {
                            Toast.makeText(MainActivity.this, "正在编辑...", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        binding.takePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("温馨提示:");
                dialog.setMessage("是否继续添加内容?");
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (binding.content.length() > 0)
                            httpUtil.requestFiles(binding.content.getText().toString());
                    }
                });

                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        buffer.append(binding.content.getText());
                        startCameraActivity();
                    }
                });
            }
        });

    }

    public void requestPermission(){
        for (int i = 0; i < permissions.length; i++){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{permissions[i]}, 100);
                }
            }
        }
    }

    private boolean checkTokenStatus() {
        if (!hasToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasToken;
    }

    /**
     * 以license文件方式初始化
     */
    private void initAccessToken() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   initAccessToken();
                } else {
                    Toast.makeText(getApplicationContext(), "需要android.permission.READ_PHONE_STATE", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 识别成功回调，通用文字识别（高精度版）
        if (requestCode == REQUEST_CODE_ACCURATE_BASIC && resultCode == Activity.RESULT_OK) {
            RecognizeService.recAccurateBasic(this, FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(final String result) {
                            Log.e(TAG, result);
                            try{
                                JSONObject json = new JSONObject(result);
                                JSONArray array = json.getJSONArray("words_result");
                                StringBuffer stringBuffer = new StringBuffer();
                                for (int i = 0; i < array.length(); i++){
                                    JSONObject words = array.getJSONObject(i);
                                    stringBuffer.append(words.getString("words"));
                                }
                                Message message = new Message();
                                message.what = 0;
                                message.obj = stringBuffer;
                                handler.sendEmptyMessage(0);
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    binding.recycler.setVisibility(View.GONE);
                    binding.editLayout.setVisibility(View.VISIBLE);
                    binding.content.setText(msg.obj.toString());
                    break;
                case 1:
                    break;
            }
        }
    };

    @Override
    public void Success(int code, String base64) {
        if (code == 200){
            FileUtil.base64ToFile(base64);
            binding.editLayout.setVisibility(View.GONE);
            binding.recycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void Failed(int code, String errorMessage) {

    }
}
