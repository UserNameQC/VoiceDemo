package com.qiao.voice.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.baidu.ocr.ui.camera.CameraActivity;
import com.just.agentweb.AgentWeb;
import com.qiao.voice.R;
import com.qiao.voice.databinding.ActivityAgmentWebBinding;
import com.qiao.voice.util.BarUtil;
import com.qiao.voice.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;

public class AgmentWebActivity extends BaseActivity {

    public AgentWeb agentWeb;
    public ActivityAgmentWebBinding binding;
    public final int REQUEST_CODE_ACCURATE_BASIC = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_agment_web);
        BarUtil.setViewHeight(this, binding.headTitleLayout.headView);
        initView();
    }

    public void initView(){
        initAgentWeb();
        initListener();
    }

    public void initAgentWeb(){
        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(binding.webLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go("http://www.baidu.com");
    }

    public void initListener(){
        binding.headTitleLayout.headAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenShot() != null){
                    startCameraActivity(screenShot());
                }
            }
        });
    }

    public void startCameraActivity(String path) {
        if (!checkTokenStatus()) {
            return;
        }
        Intent intent = new Intent(AgmentWebActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path
                /*FileUtil.getSaveFile(getApplication()).getAbsolutePath()*/);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE_ACCURATE_BASIC);
    }

    public String screenShot(){
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
                String shotName = "ScreenShot.png";
                // 图片文件路径
                String filePath = dirsPath.concat(File.separator).concat(shotName);
                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                Log.e("ScreenShot", "存储完成");
                return filePath;
            } catch (Exception e) {
            }
            //return bitmap;
        }
        return null;
    }
}
