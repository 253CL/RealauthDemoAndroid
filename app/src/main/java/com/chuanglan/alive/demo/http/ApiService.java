package com.chuanglan.alive.demo.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST
    Call<ResponseBody> getAuthToken(@Url String url, @Body Map<String, String> params);

    @Multipart
    @POST
    Call<ResponseBody> uploadVideo(@Url String url, @Part MultipartBody.Part body);
}
