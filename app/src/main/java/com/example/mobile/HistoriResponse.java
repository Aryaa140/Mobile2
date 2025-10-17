package com.example.mobile;

import java.util.List;

public class HistoriResponse {
    private boolean success;
    private String message;
    private List<HistoriUserProspek> data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<HistoriUserProspek> getData() { return data; }
    public void setData(List<HistoriUserProspek> data) { this.data = data; }
}