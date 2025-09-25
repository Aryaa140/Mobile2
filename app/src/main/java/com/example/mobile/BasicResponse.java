package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;
    @SerializedName("available")
    private Boolean available;
    // GETTERS ONLY - jangan ada setters untuk avoid issues
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message != null ? message : "";
    }
    public boolean isAvailable() {
        return available != null && available; // Method ini diperlukan
    }
}