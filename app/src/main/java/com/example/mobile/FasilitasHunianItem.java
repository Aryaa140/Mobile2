package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FasilitasHunianItem implements Serializable {
    @SerializedName("Id_FasilitasHunian") // HARUS SESUAI DENGAN PHP
    private int idFasilitas;

    @SerializedName("Nama_Fasilitas")
    private String namaFasilitas;

    @SerializedName("Jumlah")
    private int jumlah;

    @SerializedName("Nama_Hunian")
    private String namaHunian;

    // Constructor default
    public FasilitasHunianItem() {
    }

    // Constructor dengan parameter
    public FasilitasHunianItem(int idFasilitas, String namaFasilitas, int jumlah, String namaHunian) {
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
        this.jumlah = jumlah;
        this.namaHunian = namaHunian;
    }

    // Getters and Setters
    public int getIdFasilitas() {
        return idFasilitas;
    }

    public void setIdFasilitas(int idFasilitas) {
        this.idFasilitas = idFasilitas;
    }

    public String getNamaFasilitas() {
        return namaFasilitas;
    }

    public void setNamaFasilitas(String namaFasilitas) {
        this.namaFasilitas = namaFasilitas;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getNamaHunian() {
        return namaHunian;
    }

    public void setNamaHunian(String namaHunian) {
        this.namaHunian = namaHunian;
    }

    @Override
    public String toString() {
        return "FasilitasHunianItem{" +
                "idFasilitas=" + idFasilitas +
                ", namaFasilitas='" + namaFasilitas + '\'' +
                ", jumlah=" + jumlah +
                ", namaHunian='" + namaHunian + '\'' +
                '}';
    }
}