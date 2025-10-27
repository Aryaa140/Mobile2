package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KavlingWithInfoResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<KavlingWithInfo> data;

    @SerializedName("message")
    private String message;

    @SerializedName("total")
    private int total;

    public boolean isSuccess() {
        return success;
    }

    public List<KavlingWithInfo> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public int getTotal() {
        return total;
    }
}