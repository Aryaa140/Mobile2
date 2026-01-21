package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsHistoriResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<NewsHistoriItem> data;

    @SerializedName("total")
    private int total;

    // ✅ TAMBAHKAN FIELD BARU INI
    @SerializedName("deleted_count")
    private int deletedCount;

    @SerializedName("expired_count")
    private int expiredCount;

    @SerializedName("valid_images")
    private int validImages;

    // Constructor
    public NewsHistoriResponse() {
    }

    public NewsHistoriResponse(boolean success, String message, List<NewsHistoriItem> data, int total) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.total = total;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NewsHistoriItem> getData() {
        return data;
    }

    public void setData(List<NewsHistoriItem> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // ✅ TAMBAHKAN GETTER UNTUK FIELD BARU
    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(int expiredCount) {
        this.expiredCount = expiredCount;
    }

    public int getValidImages() {
        return validImages;
    }

    public void setValidImages(int validImages) {
        this.validImages = validImages;
    }

    // Helper methods
    public boolean hasData() {
        return success && data != null && !data.isEmpty();
    }

    public int getDataCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public String toString() {
        return "NewsHistoriResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data_count=" + (data != null ? data.size() : 0) +
                ", total=" + total +
                ", deleted_count=" + deletedCount +
                ", expired_count=" + expiredCount +
                ", valid_images=" + validImages +
                '}';
    }
}