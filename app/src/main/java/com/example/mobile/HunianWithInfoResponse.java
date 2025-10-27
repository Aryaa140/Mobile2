package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HunianWithInfoResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<HunianWithInfo> data;

    @SerializedName("message")
    private String message;

    @SerializedName("total")
    private int total;

    public boolean isSuccess() {
        return success;
    }

    public List<HunianWithInfo> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public int getTotal() {
        return total;
    }
}