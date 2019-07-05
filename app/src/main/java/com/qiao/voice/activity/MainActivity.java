package com.qiao.voice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.qiao.voice.adapter.FileNameAdapter;
import com.qiao.voice.api.mainApi;
import com.qiao.voice.util.FileUtil;
import com.qiao.voice.http.HttpUtil;
import com.qiao.voice.R;
import com.qiao.voice.databinding.ActivityMainBinding;
import com.qiao.voice.util.BarUtil;
import com.qiao.voice.util.TopMenu;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity implements mainApi {

    public final int REQUEST_CODE_ACCURATE_BASIC = 100;

    public boolean hasToken = false;
    public final String TAG = MainActivity.class.getSimpleName();
    public ActivityMainBinding binding;
    public HttpUtil httpUtil;
    public String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public StringBuffer buffer = new StringBuffer();
    public FileNameAdapter nameAdapter;
    public List<String> dataList = new ArrayList<>();
    public Dialog fileDialog;

    public boolean isEditText = true;
    public boolean isToCamera = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
    }

    public void initView() {
        BarUtil.setViewHeight(this, binding.headTitleLayout.headView);
        httpUtil = new HttpUtil(this);
        requestPermission();
        initAccessToken();
        initListener();
        checkFileList();
    }

    public void checkFileList() {
        httpUtil.executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = FileUtil.getFileList();
                    dataList.clear();
                    dataList.addAll(Arrays.asList(file.list()));
                    handler.sendEmptyMessage(3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initRecycler() {
        if (nameAdapter == null) {
            binding.recycler.setLayoutManager(new LinearLayoutManager(this));
            nameAdapter = new FileNameAdapter(this, dataList);
            binding.recycler.setAdapter(nameAdapter);
        } else {
            nameAdapter.notifyDataSetChanged();
        }
    }

    public void startCameraActivity() {
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

    public void initListener() {
        binding.headTitleLayout.headAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopMenu.showMenu(MainActivity.this, binding.headTitleLayout.headAdd);
                TopMenu.setItemListener(new TopMenu.ItemClickListener() {
                    @Override
                    public void takePhotos() {

                        if (isToCamera) {
                            if (binding.content.length() <= 0) {
                                startCameraActivity();
                            } else {
                                Toast.makeText(MainActivity.this, "有正在编辑的内容，请保存!", Toast.LENGTH_SHORT).show();
                            }

                            isToCamera = false;
                        } else {

                        }
                    }

                    @Override
                    public void edit() {
                        if (binding.content.getVisibility() == View.VISIBLE) {
                            if (isEditText) {
                                binding.content.setEnabled(true);
                                isEditText = false;
                            } else {
                                Toast.makeText(MainActivity.this, "正在编辑...", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "无内容可编辑", Toast.LENGTH_SHORT).show();
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
                dialog.setCancelable(false);
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (binding.content.length() > 0) {
                            buffer.append(binding.content.getText());
                        }
                        if (buffer.length() > 0) {
                            httpUtil.requestFiles(buffer.toString());
                            isToCamera = true;
                            isEditText = true;
                            binding.content.setEnabled(false);
                        } else {
                            Toast.makeText(MainActivity.this, "没有任何数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isEditText = true;
                        isToCamera = true;
                        binding.content.setEnabled(false);
                        buffer.append(binding.content.getText());
                        binding.content.setText("");
                        startCameraActivity();
                    }
                });

                dialog.setNeutralButton("继续编辑", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

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
                            try {
                                JSONObject json = new JSONObject(result);
                                JSONArray array = json.getJSONArray("words_result");
                                StringBuffer stringBuffer = new StringBuffer();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject words = array.getJSONObject(i);
                                    stringBuffer.append(words.getString("words"));
                                }
                                Message message = new Message();
                                message.what = 0;
                                message.obj = stringBuffer;
                                handler.sendMessage(message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    binding.recycler.setVisibility(View.GONE);
                    binding.editLayout.setVisibility(View.VISIBLE);
                    binding.content.setText(msg.obj.toString());
                    break;
                case 1:
                    showEditFileNameDialog(msg.obj.toString());
                    break;
                case 3:
                    initRecycler();
                    break;
                case 4:
                    fileDialog.dismiss();
                    binding.editLayout.setVisibility(View.GONE);
                    binding.recycler.setVisibility(View.VISIBLE);
                    checkFileList();
                    break;
                case 5:
                    Toast.makeText(MainActivity.this, "存在相同文件", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void showEditFileNameDialog(final String base64) {
        if (fileDialog == null) {
            fileDialog = new Dialog(this, R.style.dialog_style);
            fileDialog.setContentView(R.layout.file_name_layout);

            WindowManager.LayoutParams params = fileDialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER_VERTICAL;
            getWindow().getDecorView().setPadding(0,0,0,0);

            fileDialog.setCanceledOnTouchOutside(false);
            final EditText editText = fileDialog.findViewById(R.id.file_name);
            fileDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileDialog.dismiss();
                }
            });

            fileDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText.length() > 0) {
                        final String fileName = editText.getText().toString();
                        httpUtil.executors.execute(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.base64ToFile(handler, base64, fileName, false);
                            }
                        });

                    }
                }
            });
        }
        fileDialog.show();
    }

    @Override
    public void Success(int code, String base64) {
        if (code == 200) {
            //FileUtil.base64ToFile(base64, "");
            buffer.delete(0, buffer.length());
            Message message = new Message();
            message.what = 1;
            message.obj = base64;
            handler.sendMessage(message);
        }
    }

    @Override
    public void Failed(int code, String errorMessage) {

    }
}
