package com.qiao.voice.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.ocr.ui.camera.CameraActivity;
import com.just.agentweb.AgentWeb;
import com.qiao.voice.adapter.FileNameAdapter;
import com.qiao.voice.api.mainApi;
import com.qiao.voice.util.FileUtil;
import com.qiao.voice.http.HttpUtil;
import com.qiao.voice.R;
import com.qiao.voice.databinding.ActivityMainBinding;
import com.qiao.voice.util.BarUtil;
import com.qiao.voice.util.SpUtil;
import com.qiao.voice.util.TopMenu;
import com.qiao.voice.util.UriUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity implements mainApi {

    public final int REQUEST_CODE_ACCURATE_BASIC = 100;

    public AgentWeb agentWeb;
    public final String TAG = MainActivity.class.getSimpleName();
    public ActivityMainBinding binding;
    public HttpUtil httpUtil;
    //public String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public StringBuffer bufferBase64 = new StringBuffer();
    public FileNameAdapter nameAdapter;
    public List<String> dataList = new ArrayList<>();
    public Dialog fileDialog;
    public MediaProjectionManager projectionManager;
    public MediaProjection mediaProjection;
    public ImageReader imageReader;
    public VirtualDisplay virtualDisplay;
    public Uri uri = null;

    public SpUtil spUtil;

    public boolean isEditText = true;
    public boolean isToCamera = true;
    public boolean isAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
    }

    public void initView() {
        BarUtil.setViewHeight(this, binding.headTitleLayout.headView);
        httpUtil = new HttpUtil(this);
        spUtil = new SpUtil(this);
        //requestPermission();
        //initAccessToken();
        initListener();
        checkFileList();
        initAgentWeb();
    }

    /**
     * 声音文件列表
     */
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

    public void startCameraActivity(Uri path) {
        if (!checkTokenStatus()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        intent.putExtra(CameraActivity.CONTENT_TYPE_URI, path);
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
                                //startCameraActivity();
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

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void shot() {
//                        Uri path = screenShot();
//                        if (path != null){
//                            startCameraActivity(path);
//                        }

                        initMediaProjection();
                    }

                    @Override
                    public void mp3() {
                        binding.webLinearLayout.setVisibility(View.GONE);
                        binding.editLayout.setVisibility(View.GONE);
                        binding.recycler.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void saved() {
                        if (agentWeb != null){
                            String url = agentWeb.getWebCreator().getWebView().getUrl();
                            spUtil.putString(SpUtil.URL, url);
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
                        isAdd = false;
                        if (binding.content.length() > 0) {
                            httpUtil.requestFiles(binding.content.getText().toString());
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
                        isAdd = true;
                        isEditText = true;
                        isToCamera = true;
                        binding.content.setEnabled(false);
                        if (binding.content.length() > 0) {
                            httpUtil.requestFiles(binding.content.getText().toString());
                            binding.content.setText("");
                            binding.webLinearLayout.setVisibility(View.VISIBLE);
                            binding.recycler.setVisibility(View.GONE);
                            binding.editLayout.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity.this, "没有任何数据", Toast.LENGTH_SHORT).show();
                        }

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initMediaProjection(){
        projectionManager = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), 200);
    }

    public void initAgentWeb(){
        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(binding.webLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(spUtil.getString(SpUtil.URL));
        //agentWeb.getWebCreator().getWebView().getUrl();
    }

    public Uri screenShot(){
        View dView = getWindow().getDecorView();

        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                String dirs = "com.qiao.voice";
                String dirsPath = sdCardPath.concat(File.separator).concat(dirs);
                File dirFile = new File(dirsPath);
                if (!dirFile.exists()){
                    dirFile.mkdirs();
                }
                String shotName = System.currentTimeMillis()/1000 + ".png";
                // 图片文件路径
                String filePath = dirsPath.concat(File.separator).concat(shotName);
                File file = new File(filePath);

                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Log.e("ScreenShot", "存储完成");
                return UriUtil.getImageContentUri(this, file);
            } catch (Exception e) {
            }
            //return bitmap;
        }
        return null;
    }

    @Override
    protected void onPause() {
        agentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        agentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        agentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (agentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //initAccessToken();
                } else {
                    Toast.makeText(getApplicationContext(), "需要android.permission.READ_PHONE_STATE", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        } else

        if (requestCode == 200 && resultCode == RESULT_OK && data != null){
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            getCapture();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getCapture(){
        WindowManager manager = this.getWindowManager();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(displayMetrics);
        final int mWidth = displayMetrics.widthPixels;
        final int mHeight = displayMetrics.heightPixels;
        imageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "screenShot",
                mWidth,
                mHeight,
                displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.getSurface(),
                null,
                handler);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                FileOutputStream fos = null;
                Bitmap bitmap = null;

                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * mWidth;

                        bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride,
                                mHeight, Bitmap.Config.ARGB_8888);
                        buffer.position(0);
                        bitmap.copyPixelsFromBuffer(buffer);

//                        Date currentDate = new Date();
//                        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddhhmmss");
//                        String fileName = STORE_DIR + "/myScreen_" + date.format(currentDate) + ".png";
//
                        // 获取内置SD卡路径
                        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                        String dirs = "com.qiao.voice/png";
                        String dirsPath = sdCardPath.concat(File.separator).concat(dirs);
                        File dirFile = new File(dirsPath);
                        if (!dirFile.exists()){
                            dirFile.mkdirs();
                        }
                        String shotName = System.currentTimeMillis()/1000 + ".png";
                        // 图片文件路径
                        String filePath = dirsPath.concat(File.separator).concat(shotName);
                        File file = new File(filePath);
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        stopProjection();
                        uri = UriUtil.getImageContentUri(MainActivity.this, file);
                        if (uri != null) {
                            startCameraActivity(uri);
                        }
                        Log.d("WOW", "End now!!!!!!");
                        Toast.makeText(MainActivity.this, "Screenshot saved in " + filePath, Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    if (image != null) {
                        image.close();
                    }
                }
            }
        }, handler);
        mediaProjection.registerCallback(new MediaProjectionStopCallback(), handler);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (virtualDisplay != null) {
                        virtualDisplay.release();
                    }
                    if (imageReader != null) {
                        imageReader.setOnImageAvailableListener(null, null);
                    }
                    mediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopProjection(){
        mediaProjection.stop();
    }

    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    binding.recycler.setVisibility(View.GONE);
                    binding.webLinearLayout.setVisibility(View.GONE);
                    binding.editLayout.setVisibility(View.VISIBLE);
                    binding.content.setText(msg.obj.toString());
                    break;
                case 1:
                    showEditFileNameDialog(msg.obj.toString());
                    break;
                case 2:
                    break;
                case 3:
                    initRecycler();
                    break;
                case 4:
                    bufferBase64.delete(0, bufferBase64.length());
                    fileDialog.dismiss();
                    binding.editLayout.setVisibility(View.GONE);
                    binding.recycler.setVisibility(View.VISIBLE);
                    binding.webLinearLayout.setVisibility(View.GONE);
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
            try {
                JSONObject jsonObject = new JSONObject(base64);
                String data = jsonObject.getString("data");
                bufferBase64.append(data);
                Message message = new Message();
                if (!isAdd){
                    message.what = 1;
                    message.obj = bufferBase64;
                    Log.e("Base64", bufferBase64.toString());
                    handler.sendMessage(message);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void Failed(int code, String errorMessage) {

    }
}
