package com.example.mobile;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> loginUser(
            @Field("ussername") String ussername,
            @Field("Password") String password
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<LoginResponse> registerUser(
            @Field("ussername") String ussername,
            @Field("NIP") int NIP,
            @Field("Divisi") String Divisi,
            @Field("Password") String password
    );
}
