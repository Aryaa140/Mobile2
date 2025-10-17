package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class KavlingRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("tipe_hunian")
    private String tipe_hunian;

    @SerializedName("hunian")
    private String hunian;

    @SerializedName("proyek")
    private String proyek;

    @SerializedName("status_penjualan")
    private String status_penjualan;

    @SerializedName("kode_kavling")
    private String kode_kavling;

    // Constructor
    public KavlingRequest(String action, String tipe_hunian, String hunian, String proyek, String status_penjualan, String kode_kavling) {
        this.action = action;
        this.tipe_hunian = tipe_hunian;
        this.hunian = hunian;
        this.proyek = proyek;
        this.status_penjualan = status_penjualan;
        this.kode_kavling = kode_kavling;
    }

    // Default constructor
    public KavlingRequest() {
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTipe_hunian() {
        return tipe_hunian;
    }

    public void setTipe_hunian(String tipe_hunian) {
        this.tipe_hunian = tipe_hunian;
    }

    public String getHunian() {
        return hunian;
    }

    public void setHunian(String hunian) {
        this.hunian = hunian;
    }

    public String getProyek() {
        return proyek;
    }

    public void setProyek(String proyek) {
        this.proyek = proyek;
    }

    public String getStatus_penjualan() {
        return status_penjualan;
    }

    public void setStatus_penjualan(String status_penjualan) {
        this.status_penjualan = status_penjualan;
    }

    public String getKode_kavling() {
        return kode_kavling;
    }

    public void setKode_kavling(String kode_kavling) {
        this.kode_kavling = kode_kavling;
    }
}