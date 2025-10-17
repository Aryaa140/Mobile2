package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class UserProspekSimple {
    @SerializedName("id")
    private int id;

    @SerializedName("nama")
    private String nama;

    @SerializedName("penginput")
    private String penginput;

    @SerializedName("email")
    private String email;

    @SerializedName("nohp")
    private String nohp;

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

    @SerializedName("bpjs")
    private String bpjs;

    @SerializedName("npwp")
    private String npwp;

    @SerializedName("tanggal")
    private String tanggal;

    // Getter methods
    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getPenginput() { return penginput; }
    public String getEmail() { return email; }
    public String getNohp() { return nohp; }
    public String getAlamat() { return alamat; }
    public String getProyek() { return proyek; }
    public String getHunian() { return hunian; }
    public String getTipeHunian() { return tipeHunian; }
    public int getDp() { return dp; }
    public String getBpjs() { return bpjs; }
    public String getNpwp() { return npwp; }
    public String getTanggal() { return tanggal; }

    // Setter methods (jika diperlukan)
    public void setId(int id) { this.id = id; }
    public void setNama(String nama) { this.nama = nama; }
    public void setPenginput(String penginput) { this.penginput = penginput; }
    public void setEmail(String email) { this.email = email; }
    public void setNohp(String nohp) { this.nohp = nohp; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setProyek(String proyek) { this.proyek = proyek; }
    public void setHunian(String hunian) { this.hunian = hunian; }
    public void setTipeHunian(String tipeHunian) { this.tipeHunian = tipeHunian; }
    public void setDp(int dp) { this.dp = dp; }
    public void setBpjs(String bpjs) { this.bpjs = bpjs; }
    public void setNpwp(String npwp) { this.npwp = npwp; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
}