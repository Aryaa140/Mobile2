package com.example.mobile;

import java.util.Date;

public class NewsItem {
    private int id;
    private String title;
    private String penginput;
    private String status;
    private Date timestamp;
    private String imageUrl; // Ini akan berisi base64 string
    private int promoId;

    // PERBAIKAN: Hapus field action yang tidak digunakan
    public NewsItem(int id, String title, String penginput, String status, Date timestamp, String imageUrl, int promoId) {
        this.id = id;
        this.title = title;
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.promoId = promoId;
    }

    // Getter dan Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPenginput() { return penginput; }
    public void setPenginput(String penginput) { this.penginput = penginput; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }
}