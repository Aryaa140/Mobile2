package com.example.mobile;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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
    Call<RegisterResponse> registerUser(
            @Field("ussername") String username,
            @Field("NIP") String nip,
            @Field("Divisi") String division,
            @Field("Password") String password
    );
    @FormUrlEncoded
    @POST("check_nip.php")
    Call<CheckNIPResponse> checkNIP(@Field("NIP") String nip);
    @FormUrlEncoded
    @POST("update_password.php")
    Call<BasicResponse> updatePassword(
            @Field("username") String username,
            @Field("new_password") String newPassword
    );
    @FormUrlEncoded
    @POST("check_date_out.php")
    Call<DateOutResponse> checkDateOut(
            @Field("username") String username
    );
    @FormUrlEncoded
    @POST("update_profile.php")
    Call<BasicResponse> updateProfile(
            @Field("nip") int nip,
            @Field("old_username") String oldUsername,
            @Field("new_username") String newUsername,
            @Field("new_division") String newDivision,
            @Field("username_changed") int usernameChanged
    );
    @FormUrlEncoded
    @POST("check_username.php")
    Call<BasicResponse> checkUsername(
            @Field("username") String username,
            @Field("current_username") String currentUsername
    );
    @GET("get_all_users.php")
    Call<UserResponse> getSemuaUser();
    // ada kemungkinan tidak berfungsi untuk display response userp


    @FormUrlEncoded
    @POST("update_status_user.php")
    Call<BasicResponse> updateStatusUser(
            @Field("user_id") int userId,
            @Field("status_akun") String statusAkun
    );
    @GET("get_latest_nip_id.php")
    Call<LatestIdResponse> getLatestNipId();

    @POST("input_nip.php")
    Call<BasicResponse> inputNIP(@Body NipRequest nipRequest);
    @FormUrlEncoded
    @POST("input_nip.php")
    Call<BasicResponse> inputNIP(@Field("no_nip") String noNip);
    @FormUrlEncoded
    @POST("update_username_relations.php")
    Call<BasicResponse> updateUsernameInRelatedTables(
            @Field("old_username") String oldUsername,
            @Field("new_username") String newUsername
    );

    @FormUrlEncoded
    @POST("check_username_password.php")
    Call<PasswordCheckResponse> checkUsernameForPassword(
            @Field("username") String username
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
    @GET("get_prospek_by_penginput.php")
    Call<ProspekResponse> getProspekByPenginput(
            @Query("penginput") String penginput,
            @Query("user_level") String userLevel
    );
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

    @DELETE("delete_prospek.php")
    Call<BasicResponse> deleteProspekByData(
            @Query("nama_penginput") String namaPenginput,
            @Query("nama_prospek") String namaProspek,
            @Query("user_level") String userLevel
    );
    @FormUrlEncoded
    @POST("tambah_promo.php")
    Call<BasicResponse> tambahPromo(
            @Field("nama_promo") String namaPromo, // Diubah ke huruf kecil
            @Field("nama_penginput") String namaPenginput, // Diubah ke huruf kecil
            @Field("referensi_proyek") String referensiProyek, // Diubah ke huruf kecil
            @Field("gambar") String gambarBase64
    );
    @GET("get_promo.php")
    Call<PromoResponse> getSemuaPromo();
    @FormUrlEncoded
    @POST("delete_promo.php")
    Call<BasicResponse> deletePromo(
            @Field("id_promo") int idPromo
    );
    @FormUrlEncoded
    @POST("update_promo.php")
    Call<BasicResponse> updatePromo(
            @Field("id_promo") int idPromo,
            @Field("nama_promo") String namaPromo,
            @Field("nama_penginput") String namaPenginput,
            @Field("referensi_proyek") String referensiProyek,
            @Field("gambar_base64") String gambarBase64
    );
    @FormUrlEncoded
    @POST("check_new_promo.php")
    Call<PollingResponse> checkNewPromos(
            @Field("user_id") int userId,
            @Field("username") String username,
            @Field("device_id") String deviceId,
            @Field("last_check") String lastCheck
    );
    // Method untuk mendapatkan data prospek - tambahkan parameter penginput
    @GET("api_userprospek.php")
    Call<ProspekResponse> getProspekData(@Query("action") String action, @Query("penginput") String penginput);

    // Method untuk mendapatkan data proyek
    @GET("api_userprospek.php")
    Call<ProyekResponse> getProyekData(@Query("action") String action);

    // Method untuk mendapatkan hunian berdasarkan proyek
    @GET("api_userprospek.php")
    Call<HunianResponse> getHunianByProyek(@Query("action") String action, @Query("proyek") String proyek);

    // Method untuk menambah user prospek - menggunakan BasicResponse
    @POST("api_userprospek.php")
    Call<BasicResponse> addUserProspek(@Body UserProspekRequest request);
    @GET("api_userprospek.php")
    Call<UserProspekSimpleResponse> getUserProspekSimpleData(
            @Query("action") String action,
            @Query("penginput") String penginput
    );
    @GET("api_userprospek.php")
    Call<KavlingResponse> getKavlingByProyek(
            @Query("action") String action,
            @Query("proyek") String proyek
    );
    @GET("api_userprospek.php")
    Call<UpdateStatusResponse> updateStatusKavling(
            @Query("action") String action,
            @Query("proyek") String proyek,
            @Query("hunian") String hunian,
            @Query("tipe_hunian") String tipeHunian
    );
    @FormUrlEncoded
    @POST("api_proyek.php") // Sesuaikan dengan path file PHP Anda
    Call<BasicResponse> addProyek(
            @Field("action") String action,
            @Field("nama_proyek") String namaProyek
    );


    // Method untuk menambah hunian (tambahkan ini)
    @FormUrlEncoded
    @POST("api_hunian.php")
    Call<BasicResponse> addHunian(
            @Field("action") String action,
            @Field("nama_hunian") String namaHunian,
            @Field("nama_proyek") String namaProyek
    );
    @FormUrlEncoded
    @POST("api_kavling.php")
    Call<BasicResponse> tambahKavlingForm(
            @Field("action") String action,
            @Field("tipe_hunian") String tipeHunian,
            @Field("hunian") String hunian,
            @Field("proyek") String proyek,
            @Field("status_penjualan") String statusPenjualan,
            @Field("kode_kavling") String kodeKavling
    );
    // Method khusus untuk get proyek
    @GET("get_proyek.php")
    Call<ProyekResponse> getProyek();

    // Method khusus untuk get hunian by proyek
    @GET("get_hunian_by_proyek.php")
    Call<HunianResponse> getHunianByProyek(@Query("id_proyek") int idProyek);
    @FormUrlEncoded
    @POST("update_userprospek_with_histori.php") // PASTIKAN NAMA FILE BENAR
    Call<ResponseBody> updateUserProspekWithHistori(
            @Field("action") String action,
            @Field("id_userprospek") int idUserProspek,
            @Field("nama_penginput") String namaPenginput,
            @Field("nama_user") String namaUser,
            @Field("email") String email,
            @Field("no_hp") String noHp,
            @Field("alamat") String alamat,
            @Field("proyek") String proyek,
            @Field("hunian") String hunian,
            @Field("tipe_hunian") String tipeHunian,
            @Field("dp") int dp,
            @Field("status_bpjs") String statusBpjs,
            @Field("status_npwp") String statusNpwp
    );
    @FormUrlEncoded
    @POST("get_histori_userprospek.php")
    Call<HistoriUserProspekResponse> getHistoriUserProspek(
            @Field("action") String action,
            @Field("id_userprospek") int idUserProspek
    );

}

