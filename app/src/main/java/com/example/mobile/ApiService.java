package com.example.mobile;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;
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
            @Field("Id_prospek") int idProspek,
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
            @Field("status_bpjs") String statusBpjs,
            @Field("Tanggal_Input") String tanggalInput
    );
    @GET("get_prospek_by_penginput.php")
    Call<ProspekResponse> getProspekByPenginput(@Query("penginput") String penginput);

    @GET("get_prospek_by_id.php")
    Call<ProspekDetailResponse> getProspekById(@Query("id") int id);

    @FormUrlEncoded
    @POST("update_prospek.php")
    Call<BasicResponse> updateProspek(
            @Field("old_nama_prospek") String oldNamaProspek,
            @Field("old_no_hp") String oldNoHp,
            @Field("nama_penginput") String namaPenginput,
            @Field("nama_prospek") String namaProspek,
            @Field("email") String email,
            @Field("no_hp") String noHp,
            @Field("alamat") String alamat,
            @Field("referensi_proyek") String referensiProyek,
            @Field("status_npwp") String statusNpwp,
            @Field("status_bpjs") String statusBpjs
    );

    @FormUrlEncoded
    @POST("delete_prospek.php")
    Call<BasicResponse> deleteProspekByData(
            @Field("nama_penginput") String namaPenginput,
            @Field("nama_prospek") String namaProspek
    );
    @FormUrlEncoded
    @POST("tambah_promo.php")
    Call<BasicResponse> tambahPromo(
            @Field("nama_promo") String namaPromo,
            @Field("nama_penginput") String namaPenginput,
            @Field("referensi_proyek") String referensiProyek,
            @Field("gambar") String gambarBase64
    );
    @GET("get_promo.php")
    Call<PromoResponse> getSemuaPromo();
}
