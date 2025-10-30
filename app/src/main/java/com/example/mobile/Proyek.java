package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Proyek {
    @SerializedName("id_proyek")
    private int idProyek;

    @SerializedName("nama_proyek")
    private String namaProyek;

    // Tambahkan field-field baru di bawah ini
    @SerializedName("lokasi_proyek")
    private String lokasiProyek;

    @SerializedName("deskripsi_proyek")
    private String deskripsiProyek;

    @SerializedName("logo")
    private String logoBase64;

    @SerializedName("siteplan")
    private String siteplanBase64;

    // Default constructor untuk Gson - TIDAK DIUBAH
    public Proyek() {
    }

    // Constructor existing - TIDAK DIUBAH
    public Proyek(int idProyek, String namaProyek) {
        this.idProyek = idProyek;
        this.namaProyek = namaProyek;
    }

    // Constructor baru untuk semua field
    public Proyek(int idProyek, String namaProyek, String lokasiProyek, String deskripsiProyek) {
        this.idProyek = idProyek;
        this.namaProyek = namaProyek;
        this.lokasiProyek = lokasiProyek;
        this.deskripsiProyek = deskripsiProyek;
    }

    // Getter dan Setter existing - TIDAK DIUBAH
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

    // Tambahkan Getter dan Setter untuk field baru
    public String getLokasiProyek() {
        return lokasiProyek != null ? lokasiProyek : "";
    }

    public void setLokasiProyek(String lokasiProyek) {
        this.lokasiProyek = lokasiProyek;
    }

    public String getDeskripsiProyek() {
        return deskripsiProyek != null ? deskripsiProyek : "";
    }

    public void setDeskripsiProyek(String deskripsiProyek) {
        this.deskripsiProyek = deskripsiProyek;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getSiteplanBase64() {
        return siteplanBase64;
    }

    public void setSiteplanBase64(String siteplanBase64) {
        this.siteplanBase64 = siteplanBase64;
    }
}