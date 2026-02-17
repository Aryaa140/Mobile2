package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class UserProspekRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("data")
    private UserProspekData data;

    public UserProspekRequest(String action, UserProspekData data) {
        this.action = action;
        this.data = data;
    }

    // Getter dan Setter
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UserProspekData getData() {
        return data;
    }

    public void setData(UserProspekData data) {
        this.data = data;
    }
}