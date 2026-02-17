package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatusLibResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("status_list")
    private List<String> statusList;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public String getMessage() {
        return message;
    }
}