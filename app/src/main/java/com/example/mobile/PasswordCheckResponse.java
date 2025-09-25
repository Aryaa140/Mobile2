package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class PasswordCheckResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("exists")
    private boolean exists;

    @SerializedName("message")
    private String message;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public boolean isExists() {
        return exists;
    }

    public String getMessage() {
        return message != null ? message : "";
    }
}