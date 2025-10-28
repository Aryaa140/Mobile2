package com.example.mobile;

import java.util.Date;

public class NewsItem {
    private int id;
    private String title;
    private String penginput;
    private String status;
    private Date timestamp;
    private String imageUrl;
    private int promoId;
    private String type; // Tambahkan field type

    public NewsItem(int id, String title, String penginput, String status, Date timestamp, String imageUrl, int promoId) {
        this.id = id;
        this.title = title;
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.promoId = promoId;
        this.type = determineType(status); // Set type berdasarkan status
    }

    // Getter dan Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.type = determineType(this.status); // Update type ketika title berubah
    }

    public String getPenginput() { return penginput; }
    public void setPenginput(String penginput) { this.penginput = penginput; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.type = determineType(status); // Update type ketika status berubah
    }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // Method untuk menentukan type berdasarkan status
    private String determineType(String status) {
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

    // Method untuk mendapatkan waktu yang diformat
    public String getTime() {
        if (timestamp == null) return "";

        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1) return "Baru saja";
        else if (minutes < 60) return minutes + " menit lalu";
        else if (hours < 24) return hours + " jam lalu";
        else return days + " hari lalu";
    }
}