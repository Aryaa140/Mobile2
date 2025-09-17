package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // Getter dan Setter
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}