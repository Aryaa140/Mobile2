package com.example.mobile;

import java.util.List;

public class HistoriStatusLibResponse {
    private boolean success;
    private String message;
    private List<HistoriStatusLib> data;

    // Getter dan Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<HistoriStatusLib> getData() { return data; }
    public void setData(List<HistoriStatusLib> data) { this.data = data; }
}