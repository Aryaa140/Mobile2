package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProyekWithInfoResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<ProyekWithInfo> data;

    @SerializedName("message")
    private String message;

    @SerializedName("total")
    private int total;

    @SerializedName("debug")
    private Object debug;

    public boolean isSuccess() {
        return success;
    }

    public List<ProyekWithInfo> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public int getTotal() {
        return total;
    }

    public Object getDebug() {
        return debug;
    }
}