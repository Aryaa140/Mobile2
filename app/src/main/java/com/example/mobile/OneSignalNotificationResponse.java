package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class OneSignalNotificationResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("recipients")
    private int recipients;

    @SerializedName("errors")
    private String[] errors;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getRecipients() { return recipients; }
    public void setRecipients(int recipients) { this.recipients = recipients; }

    public String[] getErrors() { return errors; }
    public void setErrors(String[] errors) { this.errors = errors; }
}