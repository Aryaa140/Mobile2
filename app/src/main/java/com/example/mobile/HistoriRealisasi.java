// HistoriRealisasi.java
package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class HistoriRealisasi {
    @SerializedName("Id_HistoriRealisasi")
    private int idHistori;

    @SerializedName("Id_Realisasi")
    private int idRealisasi;

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
    private String tipeHunian;

    @SerializedName("DP")
    private int dp;

    @SerializedName("Status_BPJS")
    private String statusBpjs;

    @SerializedName("Status_NPWP")
    private String statusNpwp;

    @SerializedName("Tanggal_Input")
    private String tanggalInput;

    @SerializedName("Tanggal_Realisasi")
    private String tanggalRealisasi;

    // Constructor
    public HistoriRealisasi(int idHistori, int idRealisasi, String namaUser, String namaPenginput,
                            String email, String noHp, String alamat, String proyek, String hunian,
                            String tipeHunian, int dp, String statusBpjs, String statusNpwp,
                            String tanggalInput, String tanggalRealisasi) {
        this.idHistori = idHistori;
        this.idRealisasi = idRealisasi;
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

    // Getters
    public int getIdHistori() { return idHistori; }
    public int getIdRealisasi() { return idRealisasi; }
    public String getNamaUser() { return namaUser; }
    public String getNamaPenginput() { return namaPenginput; }
    public String getEmail() { return email; }
    public String getNoHp() { return noHp; }
    public String getAlamat() { return alamat; }
    public String getProyek() { return proyek; }
    public String getHunian() { return hunian; }
    public String getTipeHunian() { return tipeHunian; }
    public int getDp() { return dp; }
    public String getStatusBpjs() { return statusBpjs; }
    public String getStatusNpwp() { return statusNpwp; }
    public String getTanggalInput() { return tanggalInput; }
    public String getTanggalRealisasi() { return tanggalRealisasi; }
}