package com.example.mobile;

public class LatestSuffixResponse {
    private boolean success;
    private int max_suffix;
    private String message;

    public boolean isSuccess() { return success; }
    public int getMaxSuffix() { return max_suffix; }
    public String getMessage() { return message; }
}