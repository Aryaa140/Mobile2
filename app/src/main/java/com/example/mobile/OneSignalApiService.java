package com.example.mobile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OneSignalApiService {
    @POST("notifications")
    Call<OneSignalNotificationResponse> sendNotification(@Body OneSignalNotificationRequest request);
}