package com.example.mobile;

import java.util.Date;

public class NewsHistoriItem {
    private int id_news;
    private int promo_id;
    private String title;
    private String penginput;
    private String status;
    private String timestamp;
    private String image_base64;

    // Constructor
    public NewsHistoriItem() {
    }

    public NewsHistoriItem(int id_news, int promo_id, String title, String penginput,
                           String status, String timestamp, String image_base64) {
        this.id_news = id_news;
        this.promo_id = promo_id;
        this.title = title;
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.image_base64 = image_base64;
    }

    // Getters and Setters
    public int getId_news() {
        return id_news;
    }

    public void setId_news(int id_news) {
        this.id_news = id_news;
    }

    public int getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(int promo_id) {
        this.promo_id = promo_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPenginput() {
        return penginput;
    }

    public void setPenginput(String penginput) {
        this.penginput = penginput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_base64() {
        return image_base64;
    }

    public void setImage_base64(String image_base64) {
        this.image_base64 = image_base64;
    }

    // Helper methods
    public String getFormattedTime() {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Baru saja";
        }

        try {
            // Parse timestamp dari database (format: YYYY-MM-DD HH:MM:SS)
            java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dbFormat.parse(timestamp);

            long diff = System.currentTimeMillis() - date.getTime();
            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            long days = diff / (24 * 60 * 60 * 1000);

            if (minutes < 1) return "Baru saja";
            else if (minutes < 60) return minutes + " menit lalu";
            else if (hours < 24) return hours + " jam lalu";
            else return days + " hari lalu";

        } catch (Exception e) {
            return timestamp;
        }
    }

    public String getType() {
        if (status == null) return "promo_general";

        switch (status) {
            case "Ditambahkan":
                return "promo_added";
            case "Diubah":
                return "promo_updated";
            case "Dihapus":
                return "promo_deleted";
            default:
                return "promo_general";
        }
    }

    @Override
    public String toString() {
        return "NewsHistoriItem{" +
                "id_news=" + id_news +
                ", promo_id=" + promo_id +
                ", title='" + title + '\'' +
                ", penginput='" + penginput + '\'' +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", image_base64_length=" + (image_base64 != null ? image_base64.length() : 0) +
                '}';
    }
}