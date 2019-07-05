package com.qiao.voice.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.qiao.voice.R;
import com.qiao.voice.databinding.ActivityAgmentWebBinding;
import com.qiao.voice.util.BarUtil;

import java.io.File;
import java.io.FileOutputStream;

public class AgmentWebActivity extends BaseActivity {

    public AgentWeb agentWeb;
    public ActivityAgmentWebBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_agment_web);
        BarUtil.setViewHeight(this, binding.headTitleLayout.headView);
        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(binding.webLinearLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go("http://www.baidu.com");
        binding.headTitleLayout.headAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenShot();
            }
        });
    }

    public void screenShot(){
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";
                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                //DebugLog.d("a7888", "存储完成");
            } catch (Exception e) {
            }
        }
    }
}
