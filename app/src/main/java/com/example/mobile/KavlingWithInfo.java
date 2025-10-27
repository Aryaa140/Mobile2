package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class KavlingWithInfo implements Serializable {
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

    // Constructor
    public KavlingWithInfo() {}

    // Getters
    public int getIdKavling() {
        return idKavling;
    }

    public String getTipeHunian() {
        return tipeHunian != null ? tipeHunian : "";
    }

    public String getHunian() {
        return hunian != null ? hunian : "";
    }

    public String getProyek() {
        return proyek != null ? proyek : "";
    }

    public String getStatusPenjualan() {
        return statusPenjualan != null ? statusPenjualan : "";
    }

    // Setters
    public void setIdKavling(int idKavling) {
        this.idKavling = idKavling;
    }

    public void setTipeHunian(String tipeHunian) {
        this.tipeHunian = tipeHunian;
    }

    public void setHunian(String hunian) {
        this.hunian = hunian;
    }

    public void setProyek(String proyek) {
        this.proyek = proyek;
    }

    public void setStatusPenjualan(String statusPenjualan) {
        this.statusPenjualan = statusPenjualan;
    }
}