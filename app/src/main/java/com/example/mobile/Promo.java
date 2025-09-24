package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Promo {
    @SerializedName("id_promo")
    private int idPromo;

    @SerializedName("nama_promo")
    private String namaPromo;

    @SerializedName("nama_penginput")
    private String namaPenginput;

    @SerializedName("referensi_proyek")
    private String referensiProyek;

    @SerializedName("gambar_base64")
    private String gambarBase64;

    @SerializedName("tanggal_input")
    private String tanggalInput;

    // Constructor
    public Promo() {}

    public Promo(int idPromo, String namaPromo, String namaPenginput, String referensiProyek, String gambarBase64, String tanggalInput) {
        this.idPromo = idPromo;
        this.namaPromo = namaPromo;
        this.namaPenginput = namaPenginput;
        this.referensiProyek = referensiProyek;
        this.gambarBase64 = gambarBase64;
        this.tanggalInput = tanggalInput;
    }

    // Getter and Setter methods
    public int getIdPromo() { return idPromo; }
    public void setIdPromo(int idPromo) { this.idPromo = idPromo; }

    public String getNamaPromo() { return namaPromo; }
    public void setNamaPromo(String namaPromo) { this.namaPromo = namaPromo; }

    public String getNamaPenginput() { return namaPenginput; }
    public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }

    public String getReferensiProyek() { return referensiProyek; }
    public void setReferensiProyek(String referensiProyek) { this.referensiProyek = referensiProyek; }

    public String getGambarBase64() {
        // Pastikan base64 tidak null
        return gambarBase64 != null ? gambarBase64 : "";
    }
    public void setGambarBase64(String gambarBase64) { this.gambarBase64 = gambarBase64; }

    public String getTanggalInput() { return tanggalInput; }
    public void setTanggalInput(String tanggalInput) { this.tanggalInput = tanggalInput; }
}