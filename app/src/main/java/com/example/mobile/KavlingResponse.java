package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KavlingResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Kavling> data;
    public KavlingResponse() {}
    // Getter methods
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Kavling> getData() { return data; }

    // Setter methods
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(List<Kavling> data) { this.data = data; }
}