package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Realisasi {
    @SerializedName("id")
    private int id;

    @SerializedName("nama_user")
    private String namaUser;

    @SerializedName("nama_penginput")
    private String namaPenginput;

    @SerializedName("email")
    private String email;

    @SerializedName("no_hp")
    private String noHp;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("proyek")
    private String proyek;

    @SerializedName("hunian")
    private String hunian;

    @SerializedName("tipe_hunian")
    private String tipeHunian;

    @SerializedName("dp")
    private int dp;

    @SerializedName("status_bpjs")
    private String statusBpjs;

    @SerializedName("status_npwp")
    private String statusNpwp;

    @SerializedName("tanggal_input")
    private String tanggalInput;

    @SerializedName("tanggal_realisasi")
    private String tanggalRealisasi;

    // Constructor
    public Realisasi(int id, String namaUser, String namaPenginput, String email, String noHp,
                     String alamat, String proyek, String hunian, String tipeHunian, int dp,
                     String statusBpjs, String statusNpwp, String tanggalInput, String tanggalRealisasi) {
        this.id = id;
        this.namaUser = namaUser;
        this.namaPenginput = namaPenginput;
        this.email = email;
        this.noHp = noHp;
        this.alamat = alamat;
        this.proyek = proyek;
        this.hunian = hunian;
        this.tipeHunian = tipeHunian;
        this.dp = dp;
        this.statusBpjs = statusBpjs;
        this.statusNpwp = statusNpwp;
        this.tanggalInput = tanggalInput;
        this.tanggalRealisasi = tanggalRealisasi;
    }

    // Default constructor untuk Gson
    public Realisasi() {
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }

    public String getNamaPenginput() { return namaPenginput; }
    public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getProyek() { return proyek; }
    public void setProyek(String proyek) { this.proyek = proyek; }

    public String getHunian() { return hunian; }
    public void setHunian(String hunian) { this.hunian = hunian; }

    public String getTipeHunian() { return tipeHunian; }
    public void setTipeHunian(String tipeHunian) { this.tipeHunian = tipeHunian; }

    public int getDp() { return dp; }
    public void setDp(int dp) { this.dp = dp; }

    public String getStatusBpjs() { return statusBpjs; }
    public void setStatusBpjs(String statusBpjs) { this.statusBpjs = statusBpjs; }

    public String getStatusNpwp() { return statusNpwp; }
    public void setStatusNpwp(String statusNpwp) { this.statusNpwp = statusNpwp; }

    public String getTanggalInput() { return tanggalInput; }
    public void setTanggalInput(String tanggalInput) { this.tanggalInput = tanggalInput; }

    public String getTanggalRealisasi() { return tanggalRealisasi; }
    public void setTanggalRealisasi(String tanggalRealisasi) { this.tanggalRealisasi = tanggalRealisasi; }
}