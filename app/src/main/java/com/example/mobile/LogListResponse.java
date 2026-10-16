package com.example.mobile;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LogListResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<LogEntry> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<LogEntry> getData() {
        return data;
    }
}