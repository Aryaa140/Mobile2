package com.example.mobile;

import java.util.List;

public class LevelResponse {
    private boolean success;
    private List<LevelItem> data;
    private String message;

    // Getters and setters
    public boolean isSuccess() { return success; }
    public List<LevelItem> getData() { return data; }
    public String getMessage() { return message; }
}