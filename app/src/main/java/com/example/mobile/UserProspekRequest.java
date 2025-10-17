package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class UserProspekRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("data")
    private UserProspekData data;

    public UserProspekRequest(String action, UserProspekData data) {
        this.action = action;
        this.data = data;
    }

    // Tambahkan getter methods
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UserProspekData getData() {
        return data;
    }

    public void setData(UserProspekData data) {
        this.data = data;
    }
}

class UserProspekData {
    @SerializedName("Id_prospek")
    private int idProspek;

    @SerializedName("Nama_User")
    private String namaUser;

    @SerializedName("Nama_Penginput")
    private String namaPenginput;

    @SerializedName("Email")
    private String email;

    @SerializedName("No_HP")
    private String noHp;

    @SerializedName("Alamat")
    private String alamat;

    @SerializedName("Proyek")
    private String proyek;

    @SerializedName("Hunian")
    private String hunian;
    @SerializedName("Tipe_Hunian")
    private String tipeHunian; // TAMBAHAN: Kolom baru
    @SerializedName("DP")
    private int dp;

    @SerializedName("Status_BPJS")
    private String statusBpjs;

    @SerializedName("Status_NPWP")
    private String statusNpwp;

    // Constructor
    public UserProspekData(int idProspek, String namaUser, String namaPenginput, String email,
                           String noHp, String alamat, String proyek, String hunian, String tipeHunian,
                           int dp, String statusBpjs, String statusNpwp) {
        this.idProspek = idProspek;
        this.namaUser = namaUser;
        this.namaPenginput = namaPenginput;
        this.email = email;
        this.noHp = noHp;
        this.alamat = alamat;
        this.proyek = proyek;
        this.hunian = hunian;
        this.tipeHunian = tipeHunian; // TAMBAHAN
        this.dp = dp;
        this.statusBpjs = statusBpjs;
        this.statusNpwp = statusNpwp;
    }

    // Tambahkan semua getter methods
    public int getIdProspek() {
        return idProspek;
    }

    public void setIdProspek(int idProspek) {
        this.idProspek = idProspek;
    }

    public String getNamaUser() {
        return namaUser;
    }

    public void setNamaUser(String namaUser) {
        this.namaUser = namaUser;
    }

    public String getNamaPenginput() {
        return namaPenginput;
    }

    public void setNamaPenginput(String namaPenginput) {
        this.namaPenginput = namaPenginput;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getProyek() {
        return proyek;
    }

    public void setProyek(String proyek) {
        this.proyek = proyek;
    }

    public String getHunian() {
        return hunian;
    }

    public void setHunian(String hunian) {
        this.hunian = hunian;
    }
    public String getTipeHunian() {
        return tipeHunian;
    }

    public void setTipeHunian(String tipeHunian) {
        this.tipeHunian = tipeHunian;
    }
    public int getDp() {
        return dp;
    }

    public void setDp(int dp) {
        this.dp = dp;
    }

    public String getStatusBpjs() {
        return statusBpjs;
    }

    public void setStatusBpjs(String statusBpjs) {
        this.statusBpjs = statusBpjs;
    }

    public String getStatusNpwp() {
        return statusNpwp;
    }

    public void setStatusNpwp(String statusNpwp) {
        this.statusNpwp = statusNpwp;
    }
}