package com.example.mobile;

import java.util.List;

public class UserProspekDisplayResponse {
    private boolean success;
    private String message;
    private List<UserProspekDisplay> data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<UserProspekDisplay> getData() { return data; }
    public void setData(List<UserProspekDisplay> data) { this.data = data; }
}