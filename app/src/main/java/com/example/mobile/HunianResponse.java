package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HunianResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<String> data;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public List<String> getData() { return data; }
    public String getMessage() { return message; }
}