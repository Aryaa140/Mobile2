package com.example.mobile;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AllNewsHistoriItem {
    @SerializedName("id")
    private int id;

    @SerializedName("reference_id")
    private int referenceId;

    @SerializedName("title")
    private String title;

    @SerializedName("penginput")
    private String penginput;

    @SerializedName("status")
    private String status;

    @SerializedName("image_base64")
    private String imageBase64;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("kadaluwarsa")
    private String kadaluwarsa;

    @SerializedName("item_type")
    private String itemType;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getTimestamp() {
        return timestamp != null ? timestamp : "";
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getKadaluwarsa() {
        return kadaluwarsa != null ? kadaluwarsa : "";
    }

    public void setKadaluwarsa(String kadaluwarsa) {
        this.kadaluwarsa = kadaluwarsa;
    }

    public String getItemType() {
        return itemType != null ? itemType : "promo";
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
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