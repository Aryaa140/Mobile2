package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProyekHistoriResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<ProyekHistoriItem> data;

    @SerializedName("total")
    private int total;

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

    public List<ProyekHistoriItem> getData() {
        return data;
    }

    public void setData(List<ProyekHistoriItem> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean hasData() {
        return success && data != null && !data.isEmpty();
    }

    public int getDataCount() {
        return data != null ? data.size() : 0;
    }
}