package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class DeletedProyekResponse {
    @SerializedName("id_news_proyek")
    private int idNewsProyek;

    @SerializedName("proyek_id")
    private int proyekId;

    @SerializedName("nama_proyek")
    private String namaProyek;

    @SerializedName("lokasi_proyek")
    private String lokasiProyek; // ✅ TAMBAHKAN

    @SerializedName("penginput")
    private String penginput;

    @SerializedName("status")
    private String status;

    @SerializedName("image_data")
    private String imageData;

    @SerializedName("deleted_by")
    private String deletedBy; // ✅ TAMBAHKAN

    @SerializedName("timestamp")
    private String timestamp;

    // Getters dan Setters
    public int getIdNewsProyek() { return idNewsProyek; }
    public void setIdNewsProyek(int idNewsProyek) { this.idNewsProyek = idNewsProyek; }

    public int getProyekId() { return proyekId; }
    public void setProyekId(int proyekId) { this.proyekId = proyekId; }

    public String getNamaProyek() { return namaProyek != null ? namaProyek : ""; }
    public void setNamaProyek(String namaProyek) { this.namaProyek = namaProyek; }

    public String getLokasiProyek() { return lokasiProyek != null ? lokasiProyek : ""; } // ✅ TAMBAHKAN
    public void setLokasiProyek(String lokasiProyek) { this.lokasiProyek = lokasiProyek; }

    public String getPenginput() { return penginput != null ? penginput : ""; }
    public void setPenginput(String penginput) { this.penginput = penginput; }

    public String getStatus() { return status != null ? status : ""; }
    public void setStatus(String status) { this.status = status; }

    public String getImageData() { return imageData; }
    public void setImageData(String imageData) { this.imageData = imageData; }

    public String getDeletedBy() { return deletedBy != null ? deletedBy : ""; } // ✅ TAMBAHKAN
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getTimestamp() { return timestamp != null ? timestamp : ""; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}