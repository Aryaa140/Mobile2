package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HunianResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<String> data; // JANGAN DIUBAH - untuk kompatibilitas ke belakang

    @SerializedName("message")
    private String message;

    // TAMBAHKAN FIELD BARU untuk data hunian lengkap
    @SerializedName("hunian_list")
    private List<Hunian> hunianList;

    // GETTER YANG SUDAH ADA - JANGAN DIUBAH
    public boolean isSuccess() { return success; }
    public List<String> getData() { return data; }
    public String getMessage() { return message; }

    // TAMBAHKAN GETTER BARU untuk data hunian lengkap
    public List<Hunian> getHunianList() { return hunianList; }

    // TAMBAHKAN SETTER JIKA DIPERLUKAN
    public void setSuccess(boolean success) { this.success = success; }
    public void setData(List<String> data) { this.data = data; }
    public void setMessage(String message) { this.message = message; }
    public void setHunianList(List<Hunian> hunianList) { this.hunianList = hunianList; }

    // TAMBAHKAN METHOD BARU untuk kompatibilitas
    public boolean hasHunianList() {
        return hunianList != null && !hunianList.isEmpty();
    }
}