package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AllNewsHistoriResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<AllNewsHistoriItem> data;

    @SerializedName("total")
    private int total;

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

    public List<AllNewsHistoriItem> getData() {
        return data;
    }

    public void setData(List<AllNewsHistoriItem> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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
        return "AllNewsHistoriResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data_count=" + (data != null ? data.size() : 0) +
                ", total=" + total +
                '}';
    }
}