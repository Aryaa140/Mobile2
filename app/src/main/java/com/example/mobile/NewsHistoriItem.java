package com.example.mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsHistoriItem {
    private int id_news;
    private int promo_id;
    private String title;
    private String penginput;
    private String status;
    private String timestamp;
    private String image_base64;
    private String kadaluwarsa;
    private SimpleDateFormat outputFormat;

    public NewsHistoriItem() {
    }

    public NewsHistoriItem(int id_news, int promo_id, String title, String penginput,
                           String status, String timestamp, String image_base64, String kadaluwarsa) {
        this.id_news = id_news;
        this.promo_id = promo_id;
        this.title = title;
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.image_base64 = image_base64;
        this.kadaluwarsa = kadaluwarsa;
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

    public String getKadaluwarsa() {
        return kadaluwarsa;
    }

    public void setKadaluwarsa(String kadaluwarsa) {
        this.kadaluwarsa = kadaluwarsa;
    }

    // PERBAIKAN: Helper methods untuk timestamp relatif
    public String getFormattedTime() {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Baru saja";
        }

        try {
            // Parse timestamp dari database (format: YYYY-MM-DD HH:MM:SS)
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dbFormat.parse(timestamp);

            long diff = System.currentTimeMillis() - date.getTime();
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

        } catch (Exception e) {
            // Fallback ke format asli jika parsing gagal
            return timestamp;
        }
    }

    // ✅ METHOD BARU: Format kadaluwarsa untuk tampilan
    public String getFormattedKadaluwarsa() {
        if (kadaluwarsa == null || kadaluwarsa.isEmpty() || kadaluwarsa.equals("null")) {
            return "Tidak ada kadaluwarsa";
        }

        try {
            // Format dari database: yyyy-MM-dd
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // Format untuk tampilan: dd MMMM yyyy
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

            Date date = inputFormat.parse(kadaluwarsa);
            return outputFormat.format(date);

        } catch (Exception e) {
            // Fallback: coba format lain atau return as-is
            try {
                // Coba format dengan timezone
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date altDate = altFormat.parse(kadaluwarsa);
                return outputFormat.format(altDate);
            } catch (Exception e2) {
                return kadaluwarsa; // Return as-is jika semua parsing gagal
            }
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
            case "Kadaluwarsa":
                return "promo_expired";
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
                ", kadaluwarsa='" + kadaluwarsa + '\'' +
                '}';
    }
}