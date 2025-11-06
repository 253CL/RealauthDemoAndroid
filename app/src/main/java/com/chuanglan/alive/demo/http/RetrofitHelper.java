package com.chuanglan.alive.demo.http;


import com.chuanglan.alive.demo.BuildConfig;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    public ApiService getService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ApiService.class);
    }

    public Call<ResponseBody> getAuthToken(String url, Map<String, String> body) {
        return getService().getAuthToken(url, body);
    }

    public Call<ResponseBody> uploadVideo(String url, MultipartBody.Part body) {
        return getService().uploadVideo(url, body);
    }
}
