package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DivisiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("divisi_list")
    private List<String> divisiList;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<String> getDivisiList() {
        return divisiList;
    }

    public String getMessage() {
        return message;
    }
}