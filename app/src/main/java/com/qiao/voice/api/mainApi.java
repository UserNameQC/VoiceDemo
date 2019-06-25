package com.qiao.voice.api;

public interface mainApi {
    void Success(int code, String base64);
    void Failed(int code, String errorMessage);
}
