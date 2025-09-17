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
    @FormUrlEncoded
    @POST("register.php")
    Call<RegisterResponse> registerUser(
            @Field("ussername") String username,
            @Field("NIP") String nip,
            @Field("Divisi") String division,
            @Field("Password") String password
    );
    @FormUrlEncoded
    @POST("tambah_prospek.php")
    Call<BasicResponse> tambahProspek(
            @Field("nama_penginput") String namaPenginput,
            @Field("nama_prospek") String namaProspek,
            @Field("email") String email,
            @Field("no_hp") String noHp,
            @Field("alamat") String alamat,
            @Field("referensi_proyek") String referensiProyek,
            @Field("status_npwp") String statusNpwp,
            @Field("status_bpjs") String statusBpjs
    );
}
