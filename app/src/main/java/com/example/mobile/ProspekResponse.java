package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProspekResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Prospek2> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Prospek2> getData() {
        return data;
    }
}