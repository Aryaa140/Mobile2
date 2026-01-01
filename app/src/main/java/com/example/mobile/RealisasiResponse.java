package com.example.mobile;

import java.util.List;

public class RealisasiResponse {
    private boolean success;
    private String message;
    private List<Realisasi> data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Realisasi> getData() { return data; }
    public void setData(List<Realisasi> data) { this.data = data; }
}