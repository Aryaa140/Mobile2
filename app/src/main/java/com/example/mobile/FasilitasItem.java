package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class FasilitasItem {
    @SerializedName("idFasilitas")
    private int idFasilitas;

    @SerializedName("namaFasilitas")
    private String namaFasilitas;

    @SerializedName("namaProyek")
    private String namaProyek;

    @SerializedName("gambarBase64")
    private String gambarBase64;

    // Constructor default
    public FasilitasItem() {
    }

    // Constructor untuk DetailProyekActivity (dengan ID)
    public FasilitasItem(int idFasilitas, String namaFasilitas, String gambarBase64) {
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
        this.gambarBase64 = gambarBase64;
    }

    // Constructor untuk InputDataProyekActivity (tanpa ID)
    public FasilitasItem(String namaFasilitas, String gambarBase64) {
        this.namaFasilitas = namaFasilitas;
        this.gambarBase64 = gambarBase64;
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

    public String getGambarBase64() {
        return gambarBase64;
    }

    public void setGambarBase64(String gambarBase64) {
        this.gambarBase64 = gambarBase64;
    }

    public String getNamaProyek() {
        return namaProyek;
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    // DEBUG METHOD
    public String toString() {
        return "FasilitasItem{" +
                "idFasilitas=" + idFasilitas +
                ", namaFasilitas='" + namaFasilitas + '\'' +
                ", namaProyek='" + namaProyek + '\'' +
                ", gambarBase64Length=" + (gambarBase64 != null ? gambarBase64.length() : 0) +
                '}';
    }
}