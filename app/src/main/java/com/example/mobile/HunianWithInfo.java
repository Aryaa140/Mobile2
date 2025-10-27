package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HunianWithInfo implements Serializable {
    @SerializedName("id_hunian")
    private int idHunian;

    @SerializedName("nama_hunian")
    private String namaHunian;

    @SerializedName("nama_proyek")
    private String namaProyek;

    @SerializedName("jumlah_tipe_hunian")
    private int jumlahTipeHunian;

    @SerializedName("jumlah_stok")
    private int jumlahStok;

    // Constructor kosong wajib untuk Gson
    public HunianWithInfo() {}

    // Getters
    public int getIdHunian() {
        return idHunian;
    }

    public String getNamaHunian() {
        return namaHunian != null ? namaHunian : "";
    }

    public String getNamaProyek() {
        return namaProyek != null ? namaProyek : "";
    }

    public int getJumlahTipeHunian() {
        return jumlahTipeHunian;
    }

    public int getJumlahStok() {
        return jumlahStok;
    }

    // Setters
    public void setIdHunian(int idHunian) {
        this.idHunian = idHunian;
    }

    public void setNamaHunian(String namaHunian) {
        this.namaHunian = namaHunian;
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    public void setJumlahTipeHunian(int jumlahTipeHunian) {
        this.jumlahTipeHunian = jumlahTipeHunian;
    }

    public void setJumlahStok(int jumlahStok) {
        this.jumlahStok = jumlahStok;
    }
}