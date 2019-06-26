package com.qiao.voice.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.qiao.voice.api.mainApi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    private OkHttpClient okHttpClient;
    public ExecutorService executors = Executors.newScheduledThreadPool(5);
    private mainApi mainApi;
    public HttpUtil (mainApi mainApi){
        this.mainApi = mainApi;
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void requestFiles(final String message){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.qjcjob.com:89/handle?message=" + message)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("TAG", e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()){
                            assert response.body() != null;
                            String result = response.body().string();
                            Log.e("TAG", result);
                            mainApi.Success(response.code(), result);
                        }
                    }
                });
            }
        });
    }
}
