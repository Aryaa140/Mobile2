package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class ProyekWithInfo {
    @SerializedName("id_proyek")
    private int idProyek;

    @SerializedName("nama_proyek")
    private String namaProyek;

    @SerializedName("jumlah_hunian")
    private int jumlahHunian;

    @SerializedName("jumlah_stok")
    private int jumlahStok;

    // Constructor kosong wajib untuk Gson
    public ProyekWithInfo() {}

    // Getters
    public int getIdProyek() {
        return idProyek;
    }

    public String getNamaProyek() {
        return namaProyek != null ? namaProyek : "";
    }

    public int getJumlahHunian() {
        return jumlahHunian;
    }

    public int getJumlahStok() {
        return jumlahStok;
    }

    // Setters untuk hardcode data
    public void setIdProyek(int idProyek) {
        this.idProyek = idProyek;
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    public void setJumlahHunian(int jumlahHunian) {
        this.jumlahHunian = jumlahHunian;
    }

    public void setJumlahStok(int jumlahStok) {
        this.jumlahStok = jumlahStok;
    }
}