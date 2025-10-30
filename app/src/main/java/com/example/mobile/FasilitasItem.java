package com.example.mobile;

public class FasilitasItem {
    private String namaFasilitas;
    private String gambarBase64;

    public FasilitasItem(String namaFasilitas, String gambarBase64) {
        this.namaFasilitas = namaFasilitas;
        this.gambarBase64 = gambarBase64;
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
}