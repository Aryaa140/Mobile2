package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Kavling {
    @SerializedName("Id_kavling")
    private int idKavling;

    @SerializedName("Tipe_Hunian")
    private String tipeHunian;

    @SerializedName("Hunian")
    private String hunian;

    @SerializedName("Proyek")
    private String proyek;

    @SerializedName("Status_Penjualan")
    private String statusPenjualan;

    // Empty constructor untuk Gson
    public Kavling() {}

    // Constructor
    public Kavling(int idKavling, String tipeHunian, String hunian, String proyek, String statusPenjualan) {
        this.idKavling = idKavling;
        this.tipeHunian = tipeHunian;
        this.hunian = hunian;
        this.proyek = proyek;
        this.statusPenjualan = statusPenjualan;
    }

    // Getter methods
    public int getIdKavling() { return idKavling; }
    public String getTipeHunian() { return tipeHunian; }
    public String getHunian() { return hunian; }
    public String getProyek() { return proyek; }
    public String getStatusPenjualan() { return statusPenjualan; }

    // Setter methods
    public void setIdKavling(int idKavling) { this.idKavling = idKavling; }
    public void setTipeHunian(String tipeHunian) { this.tipeHunian = tipeHunian; }
    public void setHunian(String hunian) { this.hunian = hunian; }
    public void setProyek(String proyek) { this.proyek = proyek; }
    public void setStatusPenjualan(String statusPenjualan) { this.statusPenjualan = statusPenjualan; }
}