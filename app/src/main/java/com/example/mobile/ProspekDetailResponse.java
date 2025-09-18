package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class ProspekDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Prospek2 data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Prospek2 getData() {
        return data;
    }
}