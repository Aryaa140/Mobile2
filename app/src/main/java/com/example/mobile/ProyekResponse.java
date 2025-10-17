package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProyekResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<Proyek> data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<Proyek> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}