package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HunianDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Hunian> data; // UNTUK DATA BARU - List<Hunian>

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Hunian> getData() { return data; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(List<Hunian> data) { this.data = data; }
}