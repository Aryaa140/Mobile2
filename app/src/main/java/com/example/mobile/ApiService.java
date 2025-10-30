package com.example.mobile;

import java.util.Map;

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
    // Di ApiService.java
    @FormUrlEncoded
    @POST("api_proyek.php")
    Call<BasicResponse> addProyek(
            @Field("action") String action,
            @Field("nama_proyek") String namaProyek,
            @Field("lokasi_proyek") String lokasiProyek,
            @Field("deskripsi_proyek") String deskripsiProyek,
            @Field("logo") String logo,
            @Field("siteplan") String siteplan,
            @Field("fasilitas") String fasilitasJson
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
    @GET("get_proyek_with_info.php")
    Call<ProyekWithInfoResponse> getProyekWithInfo();
    @FormUrlEncoded
    @POST("delete_proyek.php")
    Call<BasicResponse> deleteProyek(
            @Field("id_proyek") int idProyek,
            @Field("nama_proyek") String namaProyek
    );
    @FormUrlEncoded
    @POST("update_proyek.php")
    Call<BasicResponse> updateProyek(
            @Field("id_proyek") int idProyek,
            @Field("old_nama_proyek") String oldNamaProyek,
            @Field("new_nama_proyek") String newNamaProyek
    );
    @GET("get_hunian_with_info.php")
    Call<HunianWithInfoResponse> getHunianWithInfo();
    @FormUrlEncoded
    @POST("delete_hunian.php")
    Call<BasicResponse> deleteHunian(
            @Field("id_hunian") int idHunian,
            @Field("nama_hunian") String namaHunian,
            @Field("nama_proyek") String namaProyek
    );
    @FormUrlEncoded
    @POST("update_hunian.php")
    Call<BasicResponse> updateHunian(
            @Field("id_hunian") int idHunian,
            @Field("old_nama_hunian") String oldNamaHunian,
            @Field("new_nama_hunian") String newNamaHunian,
            @Field("nama_proyek") String namaProyek
    );
    @GET("get_kavling_with_info.php")
    Call<KavlingWithInfoResponse> getKavlingWithInfo();
    @FormUrlEncoded
    @POST("delete_kavling.php")
    Call<BasicResponse> deleteKavling(
            @Field("id_kavling") int idKavling
    );
    @FormUrlEncoded
    @POST("update_kavling.php")
    Call<BasicResponse> updateKavling(
            @Field("id_kavling") int idKavling,
            @Field("old_tipe_hunian") String oldTipeHunian,
            @Field("new_tipe_hunian") String newTipeHunian,
            @Field("old_hunian") String oldHunian,
            @Field("new_hunian") String newHunian,
            @Field("old_proyek") String oldProyek,
            @Field("new_proyek") String newProyek
            // Status penjualan dihapus dari parameter
    );
    // news
    @GET("get_news_histori.php")
    Call<NewsHistoriResponse> getNewsHistori(
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    // ✅ METHOD UNTUK MENDAPATKAN DATA HISTORI TERBARU
    @FormUrlEncoded
    @POST("get_latest_news_histori.php")
    Call<NewsHistoriResponse> getLatestNewsHistori(
            @Field("last_sync_time") String lastSyncTime
    );

    @FormUrlEncoded
    @POST("promo_histori_handler.php")
    Call<BasicResponse> addPromoHistori(
            @Field("action") String action,
            @Field("promo_id") int promoId,
            @Field("title") String title,
            @Field("penginput") String penginput,
            @Field("status") String status,
            @Field("image_data") String imageData
    );

    @FormUrlEncoded
    @POST("promo_histori_handler.php")
    Call<BasicResponse> updatePromoHistori(
            @Field("action") String action,
            @Field("promo_id") int promoId,
            @Field("title") String title,
            @Field("penginput") String penginput,
            @Field("status") String status,
            @Field("image_data") String imageData
    );

    @FormUrlEncoded
    @POST("promo_histori_handler.php")
    Call<BasicResponse> deletePromoHistori(
            @Field("action") String action,
            @Field("promo_id") int promoId,      // ✅ Parameter ke-2
            @Field("title") String title,         // ✅ Parameter ke-3
            @Field("penginput") String penginput, // ✅ Parameter ke-4
            @Field("image_data") String imageData // ✅ Parameter ke-5
    );

    // ✅ METHOD BARU: UNTUK HISTORI DENGAN @Body (LEBIH AMAN UNTUK DATA BESAR)
    @POST("promo_histori_handler.php")
    Call<BasicResponse> addPromoHistoriWithBody(@Body Map<String, Object> body);

    // ✅ METHOD BARU: UNTUK UPDATE DAN DELETE JUGA
    @POST("promo_histori_handler.php")
    Call<BasicResponse> updatePromoHistoriWithBody(@Body Map<String, Object> body);

    @POST("promo_histori_handler.php")
    Call<BasicResponse> deletePromoHistoriWithBody(@Body Map<String, Object> body);

    // ✅ METHOD UNTUK GET PROMO BY ID (jika perlu)
    @GET("get_promo_by_id.php")
    Call<PromoResponse> getPromoById(@Query("id_promo") int idPromo);
}

