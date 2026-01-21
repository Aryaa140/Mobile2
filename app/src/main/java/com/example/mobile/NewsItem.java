package com.example.mobile;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsItem {
    private int id;
    private String title;
    private String penginput;
    private String status;
    private Date timestamp;
    private String imageUrl;
    private String kadaluwarsa;
    private int promoId;
    private String type;
    private String itemType;

    // Constructor utama (9 parameter)
    public NewsItem(int id, String title, String penginput, String status, Date timestamp,
                    String imageUrl, int promoId, String kadaluwarsa, String itemType) {
        this.id = id;
        this.title = title != null ? title : "Unknown Title";
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.promoId = promoId;
        this.kadaluwarsa = kadaluwarsa;
        this.itemType = itemType != null ? itemType : "promo";
        this.type = determineType(status);
    }

    // Constructor untuk backward compatibility (8 parameter)
    public NewsItem(int id, String title, String penginput, String status, Date timestamp,
                    String imageUrl, int promoId, String kadaluwarsa) {
        this(id, title, penginput, status, timestamp, imageUrl, promoId, kadaluwarsa, "promo");
    }

    // Constructor dengan 7 parameter (tanpa kadaluwarsa dan itemType)
    public NewsItem(int id, String title, String penginput, String status, Date timestamp,
                    String imageUrl, int promoId) {
        this(id, title, penginput, status, timestamp, imageUrl, promoId, "", "promo");
    }

    // Constructor dengan 8 parameter yang berbeda (untuk error di NewsActivity)
    public NewsItem(int id, String title, String inputter, String status, Date timestamp,
                    String imageUrl, String itemType, int promoId) {
        this(id, title, inputter, status, timestamp, imageUrl, promoId, "", itemType);
    }

    // Getter dan Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPenginput() { return penginput; }
    public void setPenginput(String penginput) { this.penginput = penginput; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.type = determineType(status);
    }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getPromoId() { return promoId; }
    public void setPromoId(int promoId) { this.promoId = promoId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

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

    // PERBAIKAN: Method untuk mendapatkan waktu relatif yang lebih natural
    public String getFormattedTimestamp() {
        if (timestamp == null) return "Baru saja";

        long diff = System.currentTimeMillis() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

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
        } else if (days < 7) {
            return days + " hari yang lalu";
        } else if (weeks == 1) {
            return "1 minggu yang lalu";
        } else if (weeks < 4) {
            return weeks + " minggu yang lalu";
        } else if (months == 1) {
            return "1 bulan yang lalu";
        } else if (months < 12) {
            return months + " bulan yang lalu";
        } else if (years == 1) {
            return "1 tahun yang lalu";
        } else {
            return years + " tahun yang lalu";
        }
    }

    // PERBAIKAN: Method untuk format tanggal lengkap (fallback)
    public String getFullFormattedTimestamp() {
        if (timestamp == null) return "";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy 'pukul' HH:mm", Locale.getDefault());
            return dateFormat.format(timestamp);
        } catch (Exception e) {
            return getFormattedTimestamp(); // Fallback ke relative time
        }
    }

    // DI NewsItem.java - Pastikan method getFormattedKadaluwarsa
    public String getFormattedKadaluwarsa() {
        if (kadaluwarsa == null || kadaluwarsa.isEmpty() || kadaluwarsa.equals("null")) {
            return "Tidak ada kadaluwarsa";
        }

        try {
            // Coba berbagai format tanggal
            SimpleDateFormat[] inputFormats = {
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            };

            Date date = null;
            for (SimpleDateFormat format : inputFormats) {
                try {
                    date = format.parse(kadaluwarsa);
                    break;
                } catch (ParseException e) {
                }
            }

            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                return outputFormat.format(date);
            } else {
                // Jika parsing gagal, return as-is
                return kadaluwarsa;
            }

        } catch (Exception e) {
            Log.e("NewsItem", "Error formatting kadaluwarsa: " + kadaluwarsa + " - " + e.getMessage());
            return kadaluwarsa;
        }
    }

    // Method untuk menentukan type berdasarkan status
    private String determineType(String status) {
        switch (status) {
            case "Ditambahkan":
                return "promo_added";
            case "Diubah":
                return "promo_updated";
            case "Dihapus":
                return "promo_deleted";
            case "Kadaluwarsa":
                return "promo_expired";
            default:
                return "promo_general";
        }
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", itemType='" + itemType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    // Di NewsItem.java - tambahkan Builder class jika belum ada
    public static class Builder {
        private int id;
        private String title;
        private String penginput;
        private String status;
        private Date timestamp;
        private String imageUrl;
        private int promoId;
        private String kadaluwarsa;
        private String itemType;

        public Builder setId(int id) { this.id = id; return this; }
        public Builder setTitle(String title) { this.title = title; return this; }
        public Builder setPenginput(String penginput) { this.penginput = penginput; return this; }
        public Builder setStatus(String status) { this.status = status; return this; }
        public Builder setTimestamp(Date timestamp) { this.timestamp = timestamp; return this; }
        public Builder setImageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder setPromoId(int promoId) { this.promoId = promoId; return this; }
        public Builder setKadaluwarsa(String kadaluwarsa) { this.kadaluwarsa = kadaluwarsa; return this; }
        public Builder setItemType(String itemType) { this.itemType = itemType; return this; }

        public NewsItem build() {
            return new NewsItem(id, title, penginput, status, timestamp,
                    imageUrl, promoId, kadaluwarsa, itemType);
        }
    }
}