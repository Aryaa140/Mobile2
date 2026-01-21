package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class ProyekHistoriItem {
    @SerializedName("id_news_proyek")
    private int idNewsProyek;

    @SerializedName("proyek_id")
    private int proyekId;

    @SerializedName("nama_proyek")
    private String namaProyek;

    @SerializedName("lokasi_proyek")
    private String lokasiProyek;

    @SerializedName("penginput")
    private String penginput;

    @SerializedName("status")
    private String status;

    @SerializedName("image_data")
    private String imageData;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("deleted_by")
    private String deletedBy;

    // Getters and Setters
    public int getIdNewsProyek() {
        return idNewsProyek;
    }

    public void setIdNewsProyek(int idNewsProyek) {
        this.idNewsProyek = idNewsProyek;
    }

    public int getProyekId() {
        return proyekId;
    }

    public void setProyekId(int proyekId) {
        this.proyekId = proyekId;
    }

    public String getNamaProyek() {
        return namaProyek != null ? namaProyek : "";
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    public String getLokasiProyek() {
        return lokasiProyek != null ? lokasiProyek : "";
    }

    public void setLokasiProyek(String lokasiProyek) {
        this.lokasiProyek = lokasiProyek;
    }

    public String getPenginput() {
        return penginput != null ? penginput : "";
    }

    public void setPenginput(String penginput) {
        this.penginput = penginput;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getTimestamp() {
        return timestamp != null ? timestamp : "";
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeletedBy() {
        return deletedBy != null ? deletedBy : "";
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    // Helper methods untuk NewsActivity
    public String getTitle() {
        return "Proyek: " + namaProyek;
    }

    public String getType() {
        return "proyek";
    }

    // Helper method untuk mendapatkan waktu relatif
    public String getFormattedTime() {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Baru saja";
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(timestamp);
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Baru saja";
            } else if (minutes == 1) {
                return "1 menit yang lalu";
            } else if (minutes < 60) {
                return minutes + " menit yang lalu";
            } else if (hours == 1) {
                return "1 jam yang lalu";
            } else if (hours < 24) {
                return hours + " jam yang lalu";
            } else if (days == 1) {
                return "1 hari yang lalu";
            } else {
                return days + " hari yang lalu";
            }
        } catch (Exception e) {
            return timestamp;
        }
    }
}