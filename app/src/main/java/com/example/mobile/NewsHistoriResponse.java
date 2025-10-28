package com.example.mobile;

import java.util.List;

public class NewsHistoriResponse {
    private boolean success;
    private String message;
    private List<NewsHistoriItem> data;
    private int total;

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
                '}';
    }
}