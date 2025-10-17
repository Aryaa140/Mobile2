package com.example.mobile;

import java.util.List;

public class UserProspekSimpleResponse {
    private boolean success;
    private String message;
    private List<UserProspekSimple> data;

    // PASTIKAN ADA GETTER METHODS
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<UserProspekSimple> getData() {
        return data;
    }

    // DAN SETTER METHODS (PENTING UNTUK GSON)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<UserProspekSimple> data) {
        this.data = data;
    }
}