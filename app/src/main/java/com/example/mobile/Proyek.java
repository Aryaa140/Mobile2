package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Proyek {
    @SerializedName("id_proyek")
    private int idProyek;

    @SerializedName("nama_proyek")
    private String namaProyek;

    public Proyek() {
        // Default constructor untuk Gson
    }

    public Proyek(int idProyek, String namaProyek) {
        this.idProyek = idProyek;
        this.namaProyek = namaProyek;
    }

    public int getIdProyek() {
        return idProyek;
    }

    public String getNamaProyek() {
        return namaProyek != null ? namaProyek : "";
    }

    public void setIdProyek(int idProyek) {
        this.idProyek = idProyek;
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }
}