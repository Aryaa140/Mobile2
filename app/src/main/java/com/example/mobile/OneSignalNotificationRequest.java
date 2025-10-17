package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class OneSignalNotificationRequest {

    @SerializedName("app_id")
    private String app_id;

    @SerializedName("included_segments")
    private String[] included_segments;

    @SerializedName("contents")
    private Contents contents;

    @SerializedName("headings")
    private Headings headings;

    @SerializedName("data")
    private Map<String, Object> data;

    // Getters and Setters
    public String getApp_id() { return app_id; }
    public void setApp_id(String app_id) { this.app_id = app_id; }

    public String[] getIncluded_segments() { return included_segments; }
    public void setIncluded_segments(String[] included_segments) { this.included_segments = included_segments; }

    public Contents getContents() { return contents; }
    public void setContents(Contents contents) { this.contents = contents; }

    public Headings getHeadings() { return headings; }
    public void setHeadings(Headings headings) { this.headings = headings; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    // Inner Classes
    public static class Contents {
        @SerializedName("en")
        private String en;

        public String getEn() { return en; }
        public void setEn(String en) { this.en = en; }
    }

    public static class Headings {
        @SerializedName("en")
        private String en;

        public String getEn() { return en; }
        public void setEn(String en) { this.en = en; }
    }
}