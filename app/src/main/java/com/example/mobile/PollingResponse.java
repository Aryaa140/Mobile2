package com.example.mobile;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PollingResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("new_promos")
    private List<Promo> newPromos;

    @SerializedName("count")
    private int count;

    @SerializedName("current_time")
    private String currentTime;

    @SerializedName("last_check_used")
    private String lastCheckUsed;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<Promo> getNewPromos() { return newPromos; }
    public void setNewPromos(List<Promo> newPromos) { this.newPromos = newPromos; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getCurrentTime() { return currentTime; }
    public void setCurrentTime(String currentTime) { this.currentTime = currentTime; }

    public String getLastCheckUsed() { return lastCheckUsed; }
    public void setLastCheckUsed(String lastCheckUsed) { this.lastCheckUsed = lastCheckUsed; }
}