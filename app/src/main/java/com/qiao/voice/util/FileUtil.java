/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.qiao.voice.util;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }

    /**
     * base64字符串转文件
     * @param base64
     * @return
     */
    public static File base64ToFile(Handler handler, String base64, String name, boolean isGetList) {
        File file = null;
        String dirsName = "com.qiao.voice/mp3";
        String fileName = dirsName.concat("/").concat(name).concat(".mp3");
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            File dirFile = new File(Environment.getExternalStorageDirectory(), dirsName);
            if (!dirFile.exists())
                dirFile.mkdirs();

            if (isGetList) return dirFile;

            file = new File(Environment.getExternalStorageDirectory(), fileName);
            if (file.exists()){
                handler.sendEmptyMessage(5);
            }else {
                //file.createNewFile();
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                byte[] buffer = new byte[1024];
                out = new FileOutputStream(file);
                int bytesum = 0;
                int byteread = 0;
                while ((byteread = in.read(buffer)) != -1) {
                    bytesum += byteread;
                    out.write(buffer, 0, byteread); // 文件写操作
                }
                handler.sendEmptyMessage(4);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (out!= null) {
                    out.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File getFileList() throws Exception {
        String dirName = "com.qiao.voice/mp3";
        File file = new File(Environment.getExternalStorageDirectory(), dirName);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static File checkFile(File file, String fileName) throws Exception{
        String path = file.getAbsolutePath().concat("/").concat(fileName).concat(".mp3");
        return new File(Environment.getExternalStorageDirectory(), path);
    }

    public static void saveFile(File file, String base64){
        FileOutputStream out = null;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int byteSum = 0;
            int byteRead = 0;
            while ((byteRead = in.read(buffer)) != -1) {
                byteSum += byteRead;
                out.write(buffer, 0, byteRead); // 文件写操作
            }
        }catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (out!= null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
